package org.javersion.object.types;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyTree;

public class StringValueType extends AbstractScalarType {

    public static final StringValueType STRING = new StringValueType();

    private StringValueType() {}

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        return (String) value;
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        context.put(path, (String) object);
    }

    @Override
    public Object fromNodeId(NodeId nodeId, ReadContext context) {
        return nodeId.getKey();
    }

    @Override
    public NodeId toNodeId(Object object, WriteContext writeContext) {
        return NodeId.key((String) object);
    }
}
