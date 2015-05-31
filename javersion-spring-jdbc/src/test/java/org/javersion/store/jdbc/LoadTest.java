package org.javersion.store.jdbc;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.javersion.path.PropertyPath.ROOT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.javersion.store.PersistenceTestConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

    @Resource
    ObjectVersionStoreJdbc<String, Void> versionStore;

    @Test
    @Ignore
    public void performance() {
        long ts;
        final int docCount = 100;
        final int docVersionCount = 50;
        final int propCount = 100;

//        VersionGraphCache<String, Void> cache = new VersionGraphCache<>(versionStore,
//                CacheBuilder.<String, ObjectVersionGraph<Void>>newBuilder()
//                        .maximumSize(docCount)
//                        .refreshAfterWrite(1, TimeUnit.NANOSECONDS));

        List<String> docIds = generateDocIds(docCount);
        for (int round=1; round <= docVersionCount; round++) {
            for (String docId : docIds) {
                ts = currentTimeMillis();

                ObjectVersionGraph<Void> versionGraph = versionStore.load(docId);
//                ObjectVersionGraph<Void> versionGraph = cache.load(docId);

                print(round, "load", ts);

                ObjectVersion.Builder<Void> builder = ObjectVersion.<Void>builder()
                        .changeset(generateProperties(propCount));

                if (!versionGraph.isEmpty()) {
                    builder.parents(versionGraph.getTip().getRevision());
                }

                ObjectVersion<Void> version = builder.build();

                ts = currentTimeMillis();
                versionStore.append(docId, versionGraph.commit(version).getTip());
                print(round, "append", ts);
            }
            ts = currentTimeMillis();
            versionStore.publish();
            print(round, "publish", ts);
        }
    }

    private void print(int round, String type, long ts) {
        double expired = currentTimeMillis() - ts;
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
        List<String> ids = new ArrayList<>(count);
        for (int i=0; i < count; i++) {
            ids.add(UUID.randomUUID().toString());
        }
        return ids;
    }

}
