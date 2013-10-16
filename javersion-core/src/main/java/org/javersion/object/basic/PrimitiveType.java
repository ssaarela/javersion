package org.javersion.object.basic;

import java.util.Map;

import org.javersion.object.ObjectDescriptor;
import org.javersion.object.ObjectDescriptors;
import org.javersion.object.SerializationContext;
import org.javersion.object.ValueType;
import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;

import com.google.common.collect.ImmutableMap;

public class PrimitiveType implements ValueType<Object> {

    @Override
    public void serialize(SerializationContext<Object> context) {
        context.serialize(context.getCurrentObject());
    }

    @Override
    public Map<String, ObjectDescriptor<Object>> describe(
            ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> parent,
            TypeDescriptor typeDescriptor,
            ObjectDescriptors<Object> objectDescriptors) {
        return ImmutableMap.of();
    }

    @Override
    public boolean applies(
            ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> parent,
            TypeDescriptor typeDescriptor) {
        return typeDescriptor.isPrimitiveOrWrapper();
    }

}
