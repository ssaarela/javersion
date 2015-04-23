package org.javersion.object;

import static org.hamcrest.Matchers.isIn;
import static org.javersion.object.TestUtil.properties;
import static org.javersion.object.TestUtil.property;
import static org.javersion.path.PropertyPath.ROOT;
import static org.javersion.reflect.TypeDescriptors.getTypeDescriptor;
import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.javersion.core.Persistent;
import org.javersion.path.PropertyPath;
import org.junit.Test;

public class ReferencesTest {

    public static final Object NODE_ALIAS = Persistent.object(getTypeDescriptor(Node.class).getSimpleName());

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
                ROOT, 1l,

                property("$REF.nodes[1]"), NODE_ALIAS,
                property("$REF.nodes[1].id"), 1l,
                property("$REF.nodes[1].left"), 2l,
                property("$REF.nodes[1].right"), 1l,

                property("$REF.nodes[2]"), NODE_ALIAS,
                property("$REF.nodes[2].id"), 2l,
                property("$REF.nodes[2].left"), 1l,
                property("$REF.nodes[2].right"), 2l
        );

        assertThat(properties).isEqualTo(expectedProperties);

        root = nodeSerializer.fromPropertyMap(properties);
        assertThat(root.id).isEqualTo(1);
        assertThat(root.left.id).isEqualTo(2);
        assertThat(root.right).isSameAs(root);
        assertThat(root.left.left).isSameAs(root);
        assertThat(root.left.right).isSameAs(root.left);
    }

    @Test
    public void Custom_Target_Path() {
        TypeMappings typeMappings = TypeMappings.builder()
                .withClass(Node.class)
                .asReferenceForPath("nodes")
                .build();
        ObjectSerializer<Node> nodeSerializer = new ObjectSerializer<>(Node.class, typeMappings);
        Node node = new Node(1);

        Map<PropertyPath, Object> properties = nodeSerializer.toPropertyMap(node);
        Map<PropertyPath, Object> expectedProperties = properties(
                ROOT, 1l,
                property("nodes[1]"), NODE_ALIAS,
                property("nodes[1].id"), 1l,
                property("nodes[1].left"), null,
                property("nodes[1].right"), null
        );

        assertThat(properties).isEqualTo(expectedProperties);
    }
}
