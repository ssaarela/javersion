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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.javersion.object.mapping.*;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TypeMappings {

    public static TypeMapping STRING = new StringTypeMapping();

    public static TypeMapping CHAR = new PrimitiveTypeMapping(Character.class, char.class);
    public static TypeMapping BYTE = new PrimitiveTypeMapping(Byte.class, byte.class);
    public static TypeMapping SHORT = new PrimitiveTypeMapping(Short.class, short.class);
    public static TypeMapping INTEGER = new PrimitiveTypeMapping(Integer.class, int.class);
    public static TypeMapping LONG = new PrimitiveTypeMapping(Long.class, long.class);
    public static TypeMapping FLOAT = new PrimitiveTypeMapping(Float.class, float.class);
    public static TypeMapping DOUBLE = new PrimitiveTypeMapping(Double.class, double.class);
    public static TypeMapping BOOLEAN = new PrimitiveTypeMapping(Boolean.class, boolean.class);

    public static TypeMapping BIG_INTEGER = new SimpleTypeMapping(BigInteger.class);
    public static TypeMapping BIG_DECIMAL = new SimpleTypeMapping(BigDecimal.class);

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
                    new SetTypeMapping(),
                    new MapTypeMapping(),
                    new CollectionTypeMapping(),
                    DATE_TIME,
                    ENUM,
                    BIG_INTEGER,
                    BIG_DECIMAL,
                    STRING,
                    INTEGER,
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
        this.types = ImmutableList.copyOf(types);
    }

    public TypeMapping getTypeMapping(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
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

        public <R> HierarchyBuilder<R> withClass(Class<R> root) {
            return new HierarchyBuilder<>(root);
        }

        public TypeMappings build() {
            return new TypeMappings(Iterables.concat(mappings, defaultMappings));
        }


        public final class HierarchyBuilder<R> {

            protected String alias;

            protected final Class<? extends R> rootType;

            protected Set<Class<? extends R>> classes = Sets.newHashSet();

            public HierarchyBuilder(Class<R> root) {
                this.rootType = root;
                classes.add(Check.notNull(root, "root"));
            }

            public Builder withTypeMapping(TypeMapping typeMapping) {
                return register().withMapping(typeMapping);
            }

            public <N> HierarchyBuilder<N> withClass(Class<N> root) {
                return register().withClass(root);
            }


            @SafeVarargs
            public final HierarchyBuilder<R> havingSubClasses(Class<? extends R>... subClasses) {
                return havingSubClasses(ImmutableList.copyOf(subClasses));
            }

            public final HierarchyBuilder<R> havingSubClasses(Collection<Class<? extends R>> subClasses) {
                classes.addAll(subClasses);
                return this;
            }

            public HierarchyBuilder<R> asReferenceWithAlias(String alias) {
                Check.notNullOrEmpty(alias, "alias");
                this.alias = alias;
                return this;
            }

            Builder register() {
                Builder builder = Builder.this;
                if (!isNullOrEmpty(alias)) {
                    builder = builder.withMapping(new ReferenceTypeMapping(rootType, alias));
                }
                Iterable<TypeDescriptor> types = Iterables.transform(classes, TypeDescriptors.DEFAULT.getTypeDescriptor);
                return builder.withMapping(new ObjectTypeMapping<>(rootType, types));
            }

            public TypeMappings build() {
                return register().build();
            }

        }

    }
}
