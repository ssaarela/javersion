package org.javersion.reflect;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

public abstract class AbstractTypeDescriptor<F extends AbstractFieldDescriptor<F, T>, T extends AbstractTypeDescriptor<F, T>> 
        extends AbstractElement {

    protected final AbstractTypeDescriptors<F, T> typeDescriptors;
    
    protected final TypeToken<?> typeToken;
    
    protected volatile Map<String, F> fields;
    
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
    
    public Iterable<Class<?>> getSuperClasses() {
        List<Class<?>> superClasses = Lists.newArrayList();
        Class<?> clazz = getRawType();
        while (clazz != null) {
            superClasses.add(clazz);
            clazz = clazz.getSuperclass();
        }
        return superClasses;
    }
    
    public Iterable<Class<?>> getInterfaces() {
        Set<Class<?>> ifaces = Sets.newLinkedHashSet();
        List<Class<?>> stack = Lists.newArrayList();

        Class<?> clazz = getRawType();
        if (clazz.isInterface()) {
            ifaces.add(clazz);
            stack.add(clazz);
        }
        // TODO Collect interfaces
        return ifaces;
    }
    
    public F getField(String name) {
        return getFields().get(name);
    }
    
    public AbstractTypeDescriptor<F, T> resolveGenericParameter(Class<?> genericClass, int genericParam) {
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
            collectFields(clazz, fields);
        }
    }

}
