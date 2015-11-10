package org.javersion.object;

import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.*;

import org.javersion.object.ReferencesTest.Node;
import org.javersion.path.PropertyPath;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class SetTest {

    @Versionable
    public static class NodeSet {
        private Set<Node> nodes = Sets.newLinkedHashSet();
    }

    public static class NodeExt extends Node {
        Set<NodeExt> nodes = Sets.newLinkedHashSet();
        SortedSet<String> sorted = new TreeSet<>();
    }

    @Versionable
    public static class DoubleSet {
        Set<Double> doubles = new HashSet<>();
        Set<Float> floats = new HashSet<>();
    }

    public static TypeMappings typeMappings = TypeMappings.builder()
            .withClass(Node.class)
            .havingSubClasses(NodeExt.class)
            .asReferenceForPath("nodes")
            .build();

    private final ObjectSerializer<NodeSet> nodeSetSerializer = new ObjectSerializer<>(NodeSet.class, typeMappings);

    private final ObjectSerializer<NodeExt> nodeExtSerializer = new ObjectSerializer<>(NodeExt.class, typeMappings);

    private final ObjectSerializer<DoubleSet> doubleSetSerializer = new ObjectSerializer<>(DoubleSet.class, typeMappings);

    @Test
    public void Write_And_Read_NodeSet() {
        NodeSet nodeSet = new NodeSet();
        Node node1 = new Node(123);
        Node node2 = new Node(456);
        node1.left = node1;
        node1.right = node2;
        node2.left = node1;
        node2.right = node2;

        nodeSet.nodes.add(node1);
        nodeSet.nodes.add(node2);

        Map<PropertyPath, Object> map = nodeSetSerializer.toPropertyMap(nodeSet);

        nodeSet = nodeSetSerializer.fromPropertyMap(map);
        assertThat(nodeSet.nodes, hasSize(2));
        Iterator<Node> iter = nodeSet.nodes.iterator();
        node1 = iter.next();
        node2 = iter.next();
        if (node1.id != 123) {
            Node tmp = node2;
            node2 = node1;
            node1 = tmp;
        }
        assertThat(node1.id, equalTo(123));
        assertThat(node2.id, equalTo(456));
        assertThat(node1.left, sameInstance(node1));
        assertThat(node1.right, sameInstance(node2));
        assertThat(node2.left, sameInstance(node1));
        assertThat(node2.right, sameInstance(node2));
    }

    @Test
    public void NodeExt_Containing_Itself_In_a_Set() {
        NodeExt nodeExt = new NodeExt();
        nodeExt.id = 789;
        nodeExt.nodes.add(nodeExt);
        nodeExt.sorted.add("omega");
        nodeExt.sorted.add("alpha");
        nodeExt.sorted.add("beta");

        Map<PropertyPath, Object> map = nodeExtSerializer.toPropertyMap(nodeExt);

        nodeExt = nodeExtSerializer.fromPropertyMap(map);
        assertThat(nodeExt.id, equalTo(789));
        assertThat(nodeExt.nodes, equalTo(singleton(nodeExt)));

        Iterator<String> iter = nodeExt.sorted.iterator();
        assertThat(iter.next(), equalTo("alpha"));
        assertThat(iter.next(), equalTo("beta"));
        assertThat(iter.next(), equalTo("omega"));
    }

    @Test
    public void double_set() {
        Set<Double> doubles = ImmutableSet.of(
                Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                1.1
        );
        Set<Float> floats = ImmutableSet.of(
                Float.NaN,
                Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY,
                (float) 1.1
        );
        DoubleSet dset = new DoubleSet();
        dset.doubles = doubles;
        dset.floats = floats;

        dset = doubleSetSerializer.fromPropertyMap(doubleSetSerializer.toPropertyMap(dset));

        assertThat(dset.doubles, equalTo(doubles));
        assertThat(dset.floats, equalTo(floats));
    }
}
