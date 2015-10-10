package org.javersion.store.jdbc;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.path.PropertyPath.ROOT;
import static org.javersion.store.sql.QTestVersion.testVersion;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.javersion.core.Revision;
import org.javersion.core.VersionNode;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.javersion.store.PersistenceTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.mysema.query.sql.SQLQueryFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PersistenceTestConfiguration.class)
public class VersionGraphCacheTest {

    @Resource
    ObjectVersionStoreJdbc<String, Void> versionStore;

    @Resource
    SQLQueryFactory queryFactory;

    @Test
    public void load_and_refresh() {
        VersionGraphCache<String, Void> cache = newRefreshingCache();

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

        cache = newRefreshingCache();
        versionGraph = cache.load(docId);
        assertThat(versionGraph.getTip().getVersion()).isEqualTo(version);

        assertThat(cache.load(docId)).isSameAs(versionGraph);
    }

    @Test
    public void manual_refresh() {
        String docId = randomUUID().toString();
        VersionGraphCache<String, Void> cache = newNonRefreshingCache();


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

    @Test
    public void auto_refresh_published_values() {
        String docId = randomUUID().toString();
        VersionGraphCache<String, Void> cache = newNonRefreshingCache();
        ObjectVersionGraph<Void> versionGraph = cache.load(docId);
        assertThat(versionGraph.isEmpty()).isTrue();

        ObjectVersion<Void> version = ObjectVersion.<Void>builder()
                .changeset(ImmutableMap.of(ROOT.property("property"), "value"))
                .build();
        versionStore.append(docId, ObjectVersionGraph.init(version).getTip());
        assertThat(cache.publish()).isEqualTo(ImmutableSet.of(docId));

        versionGraph = cache.load(docId);
        assertThat(versionGraph.isEmpty()).isFalse();
        assertThat(versionGraph.getTip().getVersion()).isEqualTo(version);
    }

    @Test
    public void clear_cache() throws InterruptedException {
        String docId = randomUUID().toString();
        VersionGraphCache<String, Void> cache = newNonRefreshingCache();

        ObjectVersion<Void> version = ObjectVersion.<Void>builder().build(); // empty version
        versionStore.append(docId, ObjectVersionGraph.init(version).getTip());
        cache.publish();

        assertThat(cache.load(docId).isEmpty()).isEqualTo(false);

        queryFactory.delete(testVersion).where(testVersion.revision.eq(version.revision)).execute();

        // Does not hit database
        assertThat(cache.load(docId).isEmpty()).isEqualTo(false);

        cache.evictAll();

        assertThat(cache.load(docId).isEmpty()).isEqualTo(true);
    }

    @Test
    public void return_empty_if_fetch_fails() throws InterruptedException {
        String docId = randomUUID().toString();
        VersionGraphCache<String, Void> cache = newRefreshingCache(1); // 100 ms

        ObjectVersion<Void> version = ObjectVersion.<Void>builder().build(); // empty version
        versionStore.append(docId, ObjectVersionGraph.init(version).getTip());
        cache.publish();

        assertThat(cache.load(docId).isEmpty()).isEqualTo(false);

        queryFactory.delete(testVersion).where(testVersion.revision.eq(version.revision)).execute();

        Thread.sleep(1);

        assertThat(cache.load(docId).isEmpty()).isEqualTo(true);
    }

    @Test
    public void auto_refresh_only_cached_graphs() {
        final MutableBoolean cacheRefreshed = new MutableBoolean(false);
        ObjectVersionStoreJdbc<String, Void> proxyStore = new ObjectVersionStoreJdbc<String, Void>() {
            @Override
            public void append(String docId, VersionNode<PropertyPath, Object, Void> version) {
                versionStore.append(docId, version);
            }
            @Override
            public Multimap<String, Revision> publish() { return versionStore.publish(); }
            @Override
            public ObjectVersionGraph<Void> load(String docId) {
                cacheRefreshed.setTrue();
                throw new RuntimeException("Should not refresh!");
            }
        };

        String docId = randomUUID().toString();
        VersionGraphCache<String, Void> cache = new VersionGraphCache<String, Void>(proxyStore,
                // Non-refreshing cache
                CacheBuilder.<String, ObjectVersionGraph<Void>>newBuilder()
                        .maximumSize(8));

        ObjectVersion<Void> version = ObjectVersion.<Void>builder()
                .changeset(ImmutableMap.of(ROOT.property("property"), "value"))
                .build();
        proxyStore.append(docId, ObjectVersionGraph.init(version).getTip());

        // This should not refresh cache as docId is not cached!
        cache.publish();
        assertThat(cacheRefreshed.getValue()).isFalse();
    }

    private VersionGraphCache<String, Void> newRefreshingCache() {
        return newRefreshingCache(1);
    }

    private VersionGraphCache<String, Void> newRefreshingCache(long refreshAfterNanos) {
        return new VersionGraphCache<>(versionStore,
                CacheBuilder.<String, ObjectVersionGraph<Void>>newBuilder()
                        .maximumSize(8)
                        .refreshAfterWrite(refreshAfterNanos, TimeUnit.NANOSECONDS)
        );
    }

    private VersionGraphCache<String, Void> newNonRefreshingCache() {
        return new VersionGraphCache<>(versionStore,
                // Non-refreshing cache
                CacheBuilder.<String, ObjectVersionGraph<Void>>newBuilder()
                        .maximumSize(8));
    }
}
