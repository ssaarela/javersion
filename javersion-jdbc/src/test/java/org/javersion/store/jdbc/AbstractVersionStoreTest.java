package org.javersion.store.jdbc;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.javersion.core.Revision;
import org.javersion.core.VersionNode;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.path.PropertyPath.ROOT;
import static org.javersion.path.PropertyPath.parse;
import static org.javersion.store.jdbc.ExecutorType.ASYNC;
import static org.javersion.store.jdbc.ExecutorType.SYNC;
import static org.javersion.store.jdbc.GraphOptions.keepHeadsAndNewest;
import static org.javersion.store.jdbc.GuavaGraphCache.guavaCacheBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PersistenceTestConfiguration.class)
public abstract class AbstractVersionStoreTest {

    @Resource
    TransactionTemplate transactionTemplate;

    private final long timeSeq = Revision.newUniqueTime();

    protected final Revision
            rev1 = new Revision(1, timeSeq),
            rev2 = new Revision(2, timeSeq),
            rev3 = new Revision(3, timeSeq),
            rev4 = new Revision(4, timeSeq),
            rev5 = new Revision(5, timeSeq),
            rev6 = new Revision(6, timeSeq);

    public static Map<PropertyPath, Object> mapOf(Object... entries) {
        Map<PropertyPath, Object> map = Maps.newHashMap();
        for (int i=0; i+1 < entries.length; i+=2) {
            map.put(parse(entries[i].toString()), entries[i+1]);
        }
        return unmodifiableMap(map);
    }

    @Test
    public void allow_squashed_parent() {
        AbstractVersionStoreJdbc<String, String, ?, ?, ?> store = getStore();
        final String docId = randomUUID().toString();
        final String doc2Id = randomUUID().toString();

        ObjectVersion<String> v1 = ObjectVersion.<String>builder(rev1).changeset(mapOf("property", "value1")).build(),
                v2 = ObjectVersion.<String>builder(rev2).changeset(mapOf("property", null)).parents(rev1).build(),
                v3 = ObjectVersion.<String>builder(rev3).changeset(mapOf("property", "value3")).parents(rev1).build(),
                v4 = ObjectVersion.<String>builder(rev4).build();

        final ObjectVersionGraph<String> originalGraph = ObjectVersionGraph.init(v1, v2, v3);

        addVersions(docId, store, originalGraph.getVersionNode(rev1), originalGraph.getVersionNode(rev2));
        // rev1 is optimized away
        optimize(docId, v -> v.revision.equals(rev2), store);
        assertThat(store.getOptimizedGraph(docId).contains(rev1)).isFalse();

        // Load one (loadOptimized)
        addVersions(docId, store, originalGraph.getVersionNode(rev3));
        ObjectVersionGraph<String> loadedGraph = store.getOptimizedGraph(docId);
        // Optimization is reset
        assertThat(loadedGraph.getVersionNode(rev1).getVersion()).isEqualTo(v1);
        assertThat(loadedGraph.getVersionNode(rev2).getVersion()).isEqualTo(v2);
        assertThat(loadedGraph.getVersionNode(rev3).getVersion()).isEqualTo(v3);

        // Batch load
        addVersions(doc2Id, store, ObjectVersionGraph.init(v4).getTip());
        GraphResults<String, String> results = store.getGraphs(asList(docId, doc2Id));
        assertThat(results.getVersionGraph(docId).getVersionNode(rev1).getVersion()).isEqualTo(v1);
        assertThat(results.getVersionGraph(docId).getVersionNode(rev2).getVersion()).isEqualTo(v2);
        assertThat(results.getVersionGraph(docId).getVersionNode(rev3).getVersion()).isEqualTo(v3);

        assertThat(results.getVersionGraph(doc2Id).getVersionNode(rev4).getVersion()).isEqualTo(v4);
    }

    @Test
    public void optimize_progressively() {
        AbstractVersionStoreJdbc<String, String, ?, ?, ?> store = getStore();
        final String docId = randomUUID().toString();

        ObjectVersionGraph<String> originalGraph = graphForOptimization();
        addVersions(docId, store, ImmutableList.copyOf(originalGraph.getVersionNodes()).reverse());
        store.publish();

        optimize(docId, v -> !v.revision.equals(rev1), store);

        // Non-optimized load returns still full graph
        assertThat(store.getFullGraph(docId).size()).isEqualTo(6);

        ObjectVersionGraph<String> versionGraph = store.getOptimizedGraph(docId);
        assertThat(versionGraph.size()).isEqualTo(5);

        VersionNode<PropertyPath, Object, String> versionNode = versionGraph.getVersionNode(rev2);
        assertThat(versionNode.getParentRevisions()).isEqualTo(ImmutableSet.of());
        assertThat(versionNode.getChangeset()).isEqualTo(mapOf(
                "property1", "value1"
                // redundant tombstone ("property2", null) is removed
        ));

        // Keep rev5, rev6 and their LCA rev3
        optimize(docId, v -> v.revision.equals(rev5) || v.revision.equals(rev6), store);

        // Non-optimized load returns still full graph
        assertThat(store.getFullGraph(docId).size()).isEqualTo(6);

        versionGraph = store.getOptimizedGraph(docId);
        assertThat(versionGraph.size()).isEqualTo(3);

        versionNode = versionGraph.getVersionNode(rev3);
        assertThat(versionNode.getParentRevisions()).isEqualTo(ImmutableSet.of());
        assertThat(versionNode.getChangeset()).isEqualTo(mapOf(
                "property1", "value1"
        ));
        verifyRedundantRelations();
    }

