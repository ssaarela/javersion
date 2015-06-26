package org.javersion.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.javersion.core.SimpleVersionGraphTest.mapOf;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class VersionGraphOptimizationTest {

    /**
     *  1
     *  2
     *  3
     */
    @Test
    public void squash_linear_history() {
        SimpleVersion v1 = new SimpleVersion.Builder()
                .changeset(mapOf("key", "value1"))
                .meta("v1 meta")
                .build();
        SimpleVersion v2 = new SimpleVersion.Builder()
                .parents(v1.revision)
                .changeset(mapOf("key", "value2"))
                .meta("v2 meta")
                .build();
        SimpleVersion v3 = new SimpleVersion.Builder()
                .parents(v2.revision)
                .changeset(mapOf("key2", "value1"))
                .meta("v3 meta")
                .build();

        SimpleVersionGraph versionGraph = SimpleVersionGraph.init(v1, v2, v3);

        SimpleVersionGraph graph = versionGraph.optimize(v3.revision);

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
        SimpleVersion v1 = new SimpleVersion.Builder()
                .changeset(mapOf("key", "value1"))
                .build();
        SimpleVersion v2 = new SimpleVersion.Builder()
                .parents(v1.revision)
                .changeset(mapOf("key", "value2"))
                .build();
        SimpleVersion v3 = new SimpleVersion.Builder()
                .parents(v2.revision)
                .changeset(mapOf("key2", "value1"))
                .build();
        SimpleVersion v4 = new SimpleVersion.Builder()
                .parents(v2.revision)
                .changeset(mapOf("key3", "value1"))
                .build();

        SimpleVersionGraph versionGraph = SimpleVersionGraph.init(v1, v2, v3, v4);

        SimpleVersionGraph graph = versionGraph.optimize(versionGraph.getHeadRevisions());

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
        SimpleVersion v1 = new SimpleVersion.Builder()
                .changeset(mapOf("key1", "value1"))
                .build();
        SimpleVersion v2 = new SimpleVersion.Builder()
                .parents(v1.revision)
                .changeset(mapOf("key2", "value1"))
                .build();
        SimpleVersion v3 = new SimpleVersion.Builder()
                .parents(v1.revision)
                .changeset(mapOf("key2", "value2"))
                .build();
        SimpleVersion v4 = new SimpleVersion.Builder()
                .parents(v2.revision, v3.revision)
                .changeset(mapOf("key3", "value1"))
                .build();

        SimpleVersionGraph versionGraph = SimpleVersionGraph.init(v1, v2, v3, v4);

        SimpleVersionGraph graph = versionGraph.optimize(versionGraph.getHeadRevisions());

        // Keep only v4
        assertNotFound(graph, v1.revision);
        assertNotFound(graph, v2.revision);
        assertNotFound(graph, v3.revision);

        VersionNode<String, String, String> v4node = graph.getVersionNode(v4.revision);
        assertThat(v4node.getChangeset()).isEqualTo(mapOf("key1", "value1", "key2", "value2", "key3", "value1"));
        assertThat(v4node.getParentRevisions()).isEmpty();
    }

    /**
     *   1
     *   2
     *  3 4
     *   5
     */
    @Test
    public void remove_tombstones() {
        SimpleVersion v1 = new SimpleVersion.Builder()
                .changeset(mapOf("key1", "value1"))
                .changeset(mapOf("key2", "value1"))
                .changeset(mapOf("key3", "value1"))
                .build();
        SimpleVersion v2 = new SimpleVersion.Builder()
                .parents(v1.revision)
                .changeset(mapOf("key1", null))
                .build();
        SimpleVersion v3 = new SimpleVersion.Builder()
                .parents(v2.revision)
                .changeset(mapOf("key2", null))
                .build();
        SimpleVersion v4 = new SimpleVersion.Builder()
                .parents(v2.revision)
                .changeset(mapOf("key3", null))
                .build();
        SimpleVersion v5 = new SimpleVersion.Builder()
                .parents(v3.revision, v4.revision)
                .changeset(mapOf("key4", "value1"))
                .build();

        SimpleVersionGraph versionGraph = SimpleVersionGraph.init(v1, v2, v3, v4, v5);

        SimpleVersionGraph graph = versionGraph.optimize(versionGraph.getHeadRevisions());

        // Keep only v4
        assertNotFound(graph, v1.revision);
        assertNotFound(graph, v2.revision);
        assertNotFound(graph, v3.revision);
        assertNotFound(graph, v4.revision);

        VersionNode<String, String, String> v4node = graph.getVersionNode(v5.revision);
        assertThat(v4node.getChangeset()).isEqualTo(mapOf("key4", "value1"));
        assertThat(v4node.getParentRevisions()).isEmpty();
    }

    private void assertNotFound(SimpleVersionGraph graph, Revision revision) {
        try {
            graph.getVersionNode(revision);
            fail("Revision should not have been found");
        } catch (VersionNotFoundException e) {
            // as expected
        }
    }


}
