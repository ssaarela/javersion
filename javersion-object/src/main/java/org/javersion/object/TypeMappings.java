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

import static org.javersion.object.mapping.PrimitiveTypeMapping.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.javersion.core.Revision;
import org.javersion.object.mapping.*;
import org.javersion.object.types.PropertyPathType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;
import org.javersion.util.Check;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class TypeMappings {

    public static TypeMapping STRING = new StringTypeMapping();

    public static TypeMapping BIG_INTEGER = new ToStringTypeMapping(BigInteger.class);
    public static TypeMapping BIG_DECIMAL = new ToStringTypeMapping(BigDecimal.class);

    public static TypeMapping ENUM = new EnumTypeMapping();

    public static TypeMapping DATE_TIME = new DateTimeMapping();

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(List<TypeMapping> mappings) {
        return new Builder(mappings);
    }

    public static final List<TypeMapping> DEFAULT_MAPPINGS =
            ImmutableList.of(
                    new VersionableReferenceTypeMapping(),
                    new VersionableTypeMapping(),
                    new ListTypeMapping(),
                    new NavigableSetMapping(),
                    new SortedSetMapping(),
                    new SetTypeMapping(),
                    new NavigableMapMapping(),
                    new SortedMapMapping(),
                    new MapTypeMapping(),
                    new CollectionTypeMapping(),
                    new ToStringTypeMapping(Revision.class),
                    new SimpleValueMapping(PropertyPath.class, new PropertyPathType()),
                    new SimpleValueMapping(UUID.class, new UUIDType()),
                    DATE_TIME,
                    ENUM,
                    BIG_INTEGER,
                    BIG_DECIMAL,
                    STRING,
                    INT,
                    LONG,
                    DOUBLE,
                    BOOLEAN,
                    BYTE,
                    SHORT,
                    FLOAT,
                    CHAR
                    );

    public static final TypeMappings DEFAULT = new TypeMappings(DEFAULT_MAPPINGS);

    private final List<TypeMapping> types;

    public TypeMappings(Iterable<TypeMapping> types) {
        ImmutableList.Builder<TypeMapping> builder = ImmutableList.builder();
        this.types = builder.addAll(types).build();
    }

    public TypeMapping getTypeMapping(Optional<PropertyPath> path, LocalTypeDescriptor localTypeDescriptor) {
        for (TypeMapping valueType : types) {
            if (valueType.applies(path, localTypeDescriptor)) {
                return valueType;
            }
        }
        throw new IllegalArgumentException("ValueType not found for " + localTypeDescriptor);
    }

    public static class Builder {

        private final List<TypeMapping> defaultMappings;

        private final List<TypeMapping> mappings = Lists.newArrayList();

        private Predicate<FieldDescriptor> defaultFilter = ObjectTypeMapping.DEFAULT_FILTER;

        public Builder() {
            this(DEFAULT_MAPPINGS);
        }

        public Builder(List<TypeMapping> defaultMappings) {
            this.defaultMappings = Check.notNull(defaultMappings, "defaultMappings");
        }

        public Builder withMapping(TypeMapping mapping) {
            mappings.add(mapping);
            return this;
        }

        public Builder withDefaultFilter(Predicate<FieldDescriptor> defaultFilter) {
            this.defaultFilter = defaultFilter;
            return this;
        }

        public <R> HierarchyBuilder<R> withClass(Class<R> root) {
            return new HierarchyBuilder<>(root);
        }

        public <R> HierarchyBuilder<R> withClass(Class<R> root, String alias) {
            return new HierarchyBuilder<>(root, alias);
        }

        public TypeMappings build() {
            return new TypeMappings(Iterables.concat(mappings, defaultMappings));
        }

        public final class HierarchyBuilder<R> {

            protected PropertyPath targetPath;

            protected BiMap<String, TypeDescriptor> typesByAlias = HashBiMap.create();

            private Predicate<FieldDescriptor> filter = defaultFilter;

            public HierarchyBuilder(Class<R> root) {
                this(root, null);
            }

            public HierarchyBuilder(Class<R> root, String alias) {
                Check.notNull(root, "root");
                register(root, alias);
            }

            private TypeDescriptor getTypeDescriptor(Class<?> clazz) {
                return TypeDescriptors.DEFAULT.getTypeDescriptor.apply(clazz);
            }

            public Builder withMapping(TypeMapping typeMapping) {
                return register().withMapping(typeMapping);
            }

            public <N> HierarchyBuilder<N> withClass(Class<N> root) {
                return register().withClass(root);
            }

            public <N> HierarchyBuilder<N> withClass(Class<N> root, String alias) {
                return register().withClass(root, alias);
            }

            @SafeVarargs
            public final HierarchyBuilder<R> havingSubClasses(Class<? extends R>... subClasses) {
                return havingSubClasses(ImmutableList.copyOf(subClasses));
            }

            public final HierarchyBuilder<R> havingSubClasses(Iterable<Class<? extends R>> subClasses) {
                for (Class<? extends R> clazz : subClasses) {
                    register(clazz, null);
                }
                return this;
            }

            public HierarchyBuilder<R> havingSubClass(Class<? extends R> clazz, String alias) {
                register(clazz, alias);
                return this;
            }

            public HierarchyBuilder<R> asReferenceForPath(String targetPath) {
                Check.notNull(targetPath, "targetPath");
                return asReferenceForPath(PropertyPath.parse(targetPath));
            }

            public HierarchyBuilder<R> asReferenceForPath(PropertyPath targetPath) {
                this.targetPath = Check.notNull(targetPath, "targetPath");
                return this;
            }

            public HierarchyBuilder<R> withFilter(Predicate<FieldDescriptor> filter) {
                this.filter = filter;
                return this;
            }

            private String register(Class<?> clazz, String alias) {
                return register(getTypeDescriptor(clazz), alias);
            }

            private String register(TypeDescriptor type, String alias) {
                alias = ObjectTypeMapping.getAlias(alias, type);
                typesByAlias.put(alias, type);
                return alias;
            }

            Builder register() {
                Builder builder = Builder.this;
                ObjectTypeMapping<R> objectTypeMapping = new ObjectTypeMapping<>(typesByAlias, filter);
                if (targetPath != null) {
                    // NOTE: ReferenceTypeMapping has higher priority and thus must be registered before ObjectTypeMapping
                    builder = builder.withMapping(new ReferenceTypeMapping(targetPath, objectTypeMapping));
                }
                return builder.withMapping(objectTypeMapping);
            }

            public TypeMappings build() {
                return register().build();
            }

        }

    }
}
