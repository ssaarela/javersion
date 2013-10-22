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
package org.javersion.object.basic;

import java.util.List;

import org.javersion.object.AbstractValueTypes;
import org.javersion.object.IdMapper;
import org.javersion.object.ValueTypeFactory;
import org.javersion.object.ValueTypes;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class BasicValueTypes extends AbstractValueTypes<Object> {
    
    public static Builder builder() {
        return new Builder();
    }
    
    private static List<ValueTypeFactory<Object>> DEFAULT_FACTORIES = ImmutableList.of(
                PrimitivesType.FACTORY,
                new VersionableTypeFactory()
                );

    public BasicValueTypes() {
        super(DEFAULT_FACTORIES);
    }
    
    public BasicValueTypes(Iterable<ValueTypeFactory<Object>> factories) {
        this(TypeDescriptors.DEFAULT, factories);
    }
    
    public BasicValueTypes(TypeDescriptors typeDescriptors, Iterable<ValueTypeFactory<Object>> factories) {
        super(Iterables.concat(factories, DEFAULT_FACTORIES));
    }

    public static class Builder extends AbstractValueTypes.Builder<Object, ValueTypes<Object>, Builder> {

        public Builder() {
            super(TypeDescriptors.DEFAULT);
        }
        
        public Builder(TypeDescriptors typeDescriptors) {
            super(typeDescriptors);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected ValueTypeFactory<Object> createObjectTypeFactory(
                Iterable<TypeDescriptor> types, 
                IdMapper<?> idMapper,
                String alias) {
            return new ObjectTypeFactory(types, idMapper, alias);
        }

        @Override
        protected ValueTypes<Object> build(List<ValueTypeFactory<Object>> factories) {
            return new BasicValueTypes(factories);
        }
        
    }
}
