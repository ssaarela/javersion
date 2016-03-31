package org.javersion.store.jdbc;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.javersion.path.PropertyPath.ROOT;

import java.util.*;

import javax.annotation.Resource;

import org.javersion.core.VersionNode;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.javersion.store.PersistenceTestConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

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
    private final int docVersionCount = 500;
    private final int propCount = 20;

    private final int optimizeEvery = 50;
    private final int optimizeKeepNewest = 15;

    @Resource
    DocumentVersionStoreJdbc<String, Void, JDocumentVersion<String>> documentStore;

    @Resource
    CustomEntityVersionStore entityStore;

    @Resource
    TransactionTemplate transactionTemplate;

    @Test
    @Ignore
    public void document_store_performance() {
        long ts;

//        VersionGraphCache<String, Void> cache = new VersionGraphCache<>(documentStore,
//                CacheBuilder.<String, ObjectVersionGraph<Void>>newBuilder()
//                        .maximumSize(docCount)
//                        .refreshAfterWrite(1, TimeUnit.NANOSECONDS));

        List<String> docIds = generateDocIds(docCount);
        for (int round=1; round <= docVersionCount; round++) {
            for (String docId : docIds) {
                ts = currentTimeMillis();

                ObjectVersionGraph<Void> versionGraph = documentStore.load(docId);
//                ObjectVersionGraph<Void> versionGraph = cache.load(docId);

                print(round, "load", ts);

                ObjectVersion.Builder<Void> builder = ObjectVersion.<Void>builder()
                        .changeset(generateProperties(propCount));

                if (!versionGraph.isEmpty()) {
                    builder.parents(versionGraph.getTip().getRevision());
                }

                ObjectVersion<Void> version = builder.build();

                ts = currentTimeMillis();
                documentStore.append(docId, versionGraph.commit(version).getTip());
                print(round, "append", ts);
            }
            ts = currentTimeMillis();
            documentStore.publish();
            print(round, "publish", ts);
        }
    }

    @Test
    @Ignore
    public void document_store_batch_performance() {
        String appendLabel = "append" + docCount;
        String loadLabel = "load" + docCount;
        long ts;

        List<String> docIds = generateDocIds(docCount);
        Multimap<String, VersionNode<PropertyPath, Object, Void>> versions = ArrayListMultimap.create(docCount, propCount);
        for (String docId : docIds) {
            ObjectVersion.Builder<Void> builder = ObjectVersion.<Void>builder()
                    .changeset(generateProperties(propCount));

            ObjectVersionGraph<Void> versionGraph = ObjectVersionGraph.<Void>init(builder.build());

            versions.put(docId, versionGraph.getTip());
        }
        ts = currentTimeMillis();
        documentStore.append(versions);
        print(1, appendLabel, ts);

        documentStore.publish();

        for (int round=2; round <= docVersionCount; round++) {

            ts = currentTimeMillis();
            FetchResults<String, Void> results = documentStore.load(docIds);
            print(round, loadLabel, ts);

            versions = ArrayListMultimap.create(docCount, propCount);
            for (String docId : docIds) {
                ObjectVersion.Builder<Void> builder = ObjectVersion.<Void>builder()
                        .changeset(generateProperties(propCount));

                ObjectVersionGraph<Void> versionGraph = results.getVersionGraph(docId);
                builder.parents(versionGraph.getTip().getRevision());

                versionGraph = versionGraph.commit(builder.build());
                versions.put(docId, versionGraph.getTip());
            }

            ts = currentTimeMillis();
            documentStore.append(versions);
            print(round, appendLabel, ts);

            documentStore.publish();
        }
    }

    @Test
    @Ignore
    public void entity_store_performance() {
        long ts;

        List<String> docIds = generateDocIds(docCount);
        for (int round=1; round <= docVersionCount; round++) {
            for (String docId : docIds) {
                ts = currentTimeMillis();

                final ObjectVersionGraph<String> versionGraph =
                        transactionTemplate.execute(status -> entityStore.loadOptimized(docId));

                print(round, "load", ts);

                ObjectVersion.Builder<String> builder = ObjectVersion.<String>builder()
                        .changeset(generateProperties(propCount));

                if (!versionGraph.isEmpty()) {
                    builder.parents(versionGraph.getTip().getRevision());
                }

                final ObjectVersion<String> version = builder.build();

                ts = currentTimeMillis();

                transactionTemplate.execute(status -> {
                    EntityUpdateBatch<String, String, JEntityVersion<String>> update = entityStore.updateBatch(docId);
                    update.addVersion(docId, versionGraph.commit(version).getTip());
                    update.execute();
                    return null;
                });
                print(round, "append", ts);
            }
            ts = currentTimeMillis();
            entityStore.publish();
            print(round, "publish", ts);

            if (round % optimizeEvery == 0) {
                for (String docId : docIds) {
                    ts = currentTimeMillis();
                    entityStore.optimize(docId, graph -> new CacheOptions.KeepHeadsAndNewest<String>(graph, optimizeKeepNewest));
                    print(round, "optimize", ts);
                }
            }
        }
    }

    private void print(int round, String type, long ts) {
        long expired = currentTimeMillis() - ts;
        System.out.println(format("%s,%s,%s", round, type, expired));
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
