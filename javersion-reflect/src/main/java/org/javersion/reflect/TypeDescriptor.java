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
package org.javersion.reflect;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.javersion.util.Check;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

public class TypeDescriptor extends ElementDescriptor {


    public static final BiMap<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE;

    static {
        ImmutableBiMap.Builder<Class<?>, Class<?>> primitives = ImmutableBiMap.builder();
        primitives.put(Byte.class, Byte.TYPE);
        primitives.put(Short.class, Short.TYPE);
        primitives.put(Integer.class, Integer.TYPE);
        primitives.put(Long.class, Long.TYPE);
        primitives.put(Float.class, Float.TYPE);
        primitives.put(Double.class, Double.TYPE);
        primitives.put(Boolean.class, Boolean.TYPE);
        primitives.put(Character.class, Character.TYPE);
        primitives.put(void.class, Void.TYPE);
        WRAPPER_TO_PRIMITIVE = primitives.build();
    }

    protected final TypeToken<?> typeToken;

    private volatile SortedMap<String, FieldDescriptor> fields;

    public TypeDescriptor(TypeDescriptors typeDescriptors, TypeToken<?> typeToken) {
        super(typeDescriptors);
        this.typeToken = Check.notNull(typeToken, "typeToken");
    }

    public boolean equalTo(Class<?> type) {
        return getRawType().equals(type);
    }

    public Map<String, FieldDescriptor> getFields() {
        Map<String, FieldDescriptor> result = fields;
        if (result == null) {
            synchronized(this) {
                result = fields;
                if (result == null) {
                    result = Maps.newHashMap();
                    collectFields(typeToken.getRawType(), result);
                    result = fields = ImmutableSortedMap.copyOf(result);
                }
            }
        }
        return result;
    }

    public String getSimpleName() {
        String fqn = getRawType().getName();
        int i = fqn.lastIndexOf('.');
        if (i > 0) {
            return fqn.substring(i + 1);
        } else {
            return fqn;
        }
    }

    public List<BeanProperty> getProperties() {
        try {
            ImmutableList.Builder<BeanProperty> properties = ImmutableList.builder();
            BeanInfo beanInfo = Introspector.getBeanInfo(getRawType());
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                MethodDescriptor readMethod = getMethodDescriptor(propertyDescriptor.getReadMethod());
                MethodDescriptor writeMethod = getMethodDescriptor(propertyDescriptor.getWriteMethod());
                BeanProperty property = new BeanProperty(propertyDescriptor.getName(), readMethod, writeMethod);
                properties.add(property);
            }
            return properties.build();
        } catch (IntrospectionException e) {
            throw new ReflectionException(e);
        }
    }

    private MethodDescriptor getMethodDescriptor(Method readMethod) {
        return readMethod != null ? new MethodDescriptor(typeDescriptors, readMethod) : null;
    }

    public boolean hasField(String fieldName) {
        return getFields().containsKey(fieldName);
    }

    public FieldDescriptor getField(String name) {
        FieldDescriptor field = getFields().get(name);
        if (field == null) {
            throw new IllegalArgumentException("Field not found: " + name);
        }
        return field;
    }

    public TypeDescriptor resolveGenericParameter(Class<?> genericClass, int genericParam) {
        return typeDescriptors.get(typeToken.resolveType(genericClass.getTypeParameters()[genericParam]));
    }

    @Override
    Class<?> getElement() {
        return getRawType();
    }

    public Class<?> getRawType() {
        return typeToken.getRawType();
    }

    private void collectFields(Class<?> clazz, Map<String, FieldDescriptor> fields) {
        for (Field field : clazz.getDeclaredFields()) {
            if (typeDescriptors.fieldFilter.apply(field) && !fields.containsKey(field.getName())) {
                fields.put(field.getName(), new FieldDescriptor(typeDescriptors, field));
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            collectFields(superClass, fields);
        }
    }

    public boolean isPrimitiveOrWrapper() {
        Class<?> clazz = getRawType();
        return WRAPPER_TO_PRIMITIVE.containsKey(clazz) || WRAPPER_TO_PRIMITIVE.containsValue(clazz);
    }

    public boolean isSuperTypeOf(Class<?> clazz) {
        return getRawType().isAssignableFrom(clazz);
    }

    public boolean isSubTypeOf(Class<?> clazz) {
        return clazz.isAssignableFrom(getRawType());
    }

    public boolean isEnum() {
        return getRawType().isEnum();
    }

    public Object newInstance() {
        Constructor<?> constructor;
        try {
            constructor = getRawType().getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new ReflectionException("Failed to instantiate " + toString(), e);
        }
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof TypeDescriptor) {
            TypeDescriptor other = (TypeDescriptor) obj;
            return this.typeToken.equals(other.typeToken) &&
                    this.typeDescriptors.equals(other.typeDescriptors);
        } else {
            return false;
        }
    }

    @Override
    public final int hashCode() {
        return 31 * typeDescriptors.hashCode() + typeToken.hashCode();
    }

    @Override
    public String toString() {
        return typeToken.toString();
    }
}