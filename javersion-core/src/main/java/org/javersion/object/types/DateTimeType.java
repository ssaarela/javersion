package org.javersion.object.types;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

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
    public String toString(Object object) {
        return Long.toString(((DateTime) object).getMillis());
    }

    @Override
    public Object fromString(String str) {
        return new DateTime(Long.valueOf(str));
    }

}
