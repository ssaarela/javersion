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

import java.util.HashMap;
import java.util.Map;

import org.javersion.object.DescribeContext;
import org.javersion.object.Id;
import org.javersion.object.TypeContext;
import org.javersion.object.VersionIgnore;
import org.javersion.object.VersionProperty;
import org.javersion.object.types.IdentifiableObjectType;
import org.javersion.object.types.IdentifiableType;
import org.javersion.object.types.ObjectType;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.reflect.BeanProperty;
import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.Property;
import org.javersion.reflect.TypeDescriptor;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;

public class ObjectTypeMapping<O> implements TypeMapping {

    private final BiMap<String, TypeDescriptor> typesByAlias;

    public ObjectTypeMapping(BiMap<String, TypeDescriptor> typesByAlias) {
        this.typesByAlias = typesByAlias;
    }

    @Override
    public boolean applies(PropertyPath path, TypeContext typeContext) {
        if (path == null) {
            return false;
        }
        for (TypeDescriptor typeDescriptor : typesByAlias.values()) {
            if (typeDescriptor.equals(typeContext.type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public  synchronized ValueType describe(PropertyPath path, TypeDescriptor typeDescriptor, DescribeContext context) {
        Describe describe = new Describe(path, context);
        typesByAlias.values().forEach(describe::add);

        if (describe.idProperty != null) {
            return new IdentifiableObjectType<>(typesByAlias, describe.properties, describe.idProperty, describe.idType);
        } else {
            return new ObjectType<>(typesByAlias, describe.properties);
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

    private static class Describe {
        Map<String, Property> properties = new HashMap<>();
        Property idProperty = null;
        IdentifiableType idType = null;

        final PropertyPath path;
        final DescribeContext context;

        Describe(PropertyPath path, DescribeContext context) {
            this.path = path;
            this.context = context;
        }

        void add(TypeDescriptor type) {
            type.getProperties().forEach(this::add);
            type.getFields().values().forEach(this::add);
        }

        private void add(BeanProperty property) {
            if (acceptProperty(property)) {
                String name = getName(property.getReadMethod(), property.getName());
                if (!properties.containsKey(name)) {
                    validate(property);
                    properties.put(name, property);
                    SubPath subPath = path.property(name);
                    if (property.getReadMethod().hasAnnotation(Id.class)) {
                        idProperty = property;
                        idType = (IdentifiableType) context.describeNow(subPath, new TypeContext(property));
                    } else {
                        context.describeAsync(subPath, new TypeContext(property));
                    }
                }
            }
        }

        private void add(FieldDescriptor fieldDescriptor) {
            if (acceptField(fieldDescriptor)) {
                String name = getName(fieldDescriptor, fieldDescriptor.getName());
                if (!properties.containsKey(name)) {
                    properties.put(name, fieldDescriptor);
                    SubPath subPath = path.property(name);
                    if (fieldDescriptor.hasAnnotation(Id.class)) {
                        idProperty = fieldDescriptor;
                        idType = (IdentifiableType) context.describeNow(subPath, new TypeContext(fieldDescriptor));
                    } else {
                        context.describeAsync(subPath, new TypeContext(fieldDescriptor));
                    }
                }
            }
        }

        private boolean acceptField(FieldDescriptor fieldDescriptor) {
            return !fieldDescriptor.isTransient() && !fieldDescriptor.hasAnnotation(VersionIgnore.class);
        }

        private boolean acceptProperty(BeanProperty property) {
            return property.isReadable() && property.getReadMethod().hasAnnotation(VersionProperty.class);
        }

        private void validate(BeanProperty property) {
            if (!property.isWritable()) {
                throw new IllegalArgumentException("@VersionProperty " + property.getDeclaringType().getSimpleName() +
                        "." + property.getName() +
                        " should have a matching setter");
            }
        }

        private String getName(ElementDescriptor element, String defaultName) {
            VersionProperty versionProperty = element.getAnnotation(VersionProperty.class);
            if (versionProperty != null && versionProperty.value().length() > 0) {
                return versionProperty.value();
            }
            return defaultName;
        }
    }
}