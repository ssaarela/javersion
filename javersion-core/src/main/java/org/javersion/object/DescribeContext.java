package org.javersion.object;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

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

    public ValueMapping<V> describe(TypeDescriptor typeDescriptor) {
        return describe(new ValueMappingKey(typeDescriptor));
    }
    
    public ValueMapping<V> describe(ValueMappingKey mappingKey) {
        synchronized (lock) {
            ValueMapping<V> valueMapping = mappings.get(mappingKey);
            if (valueMapping == null) {
                stack.addLast(mappingKey);
                try {
                    ValueType<V> valueType = valueTypes.get(mappingKey);
                    valueMapping = new ValueMapping<>(valueType);
                    mappings.put(mappingKey, valueMapping);
                    valueMapping.lock(valueType.describe(this));
                } finally {
                    stack.removeLast();
                }
            }
            return valueMapping;
        }
    }
    
    public ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> getCurrentParent() {
        return stack.getLast().parent;
    }
    
    public TypeDescriptor getCurrentType() {
        return stack.getLast().typeDescriptor;
    }

}
