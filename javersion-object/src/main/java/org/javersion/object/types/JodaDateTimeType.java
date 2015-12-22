package org.javersion.object.types;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyTree;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class JodaDateTimeType extends AbstractScalarType {

    private static final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        return fmt.parseDateTime((String) value);
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        context.put(path, fmt.print((DateTime) object));
    }

    @Override
    public NodeId toNodeId(Object object, WriteContext writeContext) {
        return NodeId.key(fmt.print((DateTime) object));
    }

    @Override
    public Object fromNodeId(NodeId nodeId, ReadContext context) {
        return fmt.parseDateTime(nodeId.getKey());
    }
}
