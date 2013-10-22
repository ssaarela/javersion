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

import java.util.List;

import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public abstract class AbstractValueTypes<V> implements ValueTypes<V> {

    private final Iterable<ValueTypeFactory<V>> types;
    
    public AbstractValueTypes(Iterable<ValueTypeFactory<V>> types) {
        this.types = types;
    }

    @Override
    public ValueTypeFactory<V> getFactory(ValueMappingKey mappingKey) {
        for (ValueTypeFactory<V> valueType : types) {
            if (valueType.applies(mappingKey)) {
                return valueType;
            }
        }
        throw new IllegalArgumentException("ValueType not found for " + mappingKey);
    }

    public static abstract class Builder<V, T extends ValueTypes<V>, B extends Builder<V, T, B>> {
        
        protected final TypeDescriptors typeDescriptors;
        
        protected final List<ValueTypeFactory<V>> factories = Lists.newArrayList();
        
        protected Builder(TypeDescriptors typeDescriptors) {
            this.typeDescriptors = typeDescriptors;
        }
        
        public B withFactory(ValueTypeFactory<V> factory) {
            factories.add(factory);
            return self();
        }
        
        public <R> HierarchyBuilder<R> withClass(Class<R> root) {
            return new HierarchyBuilder<>(root);
        }

        public ValueTypes<V> build() {
            return build(factories);
        }
        

        protected abstract B self();
        
        protected abstract ValueTypeFactory<V> createObjectTypeFactory(
                Iterable<TypeDescriptor> types,
                IdMapper<?> idMapper,
                String alias);
        
        protected abstract ValueTypes<V> build(List<ValueTypeFactory<V>> factories);

        
        
        public final class HierarchyBuilder<R> extends ObjectTypeBuilder<R, HierarchyBuilder<R>> {

            public HierarchyBuilder(Class<R> root) {
                super(root);
            }

            @Override
            protected HierarchyBuilder<R> self() {
                return this;
            }
            

            public B withFactory(ValueTypeFactory<V> factory) {
                return register().withFactory(factory);
            }
            public <N> HierarchyBuilder<N> withClass(Class<N> root) {
                return register().withClass(root);
            }
            public ValueTypes<V> build() {
                return register().build();
            }

            
            B register() {
                Iterable<TypeDescriptor> types = Iterables.transform(classes, typeDescriptors.getTypeDescriptor);
                return Builder.this.withFactory(createObjectTypeFactory(types, idMapper, alias));
            }

        }
        
    }
}
