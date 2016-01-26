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

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.javersion.object.mapping.PrimitiveTypeMapping.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.concurrent.Immutable;

import org.javersion.core.Revision;
import org.javersion.object.mapping.*;
import org.javersion.object.types.PropertyPathType;
import org.javersion.object.types.UUIDType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

@Immutable
public final class TypeMappings {

    public static Builder builder() {
        return new Builder();
    }

    public static final TypeMapping STRING = new StringTypeMapping();
    public static final TypeMapping BIG_INTEGER = new ToStringTypeMapping(BigInteger.class);
    public static final TypeMapping BIG_DECIMAL = new ToStringTypeMapping(BigDecimal.class);
    public static final TypeMapping ENUM = new EnumTypeMapping();

    public static final MappingResolver DEFAULT_RESOLVER;
    public static final List<TypeMapping> DEFAULT_MAPPINGS;

    static {
        ImmutableList.Builder<TypeMapping> mappings = ImmutableList.builder();

        mappings.add(new VersionableReferenceTypeMapping());
        mappings.add(new VersionableTypeMapping());
        mappings.add(new ListTypeMapping());
        mappings.add(new NavigableSetMapping());
        mappings.add(new SortedSetMapping());
        mappings.add(new SetTypeMapping());
        mappings.add(new NavigableMapMapping());
        mappings.add(new SortedMapMapping());
        mappings.add(new MapTypeMapping());
        mappings.add(new CollectionTypeMapping());
        mappings.add(new ToStringTypeMapping(Revision.class));
        mappings.add(new SimpleValueMapping(PropertyPath.class, new PropertyPathType()));
        mappings.add(new SimpleValueMapping(UUID.class, new UUIDType()));
        mappings.add(new InstantMapping());
        mappings.add(new LocalDateMapping());
        mappings.add(new LocalDateTimeMapping());
        mappings.add(ENUM);
        mappings.add(BIG_INTEGER);
        mappings.add(BIG_DECIMAL);
        mappings.add(STRING);
        mappings.add(INT);
        mappings.add(LONG);
        mappings.add(DOUBLE);
        mappings.add(BOOLEAN);
        mappings.add(BYTE);
        mappings.add(SHORT);
        mappings.add(FLOAT);
        mappings.add(CHAR);

        if (classFound("org.joda.time.DateTime")) {
            mappings.add(new JodaDateTimeMapping());
            mappings.add(new JodaLocalDateMapping());
        }

        mappings.add(new DelegateTypeMapping());
        DEFAULT_MAPPINGS = mappings.build();

        List<MappingResolver> resolvers = new ArrayList<>();
        resolvers.add(new JaversionMappingResolver());
        if (classFound("com.fasterxml.jackson.annotation.JacksonAnnotation")) {
            resolvers.add(new JacksonMappingResolver());
        }
        resolvers.add(new DefaultMappingResolver());

        DEFAULT_RESOLVER = new CompositeMappingResolver(resolvers);
    }

    public static final TypeDescriptors DEFAULT_TYPES = new TypeDescriptors(member -> !member.isSynthetic());
    public static final TypeMappings DEFAULT = new Builder().build();


    private final List<TypeMapping> types;

    private final TypeDescriptors typeDescriptors;

    private final MappingResolver mappingResolver;

    private TypeMappings(Builder builder) {
        this.typeDescriptors = builder.getTypeDescriptors();
        this.mappingResolver = builder.getMappingResolver();
        this.types = ImmutableList.copyOf(builder.getTypeMappings());
    }

    public TypeDescriptor getTypeDescriptor(Class<?> cls) {
        return typeDescriptors.get(cls);
    }

    public TypeDescriptor getTypeDescriptor(TypeToken<?> typeToken) {
        return typeDescriptors.get(typeToken);
    }

    public Iterable<TypeMapping> getTypeMappings() {
        return types;
    }

    public MappingResolver getMappingResolver() {
        return mappingResolver;
    }

    public final static class Builder {

        private List<TypeMapping> defaultMappings;

        private TypeDescriptors typeDescriptors;

        private MappingResolver mappingResolver;

        private final List<TypeMapping> mappings = Lists.newArrayList();

        public Builder withMapping(TypeMapping mapping) {
            mappings.add(mapping);
            return this;
        }

        public Builder withDefaultMappings(List<TypeMapping> defaultMappings) {
            if (this.defaultMappings != null) {
                throw new IllegalStateException("defaultMappings already set");
            }
            this.defaultMappings = defaultMappings;
            return this;
        }

        public Builder withTypeDescriptors(TypeDescriptors typeDescriptors) {
            if (this.typeDescriptors != null) {
                throw new IllegalStateException("typeDescriptors already set");
            }
            this.typeDescriptors = typeDescriptors;
            return this;
        }

        public Builder withMappingResolvers(MappingResolver... mappingResolvers) {
            return withMappingResolver(new CompositeMappingResolver(mappingResolvers));
        }

        public Builder withMappingResolver(MappingResolver mappingResolver) {
            if (this.mappingResolver != null) {
                throw new IllegalStateException("mappingResolver already set");
            }
            this.mappingResolver = mappingResolver;
            return this;
        }

        public <R> HierarchyBuilder<R> withClass(Class<R> root) {
            return new HierarchyBuilder<>(root);
        }

        public <R> HierarchyBuilder<R> withClass(Class<R> root, String alias) {
            return new HierarchyBuilder<>(root, alias);
        }

        public TypeMappings build() {
            return new TypeMappings(this);
        }

        public final class HierarchyBuilder<R> {

            protected PropertyPath targetPath;

            protected Map<String, TypeDescriptor> typesByAlias = new LinkedHashMap<>();

            public HierarchyBuilder(Class<R> root) {
                this(root, null);
            }

            public HierarchyBuilder(Class<R> root, String alias) {
                Check.notNull(root, "root");
                register(root, alias);
            }

            private TypeDescriptor getTypeDescriptor(Class<?> clazz) {
                return getTypeDescriptors().get(clazz);
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

            private String register(Class<?> clazz, String alias) {
                return register(getTypeDescriptor(clazz), alias);
            }

            private String register(TypeDescriptor type, String alias) {
                if (isNullOrEmpty(alias)) {
                    alias = Check.notNull(getMappingResolver().alias(type), "alias").value;
                }
                typesByAlias.put(alias, type);
                return alias;
            }

            Builder register() {
                Builder builder = Builder.this;
                ObjectTypeMapping<R> objectTypeMapping = new ObjectTypeMapping<>(typesByAlias);
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

        private TypeDescriptors getTypeDescriptors() {
            if (typeDescriptors == null) {
                typeDescriptors = DEFAULT_TYPES;
            }
            return typeDescriptors;
        }

        private MappingResolver getMappingResolver() {
            if (mappingResolver == null) {
                mappingResolver = DEFAULT_RESOLVER;
            }
            return mappingResolver;
        }

        private List<TypeMapping> getDefaultMappings() {
            if (defaultMappings == null) {
                defaultMappings = DEFAULT_MAPPINGS;
            }
            return defaultMappings;
        }

        private Iterable<TypeMapping> getTypeMappings() {
            return Iterables.concat(mappings, getDefaultMappings());
        }

    }

    public static boolean classFound(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
