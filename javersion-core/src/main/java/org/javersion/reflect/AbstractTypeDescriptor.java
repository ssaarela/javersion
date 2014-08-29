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

import static com.google.common.base.Predicates.not;
import static java.util.Collections.unmodifiableSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.javersion.util.Check;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.common.reflect.TypeToken;

public abstract class AbstractTypeDescriptor<
            F extends AbstractFieldDescriptor<F, T, Ts>,
            T extends AbstractTypeDescriptor<F, T, Ts>,
            Ts extends AbstractTypeDescriptors<F, T, Ts>>
        extends ElementDescriptor<F, T, Ts> {

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

    private static final Predicate<Class<?>> isInterface = new Predicate<Class<?>>() {

        @Override
        public boolean apply(Class<?> input) {
            return input.isInterface();
        }

    };

    protected final TypeToken<?> typeToken;

    private volatile SortedMap<String, F> fields;

    private volatile Set<Class<?>> classes;

    public AbstractTypeDescriptor(Ts typeDescriptors, TypeToken<?> typeToken) {
        super(typeDescriptors);
        this.typeToken = Check.notNull(typeToken, "typeToken");
    }

    public boolean equalTo(Class<?> type) {
        return getRawType().equals(type);
    }

    public Map<String, F> getFields() {
        Map<String, F> result = fields;
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

    public Set<Class<?>> getSuperClasses() {
        return Sets.filter(getAllClasses(), not(isInterface));
    }

    public Set<Class<?>> getInterfaces() {
        return Sets.filter(getAllClasses(), isInterface);
    }

    public Set<Class<?>> getAllClasses() {
        if (classes == null) {
            classes = unmodifiableSet(collectAllClasses(getRawType(), newLinkedHashSet()));
        }
        return classes;
    }

    public String getSimpleName() {
        return getRawType().getSimpleName();
    }

    public boolean hasField(String fieldName) {
        return getFields().containsKey(fieldName);
    }

    public F getField(String name) {
        F field = getFields().get(name);
        if (field == null) {
            throw new IllegalArgumentException("Field not found: " + name);
        }
        return field;
    }

    public T resolveGenericParameter(Class<?> genericClass, int genericParam) {
        return typeDescriptors.get(typeToken.resolveType(genericClass.getTypeParameters()[genericParam]));
    }

    @Override
    public Class<?> getElement() {
        return getRawType();
    }

    public Class<?> getRawType() {
        return typeToken.getRawType();
    }

    private void collectFields(Class<?> clazz, Map<String, F> fields) {
        for (Field field : clazz.getDeclaredFields()) {
            if (typeDescriptors.fieldFilter.apply(field) && !fields.containsKey(field.getName())) {
                fields.put(field.getName(), typeDescriptors.newFieldDescriptor(field));
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            collectFields(superClass, fields);
        }
    }

    private static Set<Class<?>> collectAllClasses(Class<?> clazz, LinkedHashSet<Class<?>> classes) {
        classes.add(clazz);

        List<Class<?>> stack = Lists.newArrayList();

        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            classes.add(superClass);
            stack.add(superClass);
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            if (classes.add(iface)) {
                stack.add(iface);
            }
        }

        for (Class<?> next : stack) {
            collectAllClasses(next, classes);
        }

        return classes;
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
            throw new RuntimeException(e);
        }
    }

    private static LinkedHashSet<Class<?>> newLinkedHashSet() {
        return Sets.<Class<?>>newLinkedHashSet();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof AbstractTypeDescriptor) {
            @SuppressWarnings("unchecked")
            T other = (T) obj;
            return getTypeDescriptors().equals(other.getTypeDescriptors())
                    && this.typeToken.equals(other.typeToken);
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