    @Test
    public void reset() {
        final String docId = randomUUID().toString();
        final ObjectVersionGraph<String> originalGraph = graphForOptimization();
        AbstractVersionStoreJdbc<String, String, ?, ?, ?> store = getStore();

        addVersions(docId, store,
                originalGraph.getVersionNode(rev1),
                originalGraph.getVersionNode(rev2),
                originalGraph.getVersionNode(rev3),
                originalGraph.getVersionNode(rev4),
                originalGraph.getVersionNode(rev5),
                originalGraph.getVersionNode(rev6));

        optimize(docId, v -> v.revision.equals(rev6), store);

        ObjectVersionGraph<String> graph = store.getOptimizedGraph(docId);
        assertThat(graph.contains(rev6)).isTrue();
        assertThat(graph.size()).isEqualTo(1);

        store.reset(docId);

        graph = store.getOptimizedGraph(docId);
        assertThat(graph.getVersionNode(rev1).getVersion()).isEqualTo(originalGraph.getVersionNode(rev1).getVersion());
        assertThat(graph.getVersionNode(rev2).getVersion()).isEqualTo(originalGraph.getVersionNode(rev2).getVersion());
        assertThat(graph.getVersionNode(rev3).getVersion()).isEqualTo(originalGraph.getVersionNode(rev3).getVersion());
        assertThat(graph.getVersionNode(rev4).getVersion()).isEqualTo(originalGraph.getVersionNode(rev4).getVersion());
        assertThat(graph.getVersionNode(rev5).getVersion()).isEqualTo(originalGraph.getVersionNode(rev5).getVersion());
        assertThat(graph.getVersionNode(rev6).getVersion()).isEqualTo(originalGraph.getVersionNode(rev6).getVersion());
    }

    @Test
    public void synchronous_publishing() {
        final String docId = randomUUID().toString();
        VersionStore<String, String> store = newStore(getStore().options.toBuilder().publisherType(SYNC).build());
        ObjectVersionGraph<String> graph = ObjectVersionGraph.init(ObjectVersion.<String>builder(rev1).build());
        addVersions(docId, store, graph.getVersionNode(rev1));
        // getGraphs(Collection) returns published documents
        GraphResults<String, String> results = store.getGraphs(asList(docId));
        graph = results.getVersionGraph(docId);
        assertThat(graph.getVersionNode(rev1).getRevision()).isEqualTo(rev1);
    }

    @Test
    public void asynchronous_publishing() throws InterruptedException {
        final String docId = randomUUID().toString();
        AbstractVersionStoreJdbc<String, String, ?, ?, ?> originalStore = getStore();
        StoreOptions<String, String, ?> options = originalStore.options.toBuilder().publisherType(ASYNC).build();

        /*
         * Override doPublish to block publish until it is verified that inserted version is not returned
         */
        CountDownLatch beforePublish = new CountDownLatch(1);
        CountDownLatch afterPublish = new CountDownLatch(1);
        MethodInterceptor interceptor = (Object o, Method method, Object[] args, MethodProxy methodProxy) -> {
            if (method.getName().equals("publish")) {
                beforePublish.await();
                try {
                    return methodProxy.invokeSuper(o, args);
                } finally {
                    afterPublish.countDown();
                }
            }
            return methodProxy.invokeSuper(o, args);
        };

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(originalStore.getClass());
        enhancer.setCallback(interceptor);
        VersionStore<String, String> store = (VersionStore<String, String>) enhancer.create(new Class[] { options.getClass() }, new Object[] { options });

        final ObjectVersionGraph<String> originalGraph = ObjectVersionGraph.init(ObjectVersion.<String>builder(rev1).build());
        transactionTemplate.execute(status -> {
            UpdateBatch<String, String> batch = store.updateBatch(docId);
            batch.addVersion(docId, originalGraph.getVersionNode(rev1));
            batch.execute();
            return null;
        });
        // getGraphs(Collection) should not return version before it's published
        GraphResults<String, String> results = store.getGraphs(asList(docId));
        assertThat(results.isEmpty()).isTrue();

        // Publish and verify that published version is found
        beforePublish.countDown();
        afterPublish.await();
        results = store.getGraphs(asList(docId));
        ObjectVersionGraph<String> graph = results.getVersionGraph(docId);
        assertThat(graph.getVersionNode(rev1).getRevision()).isEqualTo(rev1);
    }

