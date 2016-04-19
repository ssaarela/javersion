package org.javersion.store.jdbc;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.javersion.path.PropertyPath.ROOT;
import static org.javersion.store.jdbc.GraphOptions.keepHeadsAndNewest;
import static org.javersion.store.jdbc.ExecutorType.ASYNC;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
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

    private final int docCount = 200;
    private final int docVersionCount = 200;
    private final int propCount = 20;
    private final int keepNewest = 20;
    private final int compactThreshold = 100;
    private final Executor testExecutor = newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final GraphOptions<String, String> graphOptions = keepHeadsAndNewest(keepNewest, compactThreshold);

    private Writer out;

    private String storeName;

    private VersionStore<String, String> store;

    private VersionGraphCache<String, String> cache;

    @Resource
    DocumentStoreOptions<String, String, JDocumentVersion<String>> documentStoreOptions;

    @Resource
    EntityStoreOptions<String, String, JEntityVersion<String>> entityStoreOptions;


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
    public void document_store_performance() throws Exception {
        List<String> docIds = generateDocIds(docCount);
        store = new DocumentVersionStoreJdbc<>(documentStoreOptions.toBuilder()
                .graphOptions(graphOptions)
                .publisherType(ASYNC)
                .optimizerType(ASYNC)
                .build());
        run(docIds);
    }

    @Test
    @Ignore
    public void entity_store_performance() throws Exception {
        List<String> docIds = generateDocIds(docCount);
        store = new EntityVersionStoreJdbc<>(entityStoreOptions.toBuilder()
                .graphOptions(graphOptions)
                .publisherType(ASYNC)
                .optimizerType(ASYNC)
                .build());
        run(docIds);
    }

    private void run(List<String> docIds) throws IOException, InterruptedException {
        storeName = store.getClass().getSimpleName();

        cache = new VersionGraphCache<>(store,
                CacheBuilder.<String, ObjectVersionGraph<Void>>newBuilder()
                        .maximumSize(docIds.size())
                        .refreshAfterWrite(1, TimeUnit.NANOSECONDS),
                graphOptions);

        print(
                "#Store",

                "Load Size",
                "Optimized Size",
                "Cached Size",

                "Load Time",
                "Optimized(" + keepNewest + "/" + compactThreshold + ") Time",
                "Cached Time",
                "Append Time"
        );

        // Insert first versions
        for (String docId : docIds) {
            tick(docId, false);
        }

//        runByDocId(docIds);
        runByVersion(docIds);
//        CountDownLatch countDownLatch = new CountDownLatch(docIds.size() * docVersionCount);
//        for (int round = 0; round < docVersionCount; round++) {
//            for (String docId : docIds) {
//                testExecutor.execute(() -> {
//                    try {
//                        tick(docId, true);
//                    } finally {
//                        countDownLatch.countDown();
//                    }
//                });
//            }
//        }
    }

    private void runByDocId(List<String> docIds) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(docIds.size());
        for (String docId : docIds) {
            testExecutor.execute(() -> {
                for (int round = 0; round < docVersionCount; round++) {
                    try {
                        tick(docId, true);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        countDownLatch.await();
    }

    private void runByVersion(List<String> docIds) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(docIds.size() * docVersionCount);
        for (int round = 0; round < docVersionCount; round++) {
            for (String docId : docIds) {
                testExecutor.execute(() -> {
                    try {
                        tick(docId, true);
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }
        }
        countDownLatch.await();
    }

    private void tick(String docId, boolean printResult) {
        ObjectVersionGraph<String> versionGraph;

        // Load full
        long ts = currentTimeMillis();
        versionGraph = store.load(docId);
        final long loadSize = versionGraph.size();
        final long loadTime = currentTimeMillis() - ts;

        final ObjectVersionGraph<String> baseGraph = versionGraph;

        // Load optimized
        ts = currentTimeMillis();
        versionGraph = store.loadOptimized(docId);
        final long optimizedSize = versionGraph.size();
        final long optimizedTime = currentTimeMillis() - ts;

        // Load cached
        ts = currentTimeMillis();
        versionGraph = cache.load(docId);
        final long cachedSize = versionGraph.size();
        final long cachedTime = currentTimeMillis() - ts;


        ObjectVersion.Builder<String> builder = ObjectVersion.<String>builder()
                .changeset(generateProperties(propCount));

        if (!baseGraph.isEmpty()) {
            builder.parents(baseGraph.getHeadRevisions());
        }

        // Add version
        ts = currentTimeMillis();
        transactionTemplate.execute(status -> {
            store.updateBatch(docId)
                    .addVersion(docId, baseGraph.commit(builder.build()).getTip())
                    .execute();
            return null;
        });
        final long appendTime = currentTimeMillis() - ts;

        if (printResult) {
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
    }

    private synchronized void print(Object... values) {
        StringBuilder sb = new StringBuilder(64);
        for (Object value : values) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(value);
        }
        sb.append('\n');
        try {
            out.append(sb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        // select '"' || id || '",' from entity limit 100;
//         return Arrays.asList(
//         ).subList(0, count);

        List<String> ids = new ArrayList<>(count);
        for (int i=0; i < count; i++) {
            ids.add(UUID.randomUUID().toString());
        }
        return ids;
    }

}
