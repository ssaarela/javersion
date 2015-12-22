package org.javersion.object.types;

import java.time.Instant;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyTree;

public class InstantType extends AbstractScalarType {

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        return Instant.parse((String) value);
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        context.put(path, object.toString());
    }

    @Override
    public NodeId toNodeId(Object object, WriteContext writeContext) {
        return NodeId.key(object.toString());
    }

    @Override
    public Object fromNodeId(NodeId nodeId, ReadContext context) {
        return Instant.parse(nodeId.getKey());
    }
}