    @Test
    public void automatic_optimization() {
        final AtomicInteger optimizationRuns = new AtomicInteger(0);
        final Executor optimizer = runnable -> {
            optimizationRuns.incrementAndGet();
            runnable.run();
        };
        final GraphOptions<String, String> graphOptions = keepHeadsAndNewest(0, 2);
        final VersionStore<String, String> store = newStore(getStore().options.toBuilder().optimizer(optimizer).graphOptions(graphOptions).build());
        final String docId = randomUUID().toString();
        final ObjectVersionGraph<String> originalGraph = graphForOptimization();

        addVersions(docId, store, originalGraph.getVersionNode(rev1), originalGraph.getVersionNode(rev2), originalGraph.getVersionNode(rev3));

        // First time loads full graph and runs optimization in background
        assertThat(store.getOptimizedGraph(docId).size()).isEqualTo(3);
        assertThat(optimizationRuns.get()).isEqualTo(1);

        // Second time returns newly optimized graph and doesn't rerun optimization
        assertThat(store.getOptimizedGraph(docId).size()).isEqualTo(1);
        assertThat(optimizationRuns.get()).isEqualTo(1);

        addVersions(docId, store, originalGraph.getVersionNode(rev4), originalGraph.getVersionNode(rev6));

        // Return updated previous optimization directly and trigger optimization
        assertThat(store.getOptimizedGraph(docId).size()).isEqualTo(3);
        assertThat(optimizationRuns.get()).isEqualTo(2);

        // Return newly optimized
        assertThat(store.getOptimizedGraph(docId).size()).isEqualTo(1);
        assertThat(optimizationRuns.get()).isEqualTo(2);

        // Adding a version referring to squashed parent, returns the full graph and reruns optimization in background
        addVersions(docId, store, originalGraph.getVersionNode(rev5));
        assertThat(store.getOptimizedGraph(docId).size()).isEqualTo(6);
        assertThat(optimizationRuns.get()).isEqualTo(3);
        assertThat(store.getOptimizedGraph(docId).size()).isEqualTo(3);
        assertThat(optimizationRuns.get()).isEqualTo(3);
    }

    @Test
    public void allow_cglib_proxy() {
        final String docId = "docId";
        final Revision revision = new Revision();
        Function<ObjectVersionGraph<String>, Predicate<VersionNode<PropertyPath, Object, String>>> keep = g -> n -> false;

        Map<String, List<Object>> interceptedCalls = new HashMap<>();
        MethodInterceptor interceptor = (Object o, Method method, Object[] args, MethodProxy methodProxy) -> {
            interceptedCalls.put(method.getName(), ImmutableList.copyOf(args));
            return null;
        };

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(getStore().getClass());
        enhancer.setCallback(interceptor);
        @SuppressWarnings("unchecked")
        VersionStore<String, String> store = (VersionStore<String, String>) enhancer.create();

        store.publish();
        assertThat(interceptedCalls.get("publish")).isEqualTo(asList());

        store.reset(docId);
        assertThat(interceptedCalls.get("reset")).isEqualTo(asList(docId));

        store.getOptimizedGraph(docId);
        assertThat(interceptedCalls.get("getOptimizedGraph")).isEqualTo(asList(docId));

        store.getFullGraph(docId);
        assertThat(interceptedCalls.get("getFullGraph")).isEqualTo(asList(docId));

        store.getGraphs(asList(docId));
        assertThat(interceptedCalls.get("getGraphs")).isEqualTo(asList(asList(docId)));

        store.fetchUpdates(docId, revision);
        assertThat(interceptedCalls.get("fetchUpdates")).isEqualTo(asList(docId, revision));

        store.prune(docId, keep);
        assertThat(interceptedCalls.get("prune")).isEqualTo(asList(docId, keep));

        store.optimize(docId, keep);
        assertThat(interceptedCalls.get("optimize")).isEqualTo(asList(docId, keep));

        store.updateBatch(docId);
        assertThat(interceptedCalls.get("updateBatch")).isEqualTo(asList(docId));

        store.updateBatch(asList(docId));
        assertThat(interceptedCalls.get("updateBatch")).isEqualTo(asList(asList(docId)));
    }

