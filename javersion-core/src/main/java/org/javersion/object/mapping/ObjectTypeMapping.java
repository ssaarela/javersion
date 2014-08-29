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
package org.javersion.object.mapping;

import java.util.Set;

import org.javersion.object.DescribeContext;
import org.javersion.object.Id;
import org.javersion.object.LocalTypeDescriptor;
import org.javersion.object.types.IdentifiableObjectType;
import org.javersion.object.types.IdentifiableType;
import org.javersion.object.types.ObjectType;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class ObjectTypeMapping<O> implements TypeMapping {
    
    private final Set<TypeDescriptor> types;
    
    private final Class<? extends O> rootType;
    
    public ObjectTypeMapping() {
        this.rootType = null;
        this.types = null;
    }
    public ObjectTypeMapping(Class<? extends O> rootType, Iterable<TypeDescriptor> types) {
        this.rootType = Check.notNull(rootType, "rootType");
        Check.notNullOrEmpty(types, "types");

        this.types = ImmutableSet.copyOf(types);
    }

    @Override
    public boolean applies(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
        return types.contains(localTypeDescriptor.typeDescriptor);
    }
    
    @Override
    public  synchronized ValueType describe(PropertyPath path, TypeDescriptor type, DescribeContext context) {
        return describe(path, rootType, types, context);
    }
    
    public static ValueType describe(PropertyPath path, Class<?> rootType, Iterable<TypeDescriptor> allTypes, DescribeContext context) {
        Set<FieldDescriptor> uniqueFields = Sets.newHashSet();
        FieldDescriptor idField = null;
        IdentifiableType idType = null;
        for (TypeDescriptor type : allTypes) {
            for (FieldDescriptor fieldDescriptor : type.getFields().values()) {
                if (uniqueFields.add(fieldDescriptor)) {
                    SubPath subPath = path.property(fieldDescriptor.getName());
                    if (fieldDescriptor.hasAnnotation(Id.class)) {
                        idField = fieldDescriptor;
                        idType = (IdentifiableType) context.describeNow(subPath, fieldDescriptor);
                    } else {
                        context.describeAsync(subPath, fieldDescriptor);
                    }
                }
            }
        }
        if (idField != null) {
            return new IdentifiableObjectType<>(rootType, allTypes, idField, idType);
        } else {
            return new ObjectType<>(rootType, allTypes);
        }
    }
    
}