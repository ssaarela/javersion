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

import java.lang.reflect.Field;

import org.javersion.util.Check;

import com.google.common.base.Predicate;
import com.google.common.reflect.TypeToken;

public class TypeDescriptors extends AbstractTypeDescriptors<FieldDescriptor, TypeDescriptor, TypeDescriptors> {

    public static final TypeDescriptors DEFAULT = new TypeDescriptors();
    
    public static TypeDescriptor getTypeDescriptor(Class<?> clazz) {
        Check.notNull(clazz, "clazz");
        return DEFAULT.get(clazz);
    }
    
    public TypeDescriptors() {
        super();
    }
    
    public TypeDescriptors(Predicate<? super Field> fieldFilter) {
        super(fieldFilter);
    }

    @Override
    public FieldDescriptor newFieldDescriptor(Field field) {
        return new FieldDescriptor(this, field);
    }

    @Override
    protected TypeDescriptor newTypeDescriptor(TypeToken<?> typeToken) {
        return new TypeDescriptor(this, typeToken);
    }

}