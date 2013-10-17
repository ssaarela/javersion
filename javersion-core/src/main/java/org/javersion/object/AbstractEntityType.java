package org.javersion.object;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.javersion.reflect.ElementDescriptor;
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
                ValueMapping<V> child = context.describe(fieldDescriptor, fieldDescriptor.getType());
                
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
    
}