    @Test
    public void auto_refresh_published_values() {
        String docId = randomUUID().toString();

        // Non-refreshing cache
        StoreOptions options = getStore().options.toBuilder()
                .cacheBuilder(guavaCacheBuilder(CacheBuilder.<Object, Object>newBuilder().maximumSize(8))).build();
        AbstractVersionStoreJdbc<String, String, ?, ?, ?> store = newStore(options);

        final ObjectVersionGraph<String> orginalGraph = ObjectVersionGraph.init(
                ObjectVersion.<String>builder(rev1)
                        .changeset(ImmutableMap.of(ROOT.property("property"), "value1"))
                        .build(),
                ObjectVersion.<String>builder(rev2)
                        .parents(rev1)
                        .changeset(ImmutableMap.of(ROOT.property("property"), "value2"))
                        .build(),
                ObjectVersion.<String>builder(rev3)
                        .parents(rev1)
                        .changeset(ImmutableMap.of(ROOT.property("property"), "value3"))
                        .build()
        );

        assertThat(store.getGraph(docId).isEmpty()).isTrue();
        transactionTemplate.execute(status -> {
            store.updateBatch(docId).addVersion(docId, orginalGraph.getVersionNode(rev1)).execute();
            store.updateBatch(docId).addVersion(docId, orginalGraph.getVersionNode(rev2)).execute();
            return null;
        });
        assertThat(store.getGraph(docId).isEmpty()).isTrue();

        store.publish();
        assertThat(store.getGraph(docId).size()).isEqualTo(2);

        // Cached graph is returned after store optimization...
        store.optimize(docId, graph -> node -> node.revision.equals(rev2));
        assertThat(store.getGraph(docId).size()).isEqualTo(2);

        // ...until document is evicted
        store.cache.evict(docId);
        assertThat(store.getGraph(docId).size()).isEqualTo(1);

        // Fallback to full graph
        assertThat(store.getGraph(docId, asList(rev1)).size()).isEqualTo(2);

        // Reset and optimize
        transactionTemplate.execute(status -> {
            store.updateBatch(docId).addVersion(docId, orginalGraph.getVersionNode(rev3)).execute();
            return null;
        });
        store.optimize(docId, graph -> node -> node.revision.equals(rev3));
    }

    protected void optimize(String docId, Predicate<VersionNode<PropertyPath, Object, String>> keep, AbstractVersionStoreJdbc<String, String, ?, ?, ?> store) {
        store.optimize(docId, g -> keep);
    }

    protected void addVersions(String docId, VersionStore<String, String> store, VersionNode<PropertyPath, Object, String>... versions) {
        addVersions(docId, store, asList(versions));
    }

    protected void addVersions(String docId, VersionStore<String, String> store, List<VersionNode<PropertyPath, Object, String>> versions) {
        transactionTemplate.execute(status -> {
            UpdateBatch<String, String> batch = store.updateBatch(docId);
            versions.forEach(v -> batch.addVersion(docId, v));
            batch.execute();
            return null;
        });
        store.publish();
    }

    /**
     *   v1
     *   |
     *   v2
     *   |
     *   v3
     *  /  \
     * v4  v5
     * |
     * v6
     */
    protected ObjectVersionGraph<String> graphForOptimization() {
        ObjectVersion<String> v1 = ObjectVersion.<String>builder(rev1)
                .changeset(mapOf(
                        // This should ve moved to v3
                        "property1", "value1",
                        "property2", "value1"))
                .build();

        ObjectVersion<String> v2 = ObjectVersion.<String>builder(rev2)
                // Toombstones should be removed
                .changeset(mapOf("property2", null))
                .parents(rev1)
                .build();

        ObjectVersion<String> v3 = ObjectVersion.<String>builder(rev3)
                .parents(rev2)
                .build();

        // This intermediate version should be removed
        ObjectVersion<String> v4 = ObjectVersion.<String>builder(rev4)
                .changeset(mapOf(
                        // These should be left as is
                        "property1", "value2",
                        "property2", "value1"))
                .parents(rev3)
                .build();

        ObjectVersion<String> v5 = ObjectVersion.<String>builder(rev5)
                // This should be in conflict with v4
                .changeset(mapOf("property2", "value2"))
                .parents(rev3)
                .build();

        ObjectVersion<String> v6 = ObjectVersion.<String>builder(rev6)
                // This should be replaced with v3
                .parents(rev4)
                .build();

        return ObjectVersionGraph.init(v1, v2, v3, v4, v5, v6);
    }

    protected abstract void verifyRedundantRelations();

    protected abstract AbstractVersionStoreJdbc<String, String, ?, ?, ?> getStore();

    protected abstract AbstractVersionStoreJdbc<String, String, ?, ?, ?> newStore(StoreOptions options);
}
