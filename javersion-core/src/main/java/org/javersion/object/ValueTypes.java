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

import static org.javersion.object.BasicValueTypeMapping.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ValueTypes {
    
    public static Builder builder() {
        return new Builder();
    }

    public static final List<TypeMapping> DEFAULT_MAPPINGS = 
            ImmutableList.<TypeMapping>of(
                    BYTE,
                    SHORT,
                    INTEGER,
                    LONG,
                    FLOAT,
                    DOUBLE,
                    BOOLEAN,
                    CHAR,
                    STRING,
                    new VersionableTypeMapping()
                    );
    
    public static final ValueTypes DEFAULT = new ValueTypes(DEFAULT_MAPPINGS);
    
    private final List<TypeMapping> types;
    
    public ValueTypes(Iterable<TypeMapping> types) {
        this.types = ImmutableList.copyOf(types);
    }

    public TypeMapping getMapping(TypeMappingKey mappingKey) {
        for (TypeMapping valueType : types) {
            if (valueType.applies(mappingKey)) {
                return valueType;
            }
        }
        throw new IllegalArgumentException("ValueType not found for " + mappingKey);
    }

    public static class Builder {
        
        protected final List<TypeMapping> factories = Lists.newArrayList(DEFAULT_MAPPINGS);
        
        public Builder withMapping(TypeMapping mapping) {
            factories.add(mapping);
            return this;
        }
        
        public <R> HierarchyBuilder<R> withClass(Class<R> root) {
            return new HierarchyBuilder<>(root);
        }

        public ValueTypes build() {
            return new ValueTypes(Lists.reverse(factories));
        }
        
        
        public final class HierarchyBuilder<R> {
            
            protected String alias;
            
            protected final Class<? extends R> rootType;
            
            protected Set<Class<? extends R>> classes = Sets.newHashSet();

            protected IdMapper<R> idMapper;

            public HierarchyBuilder(Class<R> root) {
                this.rootType = root;
                classes.add(Check.notNull(root, "root"));
                alias = Check.notNull(root.getCanonicalName(), "root.getCanonicalName()");
            }

            public Builder withFactory(TypeMapping factory) {
                return register().withMapping(factory);
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
            
            public HierarchyBuilder<R> havingIdMapper(IdMapper<R> idMapper) {
                Check.notNull(idMapper, "idMapper");
                this.idMapper = idMapper;
                return this;
            }

            public HierarchyBuilder<R> havingAlias(String alias) {
                Check.notNullOrEmpty(alias, "alias");
                this.alias = alias;
                return this;
            }
            
            Builder register() {
                Iterable<TypeDescriptor> types = Iterables.transform(classes, TypeDescriptors.DEFAULT.getTypeDescriptor);
                return Builder.this.withMapping(new ObjectMapping<R>(rootType, types, idMapper, alias));
            }

            public ValueTypes build() {
                return register().build();
            }

        }
        
    }
}
