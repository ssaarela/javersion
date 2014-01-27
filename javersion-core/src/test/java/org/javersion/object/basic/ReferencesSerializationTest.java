package org.javersion.object.basic;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;
import static org.javersion.object.basic.TestUtil.properties;
import static org.javersion.object.basic.TestUtil.property;
import static org.javersion.path.PropertyPath.ROOT;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.javersion.object.IdMapper;
import org.javersion.object.ValueTypes;
import org.javersion.path.PropertyPath;
import org.junit.Test;

public class ReferencesSerializationTest {

    public static class Node {

        public long id;
        
        public Node node;
        
        public Node() {}
        
        public Node(long id) {
            this.id = id;
        }
    }
    
    private ValueTypes<Object> valueTypes = BasicValueTypes.builder()
            .withClass(Node.class)
            .havingAlias("nodes")
            .havingIdMapper(new IdMapper<Node>() {
                @Override
                public String getId(Node object) {
                    return Long.toString(object.id);
                }
            })
            .build();
    
    private final BasicObjectSerializer<Node> nodeSerializer = 
            new BasicObjectSerializer<>(Node.class, valueTypes);
    
    @Test
    public void Cycle() {
        Node root = new Node(1);
        root.node = new Node(2);
        root.node.node = root;
        
        Map<PropertyPath, Object> properties = nodeSerializer.toMap(root);
        
        Map<PropertyPath, Object> expectedProperties = properties(
                ROOT, "1",

                property("@REF@.nodes[1]"), Node.class,
                property("@REF@.nodes[1].id"), 1l,
                property("@REF@.nodes[1].node"), "2",
        
                property("@REF@.nodes[2]"), Node.class,
                property("@REF@.nodes[2].id"), 2l,
                property("@REF@.nodes[2].node"), "1"
        );
        
        assertThat(properties.entrySet(), everyItem(isIn(expectedProperties.entrySet())));
    }
}
