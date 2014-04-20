package org.javersion.object;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.javersion.object.ReferencesTest.Node;
import org.javersion.path.PropertyPath;
import org.junit.Test;

import com.google.common.collect.Sets;

public class SetTest {

    @Versionable
    public static class NodeSet {
        private Set<Node> nodes = Sets.newLinkedHashSet();
    }
    
    private final ObjectSerializer<NodeSet> serializer = new ObjectSerializer<>(NodeSet.class, ReferencesTest.valueTypes);
    
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

        Map<PropertyPath, Object> map = serializer.write(nodeSet);
        
        nodeSet = serializer.read(map);
        assertThat(nodeSet.nodes, hasSize(2));
        Iterator<Node> iter = nodeSet.nodes.iterator();
        node1 = iter.next();
        node2 = iter.next();
        assertThat(node1.id, equalTo(123));
        assertThat(node2.id, equalTo(456));
        assertThat(node1.left, sameInstance(node1));
        assertThat(node1.right, sameInstance(node2));
        assertThat(node2.left, sameInstance(node1));
        assertThat(node2.right, sameInstance(node2));
    }
}
