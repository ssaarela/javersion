package org.javersion.object.basic;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.sameInstance;
import static org.javersion.object.basic.TestUtil.properties;
import static org.javersion.object.basic.TestUtil.property;
import static org.javersion.path.PropertyPath.ROOT;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.javersion.object.IdMapper;
import org.javersion.object.ObjectSerializer;
import org.javersion.object.ValueTypes;
import org.javersion.path.PropertyPath;
import org.junit.Test;

public class ReferencesSerializationTest {

    public static class Node {

        public int id;
        
        public Node left;

        public Node right;

        public Node() {}
        
        public Node(int id) {
            this.id = id;
        }
    }
    
    private ValueTypes valueTypes = ValueTypes.builder()
            .withClass(Node.class)
            .havingAlias("nodes")
            .havingIdMapper(new IdMapper<Node>() {
                @Override
                public String getId(Node object) {
                    return Long.toString(object.id);
                }
            })
            .build();
    
    private final ObjectSerializer<Node> nodeSerializer = new ObjectSerializer<>(Node.class, valueTypes);
    
    @Test
    public void Cycles() {
        Node root = new Node(1);
        root.left = new Node(2);
        root.right = root;
        root.left.left = root;
        root.left.right = root.left;
        
        Map<PropertyPath, Object> properties = nodeSerializer.toMap(root);
        
        Map<PropertyPath, Object> expectedProperties = properties(
                ROOT, "1",

                property("@REF@.nodes[1]"), Node.class,
                property("@REF@.nodes[1].id"), 1,
                property("@REF@.nodes[1].left"), "2",
                property("@REF@.nodes[1].right"), "1",
        
                property("@REF@.nodes[2]"), Node.class,
                property("@REF@.nodes[2].id"), 2,
                property("@REF@.nodes[2].left"), "1",
                property("@REF@.nodes[2].right"), "2"
        );
        
        assertThat(properties.entrySet(), everyItem(isIn(expectedProperties.entrySet())));
        
        root = nodeSerializer.fromMap(properties);
        assertThat(root.id, equalTo(1));
        assertThat(root.left.id, equalTo(2));
        assertThat(root.right, sameInstance(root));
        assertThat(root.left.left, sameInstance(root));
        assertThat(root.left.right, sameInstance(root.left));
    }
}
