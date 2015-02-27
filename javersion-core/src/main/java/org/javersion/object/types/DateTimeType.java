package org.javersion.object.types;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.NodeId;
import org.javersion.path.PropertyTree;
import org.joda.time.DateTime;

public class DateTimeType extends AbstractScalarType {

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        return new DateTime((Long) value);
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        context.put(path, ((DateTime) object).getMillis());
    }

    @Override
    public NodeId toNodeId(Object object) {
        return NodeId.valueOf(((DateTime) object).getMillis());
    }

    @Override
    public Object fromNodeId(NodeId nodeId) {
        return new DateTime(nodeId.getIndex());
    }

}
