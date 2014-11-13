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

import java.util.Map;

import org.javersion.object.Persistent;
import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;


public class ObjectType<O> implements ValueType {

    protected final Map<String, TypeDescriptor> typesByAlias;

    protected final Map<Class<?>, String> aliasByClass;

    @SuppressWarnings("unchecked")
    public ObjectType(String alias, TypeDescriptor type) {
        this(ImmutableBiMap.of(alias, type));
    }

    public ObjectType(BiMap<String, TypeDescriptor> typesByAlias) {
        Check.notNullOrEmpty(typesByAlias, "typesByAlias");
        this.typesByAlias = ImmutableMap.copyOf(typesByAlias);
        ImmutableMap.Builder<Class<?>, String> builder = ImmutableMap.builder();
        for (Map.Entry<String, TypeDescriptor> entry : typesByAlias.entrySet()) {
            builder.put(entry.getValue().getRawType(), entry.getKey());
        }
        aliasByClass = builder.build();
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        String alias = ((Persistent.Object) value).type;
        TypeDescriptor typeDescriptor = Check.notNull$(typesByAlias.get(alias), "Unsupported type: %s", alias);
        return typeDescriptor.newInstance();
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
        TypeDescriptor typeDescriptor = getAlias(object.getClass());
        for (PropertyTree child : propertyTree.getChildren()) {
            String fieldName = child.getName();
            if (typeDescriptor.hasField(fieldName)) {
                FieldDescriptor fieldDescriptor = typeDescriptor.getField(fieldName);
                Object value = context.getObject(child);
                fieldDescriptor.set(object, value);
            }
        }
    }

    private TypeDescriptor getAlias(Class<?> clazz) {
        return typesByAlias.get(aliasByClass.get(clazz));
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        String alias = aliasByClass.get(object.getClass());
        context.put(path, Persistent.object(alias));
        TypeDescriptor typeDescriptor = typesByAlias.get(alias);
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
        return "ObjectType of " + typesByAlias.values();
    }

    public int hashCode() {
        return typesByAlias.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj != null && obj.getClass().equals(ObjectType.class)) {
            ObjectType<?> other = (ObjectType<?>) obj;
            return this.typesByAlias.equals(other.typesByAlias);
        } else {
            return false;
        }
    }
}
