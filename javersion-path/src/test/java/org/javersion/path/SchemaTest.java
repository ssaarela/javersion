package org.javersion.path;

import static org.javersion.path.PropertyPath.NodeId.valueOf;
import static org.javersion.path.PropertyPath.ROOT;
import static org.assertj.core.api.Assertions.*;
import static org.javersion.path.PropertyPath.parse;

import org.javersion.path.PropertyPath.NodeId;
import org.javersion.path.Schema.Builder;
import org.junit.Test;

public class SchemaTest {

    @Test
    public void anything_goes() {
        Builder<String> root = new Builder<>("root");
        root.connect(ROOT.any(), root);
        assertThat(root.getValue()).isEqualTo("root");

        Schema<String> schema = root.build();
        assertThat(schema.getValue()).isEqualTo("root");
        assertThat(schema.getChild(NodeId.ANY)).isEqualTo(schema);
        assertThat(schema.find(ROOT)).isEqualTo(schema);
        assertThat(schema.find(ROOT.key("foo").property("bar").index(123))).isEqualTo(schema);
    }

    @Test
    public void list_of_named_objects() {
        Builder<String> root = new Builder<>("root");
        Builder<String> list = root.addChild(valueOf("list"), new Builder<>());
        list.getOrCreate(parse("[].name"), "name");
        root.getOrCreate(parse("list[]")).setValue("element");

        Schema<String> schema = root.build();
        assertThat(schema.getValue()).isEqualTo("root");
        Schema<String> child = schema.get(parse("list"));
        assertThat(child.getValue()).isNull();
        child = child.getChild(NodeId.ANY_INDEX);
        assertThat(child.getValue()).isEqualTo("element");
        assertThat(child.get(parse("name")).getValue()).isEqualTo("name");
    }

    @Test
    public void has_child() {
        Builder<String> root = new Builder<>("root");
        root.getOrCreate(ROOT.property("property"));
        Schema<String> schema = root.build();

        assertThat(schema.hasChildren()).isTrue();
        assertThat(schema.hasChild(valueOf("property"))).isTrue();
        assertThat(schema.hasChild(NodeId.ANY_KEY)).isFalse();

        schema = schema.getChild(valueOf("property"));
        assertThat(schema.hasChildren()).isFalse();
        assertThat(schema.hasChild(NodeId.ANY)).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void node_not_found() {
        new Builder<>("root").get(ROOT.property("foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void get_by_null_throws_exception() {
        new Builder<>().get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_get_child_by_null_throws_exception() {
        new Builder<>().getChild(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void schema_get_child_by_null_throws_exception() {
        new Builder<>().build().getChild(null);
    }
}
