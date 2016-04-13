package org.javersion.store.jdbc;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.path.PropertyPath.parse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import javax.annotation.Resource;

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
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PersistenceTestConfiguration.class)
public abstract class AbstractVersionStoreTest {

    @Resource
    TransactionTemplate transactionTemplate;

    private final long timeSeq = Revision.newUniqueTime();

    protected final Revision
            rev1 = new Revision(timeSeq, 1),
            rev2 = new Revision(timeSeq, 2),
            rev3 = new Revision(timeSeq, 3),
            rev4 = new Revision(timeSeq, 4),
            rev5 = new Revision(timeSeq, 5),
            rev6 = new Revision(timeSeq, 6);

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
        assertThat(store.loadOptimized(docId).versionNodes.get(rev1)).isNull();

        // Load one (loadOptimized)
        addVersions(docId, store, originalGraph.getVersionNode(rev3));
        ObjectVersionGraph<String> loadedGraph = store.loadOptimized(docId);
        // Optimization is reset
        assertThat(loadedGraph.getVersionNode(rev1).getVersion()).isEqualTo(v1);
        assertThat(loadedGraph.getVersionNode(rev2).getVersion()).isEqualTo(v2);
        assertThat(loadedGraph.getVersionNode(rev3).getVersion()).isEqualTo(v3);

        // Batch load
        addVersions(doc2Id, store, ObjectVersionGraph.init(v4).getTip());
        GraphResults<String, String> results = store.load(asList(docId, doc2Id));
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
        assertThat(store.load(docId).versionNodes.size()).isEqualTo(6);

        ObjectVersionGraph<String> versionGraph = store.loadOptimized(docId);
        assertThat(versionGraph.versionNodes.size()).isEqualTo(5);

        VersionNode<PropertyPath, Object, String> versionNode = versionGraph.getVersionNode(rev2);
        assertThat(versionNode.getParentRevisions()).isEqualTo(ImmutableSet.of());
        assertThat(versionNode.getChangeset()).isEqualTo(mapOf(
                "property1", "value1"
                // redundant tombstone ("property2", null) is removed
        ));

        // Keep rev5, rev6 and their LCA rev3
        optimize(docId, v -> v.revision.equals(rev5) || v.revision.equals(rev6), store);

        // Non-optimized load returns still full graph
        assertThat(store.load(docId).versionNodes.size()).isEqualTo(6);

        versionGraph = store.loadOptimized(docId);
        assertThat(versionGraph.versionNodes.size()).isEqualTo(3);

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

        ObjectVersionGraph<String> graph = store.loadOptimized(docId);
        assertThat(graph.versionNodes.containsKey(rev6)).isTrue();
        assertThat(graph.versionNodes.size()).isEqualTo(1);

        store.reset(docId);

        graph = store.loadOptimized(docId);
        assertThat(graph.getVersionNode(rev1).getVersion()).isEqualTo(originalGraph.getVersionNode(rev1).getVersion());
        assertThat(graph.getVersionNode(rev2).getVersion()).isEqualTo(originalGraph.getVersionNode(rev2).getVersion());
        assertThat(graph.getVersionNode(rev3).getVersion()).isEqualTo(originalGraph.getVersionNode(rev3).getVersion());
        assertThat(graph.getVersionNode(rev4).getVersion()).isEqualTo(originalGraph.getVersionNode(rev4).getVersion());
        assertThat(graph.getVersionNode(rev5).getVersion()).isEqualTo(originalGraph.getVersionNode(rev5).getVersion());
        assertThat(graph.getVersionNode(rev6).getVersion()).isEqualTo(originalGraph.getVersionNode(rev6).getVersion());
    }

    @Test
    public void automatic_optimization() {
        AtomicInteger optimizationRuns = new AtomicInteger(0);
        VersionStore<String, String> store = newStore(
                runnable -> {
                    optimizationRuns.incrementAndGet();
                    runnable.run();
                },
                GraphOptions.keepHeadsAndNewest(0, 2));
        final String docId = randomUUID().toString();
        final ObjectVersionGraph<String> originalGraph = graphForOptimization();

        addVersions(docId, store, originalGraph.getVersionNode(rev1), originalGraph.getVersionNode(rev2), originalGraph.getVersionNode(rev3));

        // First time loads full graph and runs optimization in background
        assertThat(store.loadOptimized(docId).versionNodes.size()).isEqualTo(3);
        assertThat(optimizationRuns.get()).isEqualTo(1);

        // Second time returns newly optimized graph and doesn't rerun optimization
        assertThat(store.loadOptimized(docId).versionNodes.size()).isEqualTo(1);
        assertThat(optimizationRuns.get()).isEqualTo(1);

        addVersions(docId, store, originalGraph.getVersionNode(rev4), originalGraph.getVersionNode(rev6));

        // Return updated previous optimization directly and trigger optimization
        assertThat(store.loadOptimized(docId).versionNodes.size()).isEqualTo(3);
        assertThat(optimizationRuns.get()).isEqualTo(2);

        // Return newly optimized
        assertThat(store.loadOptimized(docId).versionNodes.size()).isEqualTo(1);
        assertThat(optimizationRuns.get()).isEqualTo(2);

        // Adding a version referring to squashed parent, returns the full graph and reruns optimization in background
        addVersions(docId, store, originalGraph.getVersionNode(rev5));
        assertThat(store.loadOptimized(docId).versionNodes.size()).isEqualTo(6);
        assertThat(optimizationRuns.get()).isEqualTo(3);
        assertThat(store.loadOptimized(docId).versionNodes.size()).isEqualTo(3);
        assertThat(optimizationRuns.get()).isEqualTo(3);
    }

    protected void optimize(String docId, Predicate<VersionNode<PropertyPath, Object, String>> keep, AbstractVersionStoreJdbc<String, String, ?, ?, ?> store) {
        transactionTemplate.execute(status -> {
            store.updateBatch(docId)
                    .optimize(store.loadOptimized(docId), keep)
                    .execute();
            return null;
        });
    }

    protected void addVersions(String docId, VersionStore<String, String> store, VersionNode<PropertyPath, Object, String>... versions) {
        addVersions(docId, store, asList(versions));
    }

    private void addVersions(String docId, VersionStore<String, String> store, List<VersionNode<PropertyPath, Object, String>> versions) {
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

    protected abstract AbstractVersionStoreJdbc<String, String, ?, ?, ?> newStore(Executor executor, GraphOptions<String, String> graphOptions);

}
