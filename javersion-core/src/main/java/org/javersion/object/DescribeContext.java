package org.javersion.object;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import javax.annotation.Nullable;

import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;

import com.google.common.collect.Maps;

public class DescribeContext<V> {
    
    private final Object lock = new Object();
    
    private final Map<ValueMappingKey, ValueMapping<V>> mappings = Maps.newHashMap();

    private final ValueTypes<V> valueTypes;
    
    private Deque<ValueMappingKey> stack = new ArrayDeque<>();
    
    public DescribeContext(ValueTypes<V> valueTypes) {
        this.valueTypes = valueTypes;
    }

    public ValueMapping<V> describe(
            @Nullable ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> parent, 
            TypeDescriptor typeDescriptor) {
        synchronized (lock) {
            ValueMappingKey key = new ValueMappingKey(parent, typeDescriptor);
            ValueMapping<V> objectDescriptor = mappings.get(key);
            if (objectDescriptor == null) {
                objectDescriptor = describe(key);
            }
            return objectDescriptor;
        }
    }
    
    public ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> getCurrentParent() {
        return stack.getLast().parent;
    }
    
    public TypeDescriptor getCurrentType() {
        return stack.getLast().typeDescriptor;
    }

    private ValueMapping<V> describe(ValueMappingKey mappingKey) {
        stack.addLast(mappingKey);
        try {
            ValueType<V> valueType = valueTypes.get(mappingKey);
            Map<String, ValueMapping<V>> children = valueType.describe(this);
            ValueMapping<V> objectDescriptor = new ValueMapping<V>(valueType, children);
            mappings.put(mappingKey, objectDescriptor);
            return objectDescriptor;
        } finally {
            stack.removeLast();
        }
    }
    
}
