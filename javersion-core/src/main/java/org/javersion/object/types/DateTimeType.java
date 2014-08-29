package org.javersion.object.types;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class DateTimeType implements ValueType {

    private final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();


    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        return fmt.parseDateTime(value.toString());
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        context.put(path, fmt.print((DateTime) object));
    }

    @Override
    public boolean isReference() {
        return false;
    }

}
