package org.javersion.object;

import org.javersion.path.PropertyTree;

public class BasicValueType implements ValueType {

    private final Class<?> valueType;
    
    public BasicValueType(Class<?> valueType) {
        this.valueType = valueType;
    }
    
    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, DeserializationContext context) throws Exception {
        return value;
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, DeserializationContext context) throws Exception {}

    @Override
    public void serialize(Object object, SerializationContext context) {
        context.put(object);
    }

    @Override
    public Class<?> getValueType() {
        return valueType;
    }

}
