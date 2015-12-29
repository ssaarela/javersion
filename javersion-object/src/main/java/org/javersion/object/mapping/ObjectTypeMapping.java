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
import org.javersion.object.VersionIgnore;
import org.javersion.object.types.IdentifiableObjectType;
import org.javersion.object.types.IdentifiableType;
import org.javersion.object.types.ObjectType;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Sets;

public class ObjectTypeMapping<O> implements TypeMapping {

    private final BiMap<String, TypeDescriptor> typesByAlias;

    public ObjectTypeMapping(BiMap<String, TypeDescriptor> typesByAlias) {
        this.typesByAlias = typesByAlias;
    }

    @Override
    public boolean applies(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
        if (path == null) {
            return false;
        }
        for (TypeDescriptor typeDescriptor : typesByAlias.values()) {
            if (typeDescriptor.getRawType().equals(localTypeDescriptor.typeDescriptor.getRawType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public  synchronized ValueType describe(PropertyPath path, TypeDescriptor typeDescriptor, DescribeContext context) {
        Set<FieldDescriptor> uniqueFields = Sets.newHashSet();
        FieldDescriptor idField = null;
        IdentifiableType idType = null;
        for (TypeDescriptor type : typesByAlias.values()) {
            for (FieldDescriptor fieldDescriptor : type.getFields().values()) {
                if (uniqueFields.add(fieldDescriptor) && filter.apply(fieldDescriptor)) {
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
            // TODO: ScalarObjectType for use as Map key?
            return new IdentifiableObjectType<>(typesByAlias, idField, idType);
        } else {
            return new ObjectType<>(typesByAlias);
        }
    }

    public static String getAlias(TypeDescriptor type) {
        return type.getSimpleName();
    }

    public static String getAlias(String aliasOrEmpty, TypeDescriptor type) {
        if (!Strings.isNullOrEmpty(aliasOrEmpty)) {
            return aliasOrEmpty;
        }
        return getAlias(type);
    }

}