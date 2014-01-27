package org.javersion.object.basic;

import java.util.Map;

import org.javersion.object.ObjectSerializer;
import org.javersion.object.RootMapping;
import org.javersion.object.ValueTypes;
import org.javersion.path.PropertyPath;

public class BasicObjectSerializer<B> implements ObjectSerializer<B, Object> {
    
    private final RootMapping<Object> rootMapping;
    
    public BasicObjectSerializer(Class<B> clazz) {
        this.rootMapping = BasicDescribeContext.DEFAULT.describe(clazz);
    }
    
    public BasicObjectSerializer(Class<B> clazz, ValueTypes<Object> valueTypes) {
        this.rootMapping = new BasicDescribeContext(valueTypes).describe(clazz);
    }

    @Override
    public Map<PropertyPath, Object> toMap(B object) {
        return new BasicSerializationContext(rootMapping).serialize(object);
    }

    @Override
    public B fromMap(Map<PropertyPath, Object> properties) {
        // TODO Auto-generated method stub
        return null;
    }
}
