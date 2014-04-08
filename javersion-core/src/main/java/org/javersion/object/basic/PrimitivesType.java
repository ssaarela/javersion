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
package org.javersion.object.basic;

import org.javersion.object.*;
import org.javersion.path.PropertyTree;
import org.javersion.reflect.TypeDescriptor;

public class PrimitivesType implements ValueType<Object> {
    
    private static final PrimitivesType INSTANCE = new PrimitivesType();
    
    public static final ValueTypeMapping<Object> FACTORY = new ValueTypeMapping<Object>() {
        
        @Override
        public ValueType<Object> describe(DescribeContext<Object> context) {
            return INSTANCE;
        }
        
        @Override
        public boolean applies(ValueMappingKey mappingKey) {
            TypeDescriptor typeDescriptor = mappingKey.typeDescriptor;
            return typeDescriptor.isPrimitiveOrWrapper() || typeDescriptor.isSuperTypeOf(String.class);
        }
        
    };


    @Override
    public void serialize(Object object, SerializationContext<Object> context) {
        context.put(object);
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, DeserializationContext<Object> context) throws Exception {
        return value;
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, DeserializationContext<Object> context) throws Exception {
    }

    public String toString() {
        return getClass().getSimpleName();
    }

}
