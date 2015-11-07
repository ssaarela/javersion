package org.javersion.object.types;

import java.time.LocalDate;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyTree;

public class LocalDateType extends AbstractScalarType {

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        return LocalDate.parse((String) value);
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        context.put(path, object.toString());
    }

    @Override
    public NodeId toNodeId(Object object, WriteContext writeContext) {
        return NodeId.valueOf(object.toString());
    }

    @Override
    public Object fromNodeId(NodeId nodeId, ReadContext context) {
        return LocalDate.parse(nodeId.getKey());
    }
}
