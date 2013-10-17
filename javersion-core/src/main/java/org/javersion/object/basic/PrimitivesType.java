package org.javersion.object.basic;

import java.util.Map;

import org.javersion.object.DescribeContext;
import org.javersion.object.SerializationContext;
import org.javersion.object.ValueMapping;
import org.javersion.object.ValueMappingKey;
import org.javersion.object.ValueType;
import org.javersion.reflect.TypeDescriptor;

import com.google.common.collect.ImmutableMap;

public class PrimitivesType implements ValueType<Object> {

    @Override
    public boolean applies(ValueMappingKey mappingKey) {
        TypeDescriptor typeDescriptor = mappingKey.typeDescriptor;
        return typeDescriptor.isPrimitiveOrWrapper() || typeDescriptor.isSuperTypeOf(String.class);
    }

    @Override
    public Map<String, ValueMapping<Object>> describe(DescribeContext<Object> context) {
        return ImmutableMap.of();
    }

    @Override
    public void serialize(SerializationContext<Object> context) {
        context.put(context.getCurrentObject());
    }

    public String toString() {
        return getClass().getSimpleName();
    }
}
