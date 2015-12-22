package org.javersion.path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.path.NodeId.ANY_KEY;
import static org.javersion.path.NodeId.ANY_PROPERTY;
import static org.javersion.path.NodeId.ROOT_ID;
import static org.javersion.path.NodeId.index;
import static org.javersion.path.NodeId.key;
import static org.javersion.path.NodeId.property;
import static org.javersion.path.PropertyPath.ROOT;

import org.javersion.path.NodeId.IndexId;
import org.javersion.path.NodeId.KeyId;
import org.javersion.path.NodeId.PropertyId;
import org.junit.Test;

public class NodeIdTest {

    @Test
    public void root_node_toPath_returns_parent() {
        assertThat((Object) ROOT.node(ROOT_ID)).isEqualTo(ROOT);
    }

    @Test
    public void is_index() {
        assertThat(index(1).isIndex()).isTrue();

        assertThat(key("key").isIndex()).isFalse();
        assertThat(ROOT_ID.isIndex()).isFalse();
    }

    @Test
    public void is_key() {
        assertThat(key("key").isKey()).isTrue();

        assertThat(index(1).isKey()).isFalse();
        assertThat(ROOT_ID.isKey()).isFalse();
    }

    @Test
    public void cached_indexes() {
        assertThat(index(1)).isSameAs(index(1));
        assertThat(index(123456789)).isNotSameAs(index(123456789));
    }

    @Test
    public void hash_codes() {
        assertNotEqual(index(1), index(2));
        assertNotEqual(key("a"), key("A"));
        assertNotEqual(property("a"), property("A"));
        assertNotEqual(ANY_KEY, ANY_PROPERTY);
    }

    @Test
    public void node_types() {
        IndexId index = index(1);
        KeyId key = key("1");
        PropertyId property = property("1"); // illegal property, but not validated here

        assertNotEqual(index, key);
        assertNotEqual(key, index);

        assertNotEqual(property, key);
        assertNotEqual(key, property);

        assertNotEqual(property, index);
        assertNotEqual(index, property);
    }

    private void assertNotEqual(NodeId a, NodeId b) {
        assertThat(a).isNotEqualTo(b);
        assertThat(a).isNotEqualByComparingTo(b);
        assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
    }
}
