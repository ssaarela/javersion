package org.javersion.object;

import org.javersion.util.Check;

public class BasicValueTypeMapping implements TypeMapping {

    public static BasicValueTypeMapping STRING = new BasicValueTypeMapping(String.class);
    public static BasicValueTypeMapping BYTE = new PrimitiveValueTypeMapping(Byte.class, byte.class);
    public static BasicValueTypeMapping SHORT = new PrimitiveValueTypeMapping(Short.class, short.class);
    public static BasicValueTypeMapping INTEGER = new PrimitiveValueTypeMapping(Integer.class, int.class);
    public static BasicValueTypeMapping LONG = new PrimitiveValueTypeMapping(Long.class, long.class);
    public static BasicValueTypeMapping FLOAT = new PrimitiveValueTypeMapping(Float.class, float.class);
    public static BasicValueTypeMapping DOUBLE = new PrimitiveValueTypeMapping(Double.class, double.class);
    public static BasicValueTypeMapping BOOLEAN = new PrimitiveValueTypeMapping(Boolean.class, boolean.class);
    public static BasicValueTypeMapping CHAR = new PrimitiveValueTypeMapping(Character.class, char.class);

    public final Class<?> type;
    
    public final ValueType valueType;
    
    public BasicValueTypeMapping(Class<?> type) {
        this.type = Check.notNull(type, "type");
        this.valueType = new BasicValueType(type);
    }

    @Override
    public boolean applies(TypeMappingKey mappingKey) {
        return mappingKey.typeDescriptor.getRawType().equals(type);
    }

    @Override
    public ValueType describe(DescribeContext context) {
        return valueType;
    }
    
    public ValueType getValueType() {
        return valueType;
    }

    public static class PrimitiveValueTypeMapping extends BasicValueTypeMapping {
        
        private final Class<?> primitiveType;

        public PrimitiveValueTypeMapping(Class<?> wrapperType, Class<?> primitiveType) {
            super(wrapperType);
            this.primitiveType = primitiveType;
        }

        @Override
        public boolean applies(TypeMappingKey mappingKey) {
            return super.applies(mappingKey) || mappingKey.typeDescriptor.getRawType().equals(primitiveType);
        }

    }
}
