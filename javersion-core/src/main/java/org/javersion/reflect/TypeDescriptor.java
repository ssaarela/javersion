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
package org.javersion.reflect;

import org.javersion.util.Check;

import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;

public class TypeDescriptor extends AbstractTypeDescriptor<
            FieldDescriptor,
            TypeDescriptor,
            TypeDescriptors>{

    public static final Function<TypeDescriptor, Class<?>> getRawType = new Function<TypeDescriptor, Class<?>>() {
        @Override
        public Class<?> apply(TypeDescriptor input) {
            Check.notNull(input, "input");
            return input.getRawType();
        }
    };

    public TypeDescriptor(TypeDescriptors typeDescriptors, TypeToken<?> typeToken) {
        super(typeDescriptors, typeToken);
    }

}