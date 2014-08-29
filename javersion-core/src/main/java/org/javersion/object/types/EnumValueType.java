package org.javersion.object.types;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class EnumValueType implements ValueType {

    private final Class<Enum> enumType;

    public EnumValueType(Class<Enum> enumType) {
        this.enumType = enumType;
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        return Enum.valueOf(enumType, value.toString());
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        context.put(path, object.toString());
    }

    @Override
    public boolean isReference() {
        return false;
    }

}
