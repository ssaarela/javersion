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

import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

public class BasicTypeMapping implements TypeMapping {

    public final Class<?> type;
    
    public final ValueType valueType;
    
    public BasicTypeMapping(Class<?> type) {
        this.type = Check.notNull(type, "type");
        this.valueType = new BasicValueType(type);
    }

    @Override
    public boolean applies(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
        TypeDescriptor typeDescriptor = localTypeDescriptor.typeDescriptor;
        return typeDescriptor.getRawType().equals(type);
    }

    @Override
    public ValueType describe(DescribeContext context) {
        return valueType;
    }
    
    public ValueType getValueType() {
        return valueType;
    }

    public static class PrimitiveValueTypeMapping extends BasicTypeMapping {
        
        private final Class<?> primitiveType;

        public PrimitiveValueTypeMapping(Class<?> wrapperType, Class<?> primitiveType) {
            super(wrapperType);
            this.primitiveType = primitiveType;
        }

        @Override
        public boolean applies(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
            TypeDescriptor typeDescriptor = localTypeDescriptor.typeDescriptor;
            return super.applies(path, localTypeDescriptor) || typeDescriptor.getRawType().equals(primitiveType);
        }

    }
    
    public static class StringTypeMapping implements TypeMapping {

        public static final ValueType STRING_TYPE = new BasicValueType(String.class) {
            
            @Override
            public String toString(Object object) {
                return (String) object;
            }
            
        };

        @Override
        public boolean applies(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
            return localTypeDescriptor.typeDescriptor.getRawType().equals(String.class);
        }

        @Override
        public ValueType describe(DescribeContext context) {
            return STRING_TYPE;
        }

    }

}
