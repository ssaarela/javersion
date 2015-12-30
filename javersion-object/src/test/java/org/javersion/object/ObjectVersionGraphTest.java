package org.javersion.object;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.object.ObjectVersionGraph.init;

import java.util.Arrays;

import org.javersion.core.Revision;
import org.junit.Test;

public class ObjectVersionGraphTest {
    private ObjectVersion<String> v1 = ObjectVersion.<String>builder().build();

    private ObjectVersion<String> v2 = ObjectVersion.<String>builder(new Revision())
                .parents(v1.revision)
                .build();

    private ObjectVersion<String> v3 = ObjectVersion.<String>builder()
                .parents(v2.revision)
                .build();

    @Test
    public void initializers() {
        ObjectVersionGraph<String> graph = init(v1);
        assertThat(graph.getTip().getVersion()).isEqualTo(v1);

        graph = init(Arrays.asList(v1, v2));
        assertThat(graph.getTip().getVersion()).isEqualTo(v2);

        graph = init(v1, v2, v3);
        assertThat(graph.getTip().getVersion()).isEqualTo(v3);
    }

    @Test
    public void optimize() {
        ObjectVersionGraph<String> graph = init(v1, v2);
        graph = graph.optimize(v2.revision);
        assertThat(graph.versionNodes.size()).isEqualTo(1);
        assertThat(graph.getTip().getRevision()).isEqualTo(v2.revision);
    }
}
