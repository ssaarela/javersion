package org.javersion.core;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.javersion.core.Revision.NODE;
import static org.javersion.core.SimpleVersionGraphTest.mapOf;
import static org.javersion.core.SimpleVersionGraphTest.setOf;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class OptimizedGraphTest {

    /**
     *  1
     *  2
     *  3
     */
    @Test
    public void squash_linear_history() {
        SimpleVersion v1 = new SimpleVersion.Builder(rev(1))
                .changeset(mapOf("key", "value1"))
                .meta("v1 meta")
                .build();
        SimpleVersion v2 = new SimpleVersion.Builder(rev(2))
                .parents(v1.revision)
                .changeset(mapOf("key", "value2"))
                .meta("v2 meta")
                .build();
        SimpleVersion v3 = new SimpleVersion.Builder(rev(3))
                .parents(v2.revision)
                .changeset(mapOf("key2", "value1"))
                .meta("v3 meta")
                .build();

        SimpleVersionGraph versionGraph = SimpleVersionGraph.init(v1, v2, v3);

        assertRevisions(versionGraph, setOf(v3.revision), asList(v3.revision), asList(v2.revision, v1.revision));

        SimpleVersionGraph graph = versionGraph.optimize(v3.revision).getGraph();

        assertNotFound(graph, v2.revision);
        assertNotFound(graph, v1.revision);

        VersionNode<String, String, String> node = graph.getVersionNode(v3.revision);
        assertThat(node.getChangeset()).isEqualTo(mapOf("key", "value2", "key2", "value1"));
        assertThat(node.getParentRevisions()).isEmpty();
        assertThat(node.getMeta()).isEqualTo("v3 meta");
    }

    /**
     *  1
     *  2
     * 3 4
     */
    @Test
    public void y_inheritance() {
        SimpleVersion v1 = new SimpleVersion.Builder(rev(1))
                .changeset(mapOf("key", "value1"))
                .build();
        SimpleVersion v2 = new SimpleVersion.Builder(rev(2))
                .parents(v1.revision)
                .changeset(mapOf("key", "value2"))
                .build();
        SimpleVersion v3 = new SimpleVersion.Builder(rev(3))
                .parents(v2.revision)
                .changeset(mapOf("key2", "value1"))
                .build();
        SimpleVersion v4 = new SimpleVersion.Builder(rev(4))
                .parents(v2.revision)
                .changeset(mapOf("key3", "value1"))
                .build();

        SimpleVersionGraph versionGraph = SimpleVersionGraph.init(v1, v2, v3, v4);

        assertRevisions(versionGraph, setOf(v3.revision, v4.revision), asList(v2.revision, v3.revision, v4.revision), asList(v1.revision));

        SimpleVersionGraph graph = versionGraph.optimize(v3.revision, v4.revision).getGraph();

        // Keep v2, v3 and v4
        assertNotFound(graph, v1.revision);

        VersionNode<String, String, String> v2node = graph.getVersionNode(v2.revision);
        assertThat(v2node.getChangeset()).isEqualTo(mapOf("key", "value2"));
        assertThat(v2node.getParentRevisions()).isEmpty();

        VersionNode<String, String, String> v3node = graph.getVersionNode(v3.revision);
        assertThat(v3node.getChangeset()).isEqualTo(mapOf("key2", "value1"));
        assertThat(v3node.getParentRevisions()).isEqualTo(ImmutableSet.of(v2.revision));

        VersionNode<String, String, String> v4node = graph.getVersionNode(v4.revision);
        assertThat(v4node.getChangeset()).isEqualTo(mapOf("key3", "value1"));
        assertThat(v4node.getParentRevisions()).isEqualTo(ImmutableSet.of(v2.revision));
    }

    /**
     *  1
     * 2 3
     *  4
     */
    @Test
    public void diamond_inheritance() {
        SimpleVersion v1 = new SimpleVersion.Builder(rev(1))
                .changeset(mapOf("key1", "value1"))
                .build();
        SimpleVersion v2 = new SimpleVersion.Builder(rev(2))
                .parents(v1.revision)
                .changeset(mapOf("key2", "value1"))
                .build();
        SimpleVersion v3 = new SimpleVersion.Builder(rev(3))
                .parents(v1.revision)
                .changeset(mapOf("key2", "value2"))
                .build();
        SimpleVersion v4 = new SimpleVersion.Builder(rev(4))
                .parents(v2.revision, v3.revision)
                .changeset(mapOf("key3", "value1"))
                .build();

        SimpleVersionGraph versionGraph = SimpleVersionGraph.init(v1, v2, v3, v4);
        assertRevisions(versionGraph, setOf(v4.revision), asList(v4.revision), asList(v3.revision, v2.revision, v1.revision));
        assertRevisions(versionGraph, setOf(v3.revision, v4.revision), asList(v3.revision, v4.revision), asList(v2.revision, v1.revision));
        assertRevisions(versionGraph, setOf(v1.revision, v4.revision), asList(v1.revision, v4.revision), asList(v3.revision, v2.revision));

        SimpleVersionGraph graph = versionGraph.optimize(v4.revision).getGraph();

        // Keep only v4
        assertNotFound(graph, v1.revision);
        assertNotFound(graph, v2.revision);
        assertNotFound(graph, v3.revision);

        VersionNode<String, String, String> v4node = graph.getVersionNode(v4.revision);
        assertThat(v4node.getChangeset()).isEqualTo(mapOf("key1", "value1", "key2", "value2", "key3", "value1"));
        assertThat(v4node.getParentRevisions()).isEmpty();
    }

    /**
     *    1+
     *   2 \
     *  /| \
     * 3 4*5*
     *  \|/
     *   6*
     */
    @Test
    public void extended_diamond_inheritance() {
        SimpleVersion v1 = new SimpleVersion.Builder(rev(1))
                .build();
        SimpleVersion v2 = new SimpleVersion.Builder(rev(2))
                .parents(v1.revision)
                .build();
        SimpleVersion v3 = new SimpleVersion.Builder(rev(3))
                .parents(v2.revision)
                .build();
        SimpleVersion v4 = new SimpleVersion.Builder(rev(4))
                .parents(v2.revision)
                .build();
        SimpleVersion v5 = new SimpleVersion.Builder(rev(5))
                .parents(v1.revision)
                .build();
        SimpleVersion v6 = new SimpleVersion.Builder(rev(6))
                .parents(v3.revision, v4.revision, v5.revision)
                .build();

        SimpleVersionGraph versionGraph = SimpleVersionGraph.init(v1, v2, v3, v4, v5, v6);
        assertRevisions(versionGraph, setOf(v4.revision, v5.revision, v6.revision),
                asList(v1.revision, v4.revision, v5.revision, v6.revision),
                asList(v3.revision, v2.revision));
    }

    /**
     *   1
     *   2
     *  3 4
     *   5
     */
    @Test
    public void remove_tombstones() {
        SimpleVersion v1 = new SimpleVersion.Builder(rev(1))
                .changeset(mapOf("key1", "value1", "key2", "value1", "key3", "value1"))
                .build();
        SimpleVersion v2 = new SimpleVersion.Builder(rev(2))
                .parents(v1.revision)
                .changeset(mapOf("key1", null))
                .build();
        SimpleVersion v3 = new SimpleVersion.Builder(rev(3))
                .parents(v2.revision)
                .changeset(mapOf("key2", null))
                .build();
        SimpleVersion v4 = new SimpleVersion.Builder(rev(4))
                .parents(v2.revision)
                .changeset(mapOf("key3", null))
                .build();
        SimpleVersion v5 = new SimpleVersion.Builder(rev(5))
                .parents(v3.revision, v4.revision)
                .changeset(mapOf("key4", "value1"))
                .build();

        SimpleVersionGraph versionGraph = SimpleVersionGraph.init(v1, v2, v3, v4, v5);

        assertRevisions(versionGraph, setOf(v5.revision), asList(v5.revision), asList(v4.revision, v3.revision, v2.revision, v1.revision));
        assertRevisions(versionGraph, setOf(v3.revision, v4.revision, v5.revision), asList(v2.revision, v3.revision, v4.revision, v5.revision),
                asList(v1.revision));
        assertRevisions(versionGraph, setOf(v5.revision, v2.revision), asList(v2.revision, v5.revision),
                asList(v4.revision, v3.revision, v1.revision));

        SimpleVersionGraph graph = versionGraph.optimize(v5.revision).getGraph();

        // Keep only v5
        assertNotFound(graph, v1.revision);
        assertNotFound(graph, v2.revision);
        assertNotFound(graph, v3.revision);
        assertNotFound(graph, v4.revision);

        VersionNode<String, String, String> v4node = graph.getVersionNode(v5.revision);
        assertThat(v4node.getChangeset()).isEqualTo(mapOf("key4", "value1"));
        assertThat(v4node.getParentRevisions()).isEmpty();
    }

    /**
     *   v1
     *   |
     *   v2
     *   |
     *   v3+
     *  /  \
     * v4  v5*
     * |
     * v6*
     */
    @Test
    public void mixed_optimizations() {
        SimpleVersion v1 = SimpleVersion.builder()
                .changeset(mapOf(
                        // This should ve moved to v3
                        "property1", "value1",
                        "property2", "value1"))
                .build();

        SimpleVersion v2 = SimpleVersion.builder()
                // Toombstones should be removed
                .changeset(mapOf("property2", null))
                .parents(v1.revision)
                .build();

        SimpleVersion v3 = SimpleVersion.builder()
                .parents(v2.revision)
                .build();

        // This intermediate version should be removed
        SimpleVersion v4 = SimpleVersion.builder()
                .changeset(mapOf(
                        // These should be left as is
                        "property1", "value2",
                        "property2", "value1"))
                .parents(v3.revision)
                .build();

        SimpleVersion v5 = SimpleVersion.builder()
                // This should be in conflict with v4
                .changeset(mapOf("property2", "value2"))
                .parents(v3.revision)
                .build();

        SimpleVersion v6 = SimpleVersion.builder()
                // This should be replaced with v3
                .parents(v4.revision)
                .build();

        SimpleVersionGraph versionGraph = SimpleVersionGraph.init(v1, v2, v3, v4, v5, v6);
        versionGraph = versionGraph.optimize(v5.revision, v6.revision).getGraph();

        VersionNode<String, String, String> versionNode = versionGraph.getVersionNode(v3.revision);
        assertThat(versionNode.getParentRevisions()).isEmpty();
        // Toombstone is removed
        assertThat(versionNode.getChangeset()).isEqualTo(mapOf("property1", "value1"));
        assertThat(versionNode.getProperties()).doesNotContainKey("property2");

        versionNode = versionGraph.getVersionNode(v5.revision);
        assertThat(versionNode.getParentRevisions()).isEqualTo(ImmutableSet.of(v3.revision));
        assertThat(versionNode.getChangeset()).isEqualTo(mapOf("property2", "value2"));

        versionNode = versionGraph.getVersionNode(v6.revision);
        assertThat(versionNode.getParentRevisions()).isEqualTo(ImmutableSet.of(v3.revision));
        assertThat(versionNode.getChangeset()).isEqualTo(mapOf(
                "property1", "value2",
                "property2", "value1"));
    }

    @Test
    public void keep_all() {
        SimpleVersion v1 = new SimpleVersion.Builder()
                .changeset(mapOf("key", "value1"))
                .build();
        SimpleVersion v2 = new SimpleVersion.Builder()
                .parents(v1.revision)
                .changeset(mapOf("key1", "value1"))
                .build();
        SimpleVersion v3 = new SimpleVersion.Builder()
                .parents(v2.revision)
                .changeset(mapOf("key2", "value1"))
                .build();

        SimpleVersionGraph graph = SimpleVersionGraph.init(v1, v2, v3);

        OptimizedGraph<String, String, String, SimpleVersionGraph> optimizedGraph = graph.optimize(v1.revision, v2.revision, v3.revision);
        assertThat(optimizedGraph.getGraph()).isSameAs(graph);
    }

    @Test
    public void keep_tip_always() {
        OptimizedGraph<String, String, String, SimpleVersionGraph> optimizedGraph =
                SimpleVersionGraph.init(
                        new SimpleVersion.Builder()
                                .changeset(mapOf("key", "value1"))
                                .build()).optimize(node -> false);
        assertThat(optimizedGraph.getGraph().size()).isEqualTo(1);
    }

    @Test
    public void optimize_empty_graph() {
        SimpleVersionGraph graph = SimpleVersionGraph.init();
        OptimizedGraph<String, String, String, SimpleVersionGraph> optimizedGraph = graph.optimize(node -> true);
        assertThat(optimizedGraph.getGraph()).isSameAs(graph);
        assertThat(optimizedGraph.getKeptRevisions()).isEmpty();
        assertThat(optimizedGraph.getSquashedRevisions()).isEmpty();
    }

    @Test
    public void performance() {
        int COUNT = 10000;
        Set<Revision> revisions = Sets.newHashSetWithExpectedSize(COUNT);
        SimpleVersionGraph versionGraph = SimpleVersionGraph.init();
        Revision prev = null;
        for (int i=0; i<COUNT;i++) {
            SimpleVersion.Builder versionBuilder = new SimpleVersion.Builder()
                    .changeset(mapOf("key", Integer.toString(i)));
            if (prev != null) {
                versionBuilder.parents(prev);
            }
            versionGraph = versionGraph.commit(versionBuilder.build());
            prev = versionGraph.getTip().getRevision();
            revisions.add(prev);
        }
        System.out.println("begin " + COUNT);

        long time, ts = System.currentTimeMillis();
        SimpleVersionGraph optimizedGraph = versionGraph.optimize(versionGraph.getTip().revision).getGraph();
        time = System.currentTimeMillis() - ts;
        System.out.println("keep tip only: " + time);

        assertThat(optimizedGraph.getTip().getProperties()).isEqualTo(mapOf("key", Integer.toString(COUNT - 1)));
        assertThat(Lists.newArrayList(optimizedGraph.getVersionNodes())).hasSize(1);

        ts = System.currentTimeMillis();
        optimizedGraph = versionGraph.optimize(revisions).getGraph();
        time = System.currentTimeMillis() - ts;
        System.out.println("keep all revisions: " + time);

        assertThat(optimizedGraph.getTip().getProperties()).isEqualTo(mapOf("key", Integer.toString(COUNT - 1)));
        assertThat(Lists.newArrayList(optimizedGraph.getVersionNodes())).hasSize(COUNT);

        System.out.flush();
    }

    private void assertRevisions(SimpleVersionGraph versionGraph, Set<Revision> keepRevisions, List<Revision> keptRevisions, List<Revision> squashedRevisions) {
        OptimizedGraph<String, String, String, SimpleVersionGraph> optimizedGraph = versionGraph.optimize(keepRevisions);
        assertThat(optimizedGraph.getKeptRevisions()).as("keptRevisions").isEqualTo(keptRevisions);
        assertThat(optimizedGraph.getSquashedRevisions()).as("squashedRevisions").isEqualTo(squashedRevisions);
    }

    private void assertNotFound(SimpleVersionGraph graph, Revision revision) {
        try {
            graph.getVersionNode(revision);
            fail("Revision should not have been found");
        } catch (VersionNotFoundException e) {
            // as expected
        }
    }

    private Revision rev(long number) {
        return new Revision(NODE, number);
    }

}
