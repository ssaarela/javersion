package org.javersion.object;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.sameInstance;
import static org.javersion.object.TestUtil.properties;
import static org.javersion.object.TestUtil.property;
import static org.javersion.path.PropertyPath.ROOT;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.javersion.path.PropertyPath;
import org.junit.Test;

public class ReferencesTest {

    public static class Node {

        @Id public Integer id;

        public Node left;

        public Node right;

        public Node() {}

        public Node(int id) {
            this.id = id;
        }
        public int hashCode() {
            if (id == null) throw new IllegalStateException("id is null");
            return id;
        }
        public boolean equals(Object obj) {
            if (id == null) throw new IllegalStateException("id is null");
            if (obj == this) {
                return true;
            } else if (obj instanceof Node) {
                Node other = (Node) obj;
                return this.id  == other.id;
            } else {
                return false;
            }
        }
    }

    public static TypeMappings typeMappings = TypeMappings.builder()
            .withClass(Node.class)
            .asReferenceWithAlias("nodes")
            .build();

    private final ObjectSerializer<Node> nodeSerializer = new ObjectSerializer<>(Node.class, typeMappings);

    @Test
    public void Node_Cycles_Read_And_Write() {
        Node root = new Node(1);
        root.left = new Node(2);
        root.right = root;
        root.left.left = root;
        root.left.right = root.left;

        Map<PropertyPath, Object> properties = nodeSerializer.toPropertyMap(root);

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

        root = nodeSerializer.fromPropertyMap(properties);
        assertThat(root.id, equalTo(1));
        assertThat(root.left.id, equalTo(2));
        assertThat(root.right, sameInstance(root));
        assertThat(root.left.left, sameInstance(root));
        assertThat(root.left.right, sameInstance(root.left));
    }
}
