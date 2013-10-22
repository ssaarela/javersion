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

import java.util.Set;

import org.javersion.path.PropertyPath;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractVersionableTypeFactory<V> 
        implements ValueTypeFactory<V> {

    @Override
    public boolean applies(ValueMappingKey mappingKey) {
        return mappingKey.typeDescriptor.hasAnnotation(Versionable.class);
    }
    
    
    @Override
    public  synchronized ValueType<V> describe(DescribeContext<V> context) {
        TypeDescriptor type = context.getCurrentType();
        PropertyPath path = context.getCurrentPath();
        for (FieldDescriptor fieldDescriptor : type.getFields().values()) {
            ValueMappingKey mappingKey = new ValueMappingKey(fieldDescriptor);
            context.describe(path.property(fieldDescriptor.getName()), mappingKey);
        }
        return newEntityType(ImmutableSet.of(type));
    }
    
    protected abstract AbstractObjectType<V> newEntityType(Set<TypeDescriptor> types);
    
}