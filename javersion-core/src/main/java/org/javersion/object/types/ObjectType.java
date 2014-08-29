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
package org.javersion.object.types;

import static com.google.common.collect.Maps.uniqueIndex;

import java.util.Map;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
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
    
    public ObjectType(Class<? extends O> rootType, Iterable<TypeDescriptor> types) {
        Check.notNullOrEmpty(types, "types");
        this.rootType = rootType;
        this.types = uniqueIndex(types, TypeDescriptor.getRawType);
    }
    
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        Class<?> type = (Class<?>) value;
        TypeDescriptor typeDescriptor = Check.notNull$(types.get(value), "Unsupported type: %s", type);
        return typeDescriptor.newInstance();
    }
    
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
        TypeDescriptor typeDescriptor = types.get(object.getClass());
        for (PropertyTree child : propertyTree.getChildren()) {
            String fieldName = child.getName();
            if (typeDescriptor.hasField(fieldName)) {
                FieldDescriptor fieldDescriptor = typeDescriptor.getField(fieldName);
                Object value = context.getObject(child);
                fieldDescriptor.set(object, value);
            }
        }
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        context.put(path, object.getClass());
        TypeDescriptor typeDescriptor = types.get(object.getClass());
        for (FieldDescriptor fieldDescriptor : typeDescriptor.getFields().values()) {
            Object value = fieldDescriptor.get(object);
            PropertyPath subPath = path.property(fieldDescriptor.getName());
            context.serialize(subPath, value);
        }
    }

    @Override
    public boolean isReference() {
        return false;
    }
    
    public String toString() {
        return "ObjectType of " + types.values();
    }

    public int hashCode() {
        int hash = types.hashCode();
        return 31*hash + rootType.hashCode();
    }
    
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj != null && obj.getClass().equals(ObjectType.class)) {
            ObjectType<?> other = (ObjectType<?>) obj;
            return this.rootType.equals(other.rootType) && this.types.equals(other.types);
        } else {
            return false;
        }
    }
}
