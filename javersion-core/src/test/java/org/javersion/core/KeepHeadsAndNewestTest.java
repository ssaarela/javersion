package org.javersion.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class KeepHeadsAndNewestTest {

    @Test
    public void keep_only_heads() {
        Revision rev1 = new Revision(), rev2 = new Revision();

        SimpleVersionGraph graph = SimpleVersionGraph.init(
                SimpleVersion.builder(rev1).build(),
                SimpleVersion.builder(rev2).parents(rev1).build());

        KeepHeadsAndNewest<String, String, String> keep = new KeepHeadsAndNewest<>(graph, 0);
        assertThat(keep.test(graph.getVersionNode(rev2))).isTrue();
        assertThat(keep.test(graph.getVersionNode(rev1))).isFalse();
    }

    @Test
    public void keep_heads_and_newest() {
        Revision rev1 = new Revision(), rev2 = new Revision(), rev3 = new Revision(), rev4 = new Revision();

        SimpleVersionGraph graph = SimpleVersionGraph.init(
                SimpleVersion.builder(rev1).build(),
                SimpleVersion.builder(rev2).parents(rev1).build(),
                SimpleVersion.builder(rev3).parents(rev2).build(),
                SimpleVersion.builder(rev4).parents(rev2).build());

        KeepHeadsAndNewest<String, String, String> keep = new KeepHeadsAndNewest<>(graph, 1);
        assertThat(keep.test(graph.getVersionNode(rev4))).isTrue();
        assertThat(keep.test(graph.getVersionNode(rev3))).isTrue();
        assertThat(keep.test(graph.getVersionNode(rev2))).isTrue();
        assertThat(keep.test(graph.getVersionNode(rev1))).isFalse();
    }

}
