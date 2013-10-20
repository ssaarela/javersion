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

import java.util.Set;

import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;

import com.google.common.collect.Sets;

public abstract class AbstractEntityTypeFactory<V> 
        implements ValueTypeFactory<V> {
    
    protected final TypeDescriptors typeDescriptors;
    
    public AbstractEntityTypeFactory(TypeDescriptors typeDescriptors) {
        this.typeDescriptors = typeDescriptors;
    }

    @Override
    public boolean applies(ValueMappingKey mappingKey) {
        return mappingKey.typeDescriptor.hasAnnotation(Versionable.class);
    }
    
    @Override
    public  ValueType<V> describe(DescribeContext<V> context) {
        PropertyPath path = context.getCurrentPath();
        TypeDescriptor typeDescriptor = context.getCurrentType();
        FieldDescriptor idField = getIdField(typeDescriptor);
        SubPath targetPath = getTargetPath(idField);
        if (targetPath != null && !path.equals(targetPath)) {
            context.describe(targetPath, new ValueMappingKey(typeDescriptor));
            
            return new ReferenceType<V>(idField);
        } else {
            Set<TypeDescriptor> types = getSubTypes(typeDescriptor);
            for (TypeDescriptor type : types) {
                for (FieldDescriptor fieldDescriptor : type.getFields().values()) {
                    ValueMappingKey mappingKey = new ValueMappingKey(fieldDescriptor);
                    context.describe(path.property(fieldDescriptor.getName()), mappingKey);
                }
            }
            return newEntityType(types);
        }
    }
    
    private static SubPath getTargetPath(FieldDescriptor idField) {
        SubPath targetRoot = getTargetRoot(idField);
        return targetRoot != null ? targetRoot.index("") : null;
    }
    
    public static SubPath getTargetRoot(FieldDescriptor idField) {
        if (idField != null) {
            Id id = idField.getAnnotation(Id.class);
            return PropertyPath.ROOT.property("@").property(id.alias());
        } else {
            return null;
        }
    }

    private static FieldDescriptor getIdField(TypeDescriptor typeDescriptor) {
        for (FieldDescriptor fieldDescriptor : typeDescriptor.getFields().values()) {
            if (fieldDescriptor.hasAnnotation(Id.class)) {
                return fieldDescriptor;
            }
        }
        return null;
    }
    
    protected abstract ValueType<V> newEntityType(Set<TypeDescriptor> types);
    
    protected Set<TypeDescriptor> getSubTypes(TypeDescriptor typeDescriptor) {
        return collectSubTypes(typeDescriptor, Sets.<TypeDescriptor>newHashSet());
    }
    
    private Set<TypeDescriptor> collectSubTypes(TypeDescriptor typeDescriptor, Set<TypeDescriptor> subClasses) {
        subClasses.add(typeDescriptor);
        Versionable versionable = typeDescriptor.getAnnotation(Versionable.class);
        for (Class<?> subClass : versionable.subClasses()) {
            collectSubTypes(typeDescriptors.get(subClass), subClasses);
        }
        return subClasses;
    }
    
}