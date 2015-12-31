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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javersion.object.DescribeContext;
import org.javersion.object.Id;
import org.javersion.object.TypeContext;
import org.javersion.object.VersionIgnore;
import org.javersion.object.VersionProperty;
import org.javersion.object.types.IdentifiableType;
import org.javersion.object.types.ObjectType;
import org.javersion.object.types.ObjectType.Identifier;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.reflect.BeanProperty;
import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.Property;
import org.javersion.reflect.TypeDescriptor;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

public class ObjectTypeMapping<O> implements TypeMapping {

    private final ImmutableMap<String, TypeDescriptor> typesByAlias;

    public ObjectTypeMapping(Map<String, TypeDescriptor> typesByAlias) {
        this.typesByAlias = verifyAndSort(typesByAlias);
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

        return ObjectType.of(typesByAlias, describe.properties, describe.identifier);
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
        Identifier identifier;

        boolean first = true;
        private TypeDescriptor currentType;
        final PropertyPath path;
        final DescribeContext context;

        Describe(PropertyPath path, DescribeContext context) {
            this.path = path;
            this.context = context;
        }

        void add(TypeDescriptor type) {
            currentType = type;
            type.getProperties().values().forEach(this::add);
            type.getFields().values().forEach(this::add);
            first = false;
            currentType = null;
        }

        private void add(BeanProperty property) {
            if (acceptIdProperty(property)) {
                String name = getName(property.getReadMethod(), property.getName());
                setIdentifier(name, property, new TypeContext(property));
            }
            else if (acceptProperty(property)) {
                String name = getName(property.getReadMethod(), property.getName());
                add(name, property, new TypeContext(property));
            }
        }

        private void add(FieldDescriptor fieldDescriptor) {
            if (acceptIdField(fieldDescriptor)) {
                String name = getName(fieldDescriptor, fieldDescriptor.getName());
                setIdentifier(name, fieldDescriptor, new TypeContext(fieldDescriptor));
            }
            else if (acceptField(fieldDescriptor)) {
                String name = getName(fieldDescriptor, fieldDescriptor.getName());
                add(name, fieldDescriptor, new TypeContext(fieldDescriptor));
            }
        }

        private void setIdentifier(String name, Property property, TypeContext typeContext) {
            if (identifier != null) {
                throw new IllegalArgumentException(currentType.getSimpleName() + " should not have multiple @Id-properties");
            }
            IdentifiableType idType;
            if (property.isWritableFrom(currentType)) {
                idType = (IdentifiableType) context.describeNow(path.property(name), typeContext);
            } else {
                idType = (IdentifiableType) context.describeNow(null, typeContext);
            }
            identifier = new Identifier(property, idType, name);
        }

        private void add(String name, Property property, TypeContext typeContext) {
            SubPath subPath = path.property(name);
            if (!properties.containsKey(name)) {
                properties.put(name, property);
                context.describeAsync(subPath, typeContext);
            }
        }

        private boolean acceptIdField(FieldDescriptor fieldDescriptor) {
            return first && acceptField(fieldDescriptor) && fieldDescriptor.hasAnnotation(Id.class);
        }

        private boolean acceptField(FieldDescriptor fieldDescriptor) {
            return !fieldDescriptor.isTransient() && !fieldDescriptor.hasAnnotation(VersionIgnore.class);
        }

        private boolean acceptIdProperty(BeanProperty property) {
            return first && property.isReadable() && property.getReadMethod().hasAnnotation(Id.class);
        }

        private boolean acceptProperty(BeanProperty property) {
            if (property.isReadable() && property.getReadMethod().hasAnnotation(VersionProperty.class)) {
                if (!property.isWritable()) {
                    throw new IllegalArgumentException(currentType.getSimpleName() + ": @VersionProperty " + property.getName() +
                            " should have a matching setter");
                }
                return true;
            }
            return false;
        }

        private String getName(ElementDescriptor element, String defaultName) {
            VersionProperty versionProperty = element.getAnnotation(VersionProperty.class);
            if (versionProperty != null && versionProperty.value().length() > 0) {
                return versionProperty.value();
            }
            return defaultName;
        }
    }

    /**
     * Sorts TypeDescriptors in topological order so that super class always precedes it's sub classes.
     */
    static ImmutableMap<String, TypeDescriptor> verifyAndSort(Map<String, TypeDescriptor> typesByAlias) {
        List<String> sorted = new ArrayList<>();
        for (Map.Entry<String, TypeDescriptor> entry : typesByAlias.entrySet()) {
            String alias = entry.getKey();
            TypeDescriptor type = entry.getValue();
            int i = sorted.size()-1;
            for (; i >= 0; i--) {
                TypeDescriptor other = typesByAlias.get(sorted.get(i));
                if (other.isSuperTypeOf(type.getRawType())) {
                    break;
                }
            }
            sorted.add(i+1, alias);
        }

        ImmutableMap.Builder<String, TypeDescriptor> result = ImmutableMap.builder();
        for (String alias : sorted) {
            result.put(alias, typesByAlias.get(alias));
        }
        return result.build();
    }

}