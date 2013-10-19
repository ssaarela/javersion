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
import org.javersion.reflect.TypeDescriptors;

import com.google.common.collect.Sets;

public abstract class AbstractEntityTypeFactory<V, E extends AbstractEntityType<V>> implements ValueTypeFactory<V> {
    
    protected final TypeDescriptors typeDescriptors;
    
    public AbstractEntityTypeFactory(TypeDescriptors typeDescriptors) {
        this.typeDescriptors = typeDescriptors;
    }

    @Override
    public boolean applies(ValueMappingKey mappingKey) {
        return mappingKey.typeDescriptor.hasAnnotation(Versionable.class);
    }
    
    @Override
    public  E describe(DescribeContext<V> context) {
        PropertyPath path = context.getCurrentPath();
        Set<TypeDescriptor> types = getSubTypes(context.getCurrentType());
        for (TypeDescriptor type : types) {
            for (FieldDescriptor fieldDescriptor : type.getFields().values()) {
                ValueMappingKey mappingKey = new ValueMappingKey(fieldDescriptor, fieldDescriptor.getType());
                context.describe(path.property(fieldDescriptor.getName()), mappingKey);
            }
        }
        return newEntityDescriptor(types);
    }
    
    protected abstract E newEntityDescriptor(Set<TypeDescriptor> types);
    
    protected Set<TypeDescriptor> getSubTypes(TypeDescriptor typeDescriptor) {
        return collectSubTypes(typeDescriptor, Sets.<TypeDescriptor>newHashSet());
    }
    
    private Set<TypeDescriptor> collectSubTypes(TypeDescriptor typeDescriptor, Set<TypeDescriptor> subClasses) {
        subClasses.add(typeDescriptor);
        Versionable versionable = typeDescriptor.getAnnotation(Versionable.class);
        if (versionable != null) {
            for (Class<?> subClass : versionable.subClasses()) {
                collectSubTypes(typeDescriptors.get(subClass), subClasses);
            }
        }
        return subClasses;
    }
    
}