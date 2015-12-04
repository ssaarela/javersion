package org.javersion.object.types;

import java.util.UUID;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.object.types.AbstractScalarType;
import org.javersion.path.PropertyPath;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyTree;

public class UUIDType extends AbstractScalarType {

    @Override
    public Object fromNodeId(NodeId nodeId, ReadContext context) throws Exception {
        return UUID.fromString(nodeId.getKey());
    }

    @Override
    public NodeId toNodeId(Object object, WriteContext writeContext) {
        return NodeId.valueOf(object.toString());
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        return UUID.fromString((String) value);
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        context.put(path, object.toString());
    }

}
