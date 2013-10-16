package org.javersion.object;

import javax.annotation.Nullable;

import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;

public class ValueTypes<V> {

    private final Iterable<ValueType<V>> types;
    
    public ValueTypes(Iterable<ValueType<V>> types) {
        this.types = types;
    }

    public ValueType<V> get(
            @Nullable ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> parent, 
            TypeDescriptor typeDescriptor) {
        for (ValueType<V> valueType : types) {
            if (valueType.applies(parent, typeDescriptor)) {
                return valueType;
            }
        }
        throw new IllegalArgumentException("ValueType not found for " + typeDescriptor);
    }
    
}
