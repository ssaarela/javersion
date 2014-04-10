package org.javersion.object;

import java.util.Map;

import org.javersion.path.PropertyPath;

public class ObjectSerializer<B> {

    private final RootMapping rootMapping;
    
    public ObjectSerializer(Class<B> clazz) {
        this.rootMapping = DescribeContext.DEFAULT.describe(clazz);
    }
    
    public ObjectSerializer(Class<B> clazz, ValueTypes valueTypes) {
        this.rootMapping = new DescribeContext(valueTypes).describe(clazz);
    }

    public Map<PropertyPath, Object> toMap(B object) {
        return new SerializationContext(rootMapping, object).toMap();
    }

    @SuppressWarnings("unchecked")
    public B fromMap(Map<PropertyPath, Object> properties) {
        return (B) new DeserializationContext(rootMapping, properties).getObject();
    }
}
