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

import static java.util.regex.Matcher.quoteReplacement;
import static org.javersion.reflect.ConstructorSignature.DEFAULT_CONSTRUCTOR;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javersion.util.Check;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.reflect.TypeToken;

public final class TypeDescriptor implements ElementDescriptor {


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

    protected final TypeDescriptors typeDescriptors;

    private static final Pattern DUPLICATE_OWNER_NAME = Pattern.compile("([^, <>$]+)\\.(\\1\\$)");

    public TypeDescriptor(TypeDescriptors typeDescriptors, TypeToken<?> typeToken) {
        this.typeDescriptors = Check.notNull(typeDescriptors, "typeDescriptors");
        this.typeToken = Check.notNull(typeToken, "typeToken");
    }

    public List<Annotation> getAnnotations() {
        return ImmutableList.copyOf(getRawType().getAnnotations());
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return getRawType().getAnnotation(annotationClass);
    }

    public <A extends Annotation> boolean hasAnnotation(Class<A> annotationClass) {
        return getRawType().isAnnotationPresent(annotationClass);
    }

    public boolean equalTo(TypeDescriptor type) {
        return equalTo(type.getRawType());
    }

    public boolean equalTo(Class<?> type) {
        return getRawType().equals(type);
    }

    public Map<String, FieldDescriptor> getFields() {
        Map<String, FieldDescriptor> result = new HashMap<>();
        collectFields(typeToken.getRawType(), result);
        return ImmutableSortedMap.copyOf(result);
    }

    public String getName() {
        return getRawType().getName();
    }

    public String getSimpleName() {
        return getSimpleName(getRawType());
    }

    public static String getSimpleName(Class<?> cls) {
        String fqn = cls.getName();
        int i = fqn.lastIndexOf('.');
        if (i > 0) {
            return fqn.substring(i + 1);
        } else {
            return fqn;
        }
    }

    public Map<ConstructorSignature, ConstructorDescriptor> getConstructors() {
        ImmutableMap.Builder<ConstructorSignature, ConstructorDescriptor> result = ImmutableMap.builder();
        for (Constructor<?> constructor : getRawType().getDeclaredConstructors()) {
            if (typeDescriptors.constructorFilter.apply(constructor)) {
                result.put(new ConstructorSignature(constructor), new ConstructorDescriptor(this, constructor));
            }
        }
        return result.build();
    }

    public Map<MethodSignature, MethodDescriptor> getMethods() {
        Map<MethodSignature, MethodDescriptor> result = new HashMap<>();
        collectMethods(getRawType(), result);
        return ImmutableMap.copyOf(result);
    }

    public Map<String, BeanProperty> getProperties() {
        try {
            ImmutableMap.Builder<String, BeanProperty> properties = ImmutableMap.builder();
            BeanInfo beanInfo = Introspector.getBeanInfo(getRawType(), Object.class);
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                MethodDescriptor readMethod = getMethodDescriptor(propertyDescriptor.getReadMethod());
                MethodDescriptor writeMethod = getMethodDescriptor(propertyDescriptor.getWriteMethod());
                BeanProperty property = new BeanProperty(propertyDescriptor.getName(), readMethod, writeMethod);
                properties.put(property.getName(), property);
            }
            return properties.build();
        } catch (IntrospectionException e) {
            throw new ReflectionException(e);
        }
    }

    private MethodDescriptor getMethodDescriptor(Method method) {
        return method != null ? new MethodDescriptor(this, method) : null;
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

    public Class<?> getRawType() {
        return typeToken.getRawType();
    }

    public TypeToken<?> getTypeToken() {
        return typeToken;
    }

    public boolean isPrimitiveOrWrapper() {
        Class<?> clazz = getRawType();
        return WRAPPER_TO_PRIMITIVE.containsKey(clazz) || WRAPPER_TO_PRIMITIVE.containsValue(clazz);
    }

    public boolean isSuperTypeOf(TypeDescriptor type) {
        return isSuperTypeOf(type.getRawType());
    }

    public boolean isSuperTypeOf(Class<?> clazz) {
        return getRawType().isAssignableFrom(clazz);
    }

    public boolean isSubTypeOf(TypeDescriptor type) {
        return isSubTypeOf(type.getRawType());
    }

    public boolean isSubTypeOf(Class<?> clazz) {
        return clazz.isAssignableFrom(getRawType());
    }

    public boolean isEnum() {
        return getRawType().isEnum();
    }

    public ConstructorDescriptor getDefaultConstructor() {
        return getConstructors().get(DEFAULT_CONSTRUCTOR);
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

    public TypeDescriptors getTypeDescriptors() {
        return typeDescriptors;
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
        String name = typeToken.toString();
        Matcher matcher = DUPLICATE_OWNER_NAME.matcher(name);
        StringBuffer sb = new StringBuffer(name.length());
        while (matcher.find()) {
            matcher.appendReplacement(sb, quoteReplacement(matcher.group(2)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private void collectFields(Class<?> clazz, Map<String, FieldDescriptor> allFields) {
        for (Field field : clazz.getDeclaredFields()) {
            if (typeDescriptors.fieldFilter.apply(field) && !allFields.containsKey(field.getName())) {
                allFields.put(field.getName(), new FieldDescriptor(this, field));
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            collectFields(superClass, allFields);
        }
    }

    private void collectMethods(Class<?> clazz, Map<MethodSignature, MethodDescriptor> allMethods) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (typeDescriptors.methodFilter.apply(method)) {
                MethodSignature identifier = new MethodSignature(method);
                if (!allMethods.containsKey(identifier)) {
                    allMethods.put(identifier, new MethodDescriptor(this, method));
                }
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            collectMethods(superClass, allMethods);
        }
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(getRawType().getModifiers());
    }
}