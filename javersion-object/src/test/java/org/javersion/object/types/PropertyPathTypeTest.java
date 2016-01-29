package org.javersion.object.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.path.PropertyPath.ROOT;

import java.util.Map;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyPath;
import org.javersion.path.Schema;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class PropertyPathTypeTest {

    private final PropertyPathType type = new PropertyPathType();

    private final PropertyPath path = PropertyPath.parse("some.property");

    @Test
    public void toNodeId() {
        NodeId nodeId = type.toNodeId(path, null);
        assertThat(nodeId).isEqualTo(NodeId.key(path.toString()));
    }

    @Test
    public void fromNodeId() {
        PropertyPath parsed = (PropertyPath) type.fromNodeId(NodeId.key(path.toString()), null);
        assertThat((Object) parsed).isEqualTo(path);
    }

    @Test
    public void read_write() {
        Schema<ValueType> schema = Schema.<ValueType>builder().setValue(type).build();
        Map<PropertyPath, Object> properties = new WriteContext(schema, path).getMap();
        assertThat(properties).isEqualTo(ImmutableMap.of(
                ROOT, "some.property"
        ));
        PropertyPath parsed = (PropertyPath) new ReadContext(schema, properties).getObject();
        assertThat((Object) parsed).isEqualTo(path);
    }
}
