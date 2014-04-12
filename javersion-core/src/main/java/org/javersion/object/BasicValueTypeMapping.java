/*
 * Copyright 2013 Samppa Saarela
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
