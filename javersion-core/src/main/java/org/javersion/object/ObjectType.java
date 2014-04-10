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

import static com.google.common.collect.Maps.uniqueIndex;

import java.util.Map;
import java.util.Set;

import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableSet;


public class ObjectType<O> implements ValueType {

    protected final Map<Class<?>, TypeDescriptor> types;
    
    protected final Class<? extends O> rootType;
    
    @SuppressWarnings("unchecked")
    public ObjectType(TypeDescriptor type) {
        this((Class<? extends O>) type.getRawType(), ImmutableSet.of(type));
    }
    
    public ObjectType(Class<? extends O> rootType, Set<TypeDescriptor> types) {
        Check.notNullOrEmpty(types, "types");
        this.rootType = rootType;
        this.types = uniqueIndex(types, TypeDescriptor.getRawType);
    }
    
    public Object instantiate(PropertyTree propertyTree, Object value, DeserializationContext context) throws Exception {
        if (value == null) {
            return null;
        } else {
            return fromValue(value);
        }
    }
    
    public void bind(PropertyTree propertyTree, Object object, DeserializationContext context) throws Exception {
        TypeDescriptor typeDescriptor = types.get(object.getClass());
        for (PropertyTree child : propertyTree.getChildren()) {
            FieldDescriptor fieldDescriptor = typeDescriptor.getField(child.getName());
            Object value = context.getObject(child);
            fieldDescriptor.set(object, value);
        }
    }

    @Override
    public void serialize(Object object, SerializationContext context) {
        PropertyPath path = context.getCurrentPath();
        if (object == null) {
            context.put(path, null);
        } else {
            context.put(path, toValue(object));
            TypeDescriptor typeDescriptor = types.get(object.getClass());
            for (FieldDescriptor fieldDescriptor : typeDescriptor.getFields().values()) {
                Object value = fieldDescriptor.get(object);
                PropertyPath subPath = path.property(fieldDescriptor.getName());
                context.serialize(subPath, value);
            }
        }
    }

    public Object toValue(Object object) {
        return object.getClass();
    }

    protected Object fromValue(Object value) throws Exception {
        return ((Class<?>) value).newInstance();
    }
    
    public String toString() {
        return "EntityType of " + types.values();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Class<? extends O>> getValueType() {
        return (Class<Class<? extends O>>) rootType.getClass();
    }

}
