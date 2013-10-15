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

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;

public abstract class AbstractFieldDescriptor<F extends AbstractFieldDescriptor<F, T>, T extends AbstractTypeDescriptor<F, T>> 
        extends AbstractElement {
    
    private final AbstractTypeDescriptors<F, T> typeDescriptors;
    
    private final Field field;

    public AbstractFieldDescriptor(AbstractTypeDescriptors<F, T> typeDescriptors, Field field) {
        this.typeDescriptors = checkNotNull(typeDescriptors, "typeDescriptors");
        this.field = checkNotNull(field, "field");
        field.setAccessible(true);
    }
    
    public Object getStatic() {
        return get(null);
    }
    
    public Object get(Object obj) {
        try {
            return field.get(obj);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }
    
    public void setStatic(Object value) {
        set(null, value);
    }
    
    public void set(Object obj, Object value) {
        try {
            field.set(obj, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }

    public T getType() {
        return typeDescriptors.get(field.getGenericType());
    }
    
    @Override
    public Field getElement() {
        return field;
    }
    
}
