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
import java.util.stream.Collectors;

import org.javersion.object.DescribeContext;
import org.javersion.object.Id;
import org.javersion.object.TypeContext;
import org.javersion.object.VersionIgnore;
import org.javersion.object.VersionProperty;
import org.javersion.object.mapping.MappingResolver.Result;
import org.javersion.object.types.*;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.reflect.*;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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
        typesByAlias.forEach(describe::add);

        return describe.build();
    }
//
//    public static String getAlias(String aliasOrEmpty, TypeDescriptor type) {
//        if (!isNullOrEmpty(aliasOrEmpty)) {
//            return aliasOrEmpty;
//        }
//        return getAlias(type);
//    }
//
//    public static String getAlias(TypeDescriptor type) {
//        if (type.hasAnnotation(Versionable.class)) {
//            String alias = type.getAnnotation(Versionable.class).alias();
//            if (!isNullOrEmpty(alias)) {
//                return alias;
//            }
//        }
//        if (USE_JACKSON_ANNOTATIONS && type.hasAnnotation(JsonTypeName.class)) {
//            String alias = type.getAnnotation(JsonTypeName.class).value();
//            if (!isNullOrEmpty(alias)) {
//                return alias;
//            }
//        }
//        return type.getSimpleName();
//    }

    private static class Describe {

        final PropertyPath path;
        final DescribeContext context;

        private BasicObjectType root;
        private List<BasicObjectType> subclasses = new ArrayList<>();

        private TypeDescriptor type;
        private String alias;
        private Result<StaticExecutable> creator;
        private ImmutableSet<String> creatorParameters;
        private ObjectIdentifier identifier;
        private Map<String, Property> properties;

        Describe(PropertyPath path, DescribeContext context) {
            this.path = path;
            this.context = context;
            clear();
        }

        void add(String alias, TypeDescriptor type) {
            clear();
            this.alias = alias;
            this.type = type;
            assignConstructor(type);
            type.getProperties().values().forEach(this::add);
            type.getFields().values().forEach(this::add);

            ObjectCreator objectConstructor = getObjectConstructor();

            BasicObjectType objectType = BasicObjectType.of(type, alias, objectConstructor, identifier, properties);
            if (root == null) {
                root = objectType;
            } else {
                subclasses.add(objectType);
            }
        }

        private ObjectCreator getObjectConstructor() {
            return creator != null ? new ObjectCreator(creator.value, creatorParameters) : null;
        }

        private void clear() {
            type = null;
            alias = null;
            creator = Result.notFound();
            creatorParameters = ImmutableSet.of();
            identifier = null;
            properties = new HashMap<>();
        }

        private void assignConstructor(TypeDescriptor type) {
            type.getConstructors().values().forEach(this::add);
            type.getMethods().values().forEach(this::add);
            if (!type.isAbstract()) {
                Check.notNull(creator, "constructor");
                creatorParameters = ImmutableSet.copyOf(getCreatorParameters());
            }
        }

        private void add(ConstructorDescriptor constructor) {
            Result<StaticExecutable> creator = context.getMappingResolver().creator(constructor);
            this.creator = MappingResolver.higherOf(this.creator, creator);
        }

        private void add(MethodDescriptor creatorMethod) {
            if (creatorMethod.isStatic() && creatorMethod.getRawReturnType() != void.class) {
                Result<StaticExecutable> creator = context.getMappingResolver().creator(creatorMethod);
                this.creator = MappingResolver.higherOf(this.creator, creator);
            }
        }

        private List<String> getCreatorParameters() {
            return creator.value.getParameters().stream()
                    .map(this::getParameterName)
                    .collect(Collectors.toList());
        }

        private String getParameterName(ParameterDescriptor parameter) {
            return Check.notNull(context.getMappingResolver().name(parameter), type.getSimpleName() + " parameter name").value;
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
                throw new IllegalArgumentException(type.getSimpleName() + " should not have multiple @Id-properties");
            }
            IdentifiableType idType;
            if (property.isWritable() || creatorParameters.contains(name)) {
                idType = (IdentifiableType) context.describeNow(path.property(name), typeContext);
            } else {
                idType = (IdentifiableType) context.describeNow(null, typeContext);
            }
            identifier = new ObjectIdentifier(property, idType, name);
        }

        private void add(String name, Property property, TypeContext typeContext) {
            if (!property.isWritable() && !creatorParameters.contains(name)) {
                throw new IllegalArgumentException(type.getSimpleName() + "."
                        + name + " should have a matching setter or constructor parameter");
            }

            SubPath subPath = path.property(name);
            if (!properties.containsKey(name)) {
                properties.put(name, property);
                context.describeAsync(subPath, typeContext);
            }
        }

        private boolean acceptIdField(FieldDescriptor fieldDescriptor) {
            return acceptField(fieldDescriptor) && fieldDescriptor.hasAnnotation(Id.class);
        }

        private boolean acceptField(FieldDescriptor fieldDescriptor) {
            return !fieldDescriptor.isTransient() &&
                    !fieldDescriptor.isStatic() &&
                    !fieldDescriptor.isSynthetic() &&
                    !fieldDescriptor.hasAnnotation(VersionIgnore.class);
        }

        private boolean acceptIdProperty(BeanProperty property) {
            return property.isReadable() && property.getReadMethod().hasAnnotation(Id.class);
        }

        private boolean acceptProperty(BeanProperty property) {
            return property.isReadable() && property.getReadMethod().hasAnnotation(VersionProperty.class);
        }

        private String getName(ElementDescriptor element, String defaultName) {
            VersionProperty versionProperty = element.getAnnotation(VersionProperty.class);
            if (versionProperty != null && versionProperty.value().length() > 0) {
                return versionProperty.value();
            }
            return defaultName;
        }

        public ObjectType build() {
            if (subclasses.isEmpty()) {
                return root;
            } else {
                return PolymorphicObjectType.of(root, subclasses);
            }
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