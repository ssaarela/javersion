package org.javersion.store.jdbc;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.javersion.path.PropertyPath.ROOT;
import static org.javersion.store.jdbc.GraphOptions.keepHeadsAndNewest;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.javersion.store.PersistenceTestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.cache.CacheBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PersistenceTestConfiguration.class)
public class LoadTest {

    private static final String[] VALUES = {"Lorem","ipsum","dolor","sit","amet,","consectetuer","adipiscing","elit.","Sed","posuere","interdum","sem.",
            "Quisque","ligula","eros","ullamcorper","quis,","lacinia","quis","facilisis","sed","sapien.","Mauris","varius","diam","vitae","arcu.","Sed",
            "arcu","lectus","auctor","vitae,","consectetuer","et","venenatis","eget","velit.","Sed","augue","orci,","lacinia","eu","tincidunt","et",
            "eleifend","nec","lacus.","Donec","ultricies","nisl","ut","felis,","suspendisse","potenti.","Lorem","ipsum","ligula","ut","hendrerit",
            "mollis,","ipsum","erat","vehicula","risus,","eu","suscipit","sem","libero","nec","erat.","Aliquam","erat","volutpat.","Sed","congue",
            "augue","vitae","neque.","Nulla","consectetuer","porttitor","pede.","Fusce","purus","morbi","tortor","magna","condimentum","vel,","placerat",
            "id","blandit","sit","amet","tortor"};

    private int nextValue = 0;

    private final int docCount = 100;
    private final int docVersionCount = 250;
    private final int propCount = 20;
    private final int keepNewest = 10;
    private final int compactThreshold = 50;
    private final GraphOptions<String, String> graphOptions = keepHeadsAndNewest(keepNewest, compactThreshold);

    private Writer out;

    @Resource
    DocumentStoreOptions<String, String, JDocumentVersion<String>> documentStoreOptions;

    @Resource
    EntityStoreOptions<String, String, JEntityVersion<String>> entityStoreOptions;

    private final Executor executor = newCachedThreadPool();

    @Resource
    TransactionTemplate transactionTemplate;

    @Before
    public void initWriter() throws FileNotFoundException, UnsupportedEncodingException {
        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("results.csv", false), "utf-8"));
    }

    @After
    public void closeWriter() throws IOException {
        out.flush();
        out.close();
    }

    @Test
    @Ignore
    public void document_store_performance() throws IOException {
        List<String> docIds = generateDocIds(docCount);
        DocumentVersionStoreJdbc<String, String, JDocumentVersion<String>> store = new DocumentVersionStoreJdbc<>(documentStoreOptions.toBuilder()
                .graphOptions(graphOptions)
                .executor(executor)
                .build());
        run(store, docIds);
    }

    @Test
    @Ignore
    public void entity_store_performance() throws IOException {
        List<String> docIds = generateDocIds(docCount);
        EntityVersionStoreJdbc<String, String, JEntityVersion<String>> store = new EntityVersionStoreJdbc<>(entityStoreOptions.toBuilder()
                .graphOptions(graphOptions)
                .executor(executor)
                .build());
        run(store, docIds);
    }

    private void run(VersionStore<String, String> store, List<String> docIds) throws IOException {
        final String storeName = store.getClass().getSimpleName();

        long ts;

        VersionGraphCache<String, String> cache = new VersionGraphCache<>(store,
                CacheBuilder.<String, ObjectVersionGraph<Void>>newBuilder()
                        .maximumSize(docIds.size())
                        .refreshAfterWrite(1, TimeUnit.NANOSECONDS),
                graphOptions);

        print(
                "Store",

                "Load Size",
                "Optimized Size",
                "Cached Size",

                "Load Time",
                "Optimized(" + keepNewest + "/" + compactThreshold + ") Time",
                "Cached Time",
                "Append Time"
        );
        for (int round=1; round <= docVersionCount; round++) {
            for (String docId : docIds) {
                ObjectVersionGraph<String> versionGraph;

                // Load full
                ts = currentTimeMillis();
                versionGraph = store.load(docId);
                final long loadSize = versionGraph.versionNodes.size();
                final long loadTime = currentTimeMillis() - ts;

                final ObjectVersionGraph<String> fullGraph = versionGraph;

                // Load optimized
                ts = currentTimeMillis();
                versionGraph = store.loadOptimized(docId);
                final long optimizedSize = versionGraph.versionNodes.size();
                final long optimizedTime = currentTimeMillis() - ts;

                // Load cached
                ts = currentTimeMillis();
                versionGraph = cache.load(docId);
                final long cachedSize = versionGraph.versionNodes.size();
                final long cachedTime = currentTimeMillis() - ts;


                ObjectVersion.Builder<String> builder = ObjectVersion.<String>builder()
                        .changeset(generateProperties(propCount));

                if (!versionGraph.isEmpty()) {
                    builder.parents(versionGraph.getTip().getRevision());
                }

                ObjectVersion<String> version = builder.build();

                // Add version
                ts = currentTimeMillis();
                transactionTemplate.execute(status -> {
                    store.updateBatch(docId)
                            .addVersion(docId, fullGraph.commit(version).getTip())
                            .execute();
                    return null;
                });
                final long appendTime = currentTimeMillis() - ts;

                print(
                        storeName,

                        loadSize,
                        optimizedSize,
                        cachedSize,

                        loadTime,
                        optimizedTime,
                        cachedTime,
                        appendTime
                );
            }
            store.publish();
            out.flush();
        }
    }

    private void print(Object... values) throws IOException {
        StringBuilder sb = new StringBuilder(64);
        for (Object value : values) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(value);
        }
        sb.append('\n');
        out.append(sb);
    }

    private Map<PropertyPath, Object> generateProperties(int count) {
        Map<PropertyPath, Object> properties = new HashMap<>();
        PropertyPath.Property list = ROOT.property("list");
        for (int i=1; i <= count; i++) {
            properties.put(list.index(i), VALUES[nextValue++ % VALUES.length]);
        }
        return properties;
    }

    private List<String> generateDocIds(int count) {
        // select id from entity;
        // replace regex in selection: ([0-9\-a-z]+) "$1",
        // return Arrays.asList(
        // ).subList(0, count);

        List<String> ids = new ArrayList<>(count);
        for (int i=0; i < count; i++) {
            ids.add(UUID.randomUUID().toString());
        }
        return ids;
    }

}
