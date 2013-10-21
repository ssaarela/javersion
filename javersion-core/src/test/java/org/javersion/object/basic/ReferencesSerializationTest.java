package org.javersion.object.basic;

import static org.hamcrest.Matchers.equalTo;
import static org.javersion.object.basic.TestUtil.properties;
import static org.javersion.object.basic.TestUtil.property;
import static org.javersion.path.PropertyPath.ROOT;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.javersion.object.Id;
import org.javersion.object.RootMapping;
import org.javersion.object.Versionable;
import org.javersion.path.PropertyPath;
import org.junit.Test;

public class ReferencesSerializationTest {

    @Versionable
    public static class Node {

        @Id(alias="nodes")
        public final long id;
        
        public Node node;
        
        public Node(long id) {
            this.id = id;
        }
    }
    
    private static final RootMapping<Object> nodeValueMapping = BasicDescribeContext.describe(Node.class);
    
    @Test
    public void Cycle() {
        Node root = new Node(1);
        root.node = new Node(2);
        root.node.node = root;
        
        BasicSerializationContext serializationContext = new BasicSerializationContext(nodeValueMapping);
        serializationContext.serialize(root);
        
        Map<PropertyPath, Object> properties = serializationContext.getProperties();
        
        Map<PropertyPath, Object> expectedProperties = properties(
                ROOT, 1l,

                property("@REF@.nodes[1]"), Node.class,
                property("@REF@.nodes[1].id"), 1l,
                property("@REF@.nodes[1].node"), 2l,
        
                property("@REF@.nodes[2]"), Node.class,
                property("@REF@.nodes[2].id"), 2l,
                property("@REF@.nodes[2].node"), 1l
        );
        
        assertThat(properties, equalTo(expectedProperties));
    }
}
