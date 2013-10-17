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

import java.util.Map;
import java.util.Set;

import org.javersion.path.PropertyPath;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

public abstract class AbstractEntityType<V> implements ValueType<V> {
    
    private final TypeDescriptors typeDescriptors;
    
    public AbstractEntityType(TypeDescriptors typeDescriptors) {
        this.typeDescriptors = typeDescriptors;
    }

    @Override
    public boolean applies(ValueMappingKey mappingKey) {
        return mappingKey.typeDescriptor.hasAnnotation(Versionable.class);
    }
    
    @Override
    public Map<String, ValueMapping<V>> describe(DescribeContext<V> context) {
        ImmutableMap.Builder<String, ValueMapping<V>> children = ImmutableMap.builder();
        for (TypeDescriptor subType : getSubTypes(context.getCurrentType())) {
            for (FieldDescriptor fieldDescriptor : subType.getFields().values()) {
                ValueMappingKey mappingKey = new ValueMappingKey(fieldDescriptor, fieldDescriptor.getType());
                ValueMapping<V> child = context.describe(mappingKey);
                
                children.put(fieldDescriptor.getName(), child);
            }
        }
        return children.build();
    }

    @Override
    public void serialize(SerializationContext<V> context) {
        Object object = context.getCurrentObject();
        PropertyPath path = context.getCurrentPath();
        if (object == null) {
            context.put(path, null);
        } else {
            context.put(path, toValue(object));
            TypeDescriptor typeDescriptor = typeDescriptors.get(object.getClass());
            for (FieldDescriptor fieldDescriptor : typeDescriptor.getFields().values()) {
                Object value = fieldDescriptor.get(object);
                PropertyPath subPath = path.property(fieldDescriptor.getName());
                context.serialize(subPath, value);
            }
        }
    }

    protected abstract V toValue(Object object);
    
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
    
    public String toString() {
        return "EntityType";
    }
}
