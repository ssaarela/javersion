package org.javersion.reflect;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static java.util.Collections.unmodifiableSet;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

public abstract class AbstractTypeDescriptor<F extends AbstractFieldDescriptor<F, T>, T extends AbstractTypeDescriptor<F, T>> 
        extends AbstractElement {
    
    private static final Predicate<Class<?>> isInterface = new Predicate<Class<?>>() {

        @Override
        public boolean apply(Class<?> input) {
            return input.isInterface();
        }

    };

    protected final AbstractTypeDescriptors<F, T> typeDescriptors;
    
    protected final TypeToken<?> typeToken;
    
    private volatile Map<String, F> fields;

    private volatile Set<Class<?>> classes;

    public AbstractTypeDescriptor(AbstractTypeDescriptors<F, T> typeDescriptors, TypeToken<?> typeToken) {
        this.typeDescriptors = typeDescriptors;
        this.typeToken = checkNotNull(typeToken);
    }

    public Map<String, F> getFields() {
        Map<String, F> result = fields;
        if (result == null) {
            synchronized(this) {
                result = fields;
                if (result == null) {
                    result = Maps.newHashMap();
                    collectFields(typeToken.getRawType(), result);
                    result = fields = ImmutableMap.copyOf(result);
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
    
    private static LinkedHashSet<Class<?>> newLinkedHashSet() {
        return Sets.<Class<?>>newLinkedHashSet();
    }

}
