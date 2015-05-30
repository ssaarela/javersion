package org.javersion.store.jdbc;

import static java.util.UUID.randomUUID;

import javax.annotation.Resource;

import org.javersion.core.Revision;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.store.PersistenceTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.*;
import static org.javersion.path.PropertyPath.ROOT;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PersistenceTestConfiguration.class)
public class VersionGraphCacheTest {

    @Resource
    ObjectVersionStoreJdbc<String, Void> versionStore;

    @Test
    public void load_and_refresh() {
        VersionGraphCache<String, Void> cache = newCache();

        String docId = randomUUID().toString();

        ObjectVersionGraph<Void> versionGraph = cache.load(docId);
        assertThat(versionGraph.isEmpty()).isTrue();

        ObjectVersion<Void> version = ObjectVersion.<Void>builder()
                .changeset(ImmutableMap.of(ROOT.property("property"), "value"))
                .build();
        versionStore.append(docId, ObjectVersionGraph.init(version).getTip());
        versionStore.publish();

        versionGraph = cache.load(docId);
        assertThat(versionGraph.isEmpty()).isFalse();
        assertThat(versionGraph.getTip().getVersion()).isEqualTo(version);

        version = ObjectVersion.<Void>builder()
                .changeset(ImmutableMap.of(ROOT.property("property"), "value2"))
                .build();
        versionStore.append(docId, ObjectVersionGraph.init(version).getTip());
        versionStore.publish();

        versionGraph = cache.load(docId);
        assertThat(versionGraph.getTip().getVersion()).isEqualTo(version);

        cache = newCache();
        versionGraph = cache.load(docId);
        assertThat(versionGraph.getTip().getVersion()).isEqualTo(version);

        assertThat(cache.load(docId)).isSameAs(versionGraph);
    }

    @Test
    public void manual_refresh() {
        VersionGraphCache<String, Void> cache = new VersionGraphCache<>(versionStore,
                // Non-refreshing cache
                CacheBuilder.<String, ObjectVersionGraph<Void>>newBuilder()
                        .maximumSize(8));

        String docId = randomUUID().toString();

        ObjectVersionGraph<Void> versionGraph = cache.load(docId);
        assertThat(versionGraph.isEmpty()).isTrue();

        ObjectVersion<Void> version = ObjectVersion.<Void>builder()
                .changeset(ImmutableMap.of(ROOT.property("property"), "value"))
                .build();
        versionStore.append(docId, ObjectVersionGraph.init(version).getTip());
        versionStore.publish();

        versionGraph = cache.load(docId);
        assertThat(versionGraph.isEmpty()).isTrue();

        cache.refresh(docId);
        versionGraph = cache.load(docId);
        assertThat(versionGraph.isEmpty()).isFalse();
        assertThat(versionGraph.getTip().getVersion()).isEqualTo(version);
    }

    private VersionGraphCache<String, Void> newCache() {
        return new VersionGraphCache<>(versionStore,
                CacheBuilder.<String, ObjectVersionGraph<Void>>newBuilder()
                        .maximumSize(8)
                        .refreshAfterWrite(1, TimeUnit.NANOSECONDS)
        );
    }

}
