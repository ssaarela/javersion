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
import java.lang.reflect.Modifier;

import org.javersion.util.Check;

public abstract class AbstractFieldDescriptor<
            F extends AbstractFieldDescriptor<F, T, Ts>,
            T extends AbstractTypeDescriptor<F, T, Ts>,
            Ts extends AbstractTypeDescriptors<F, T, Ts>>
        extends ElementDescriptor<F, T, Ts> {

    protected final Field field;

    public AbstractFieldDescriptor(Ts typeDescriptors, Field field) {
        super(typeDescriptors);
        this.field = Check.notNull(field, "field");
        field.setAccessible(true);
    }

    public Object getStatic() {
        return get(null);
    }

    public Object get(Object obj) {
        try {
            return field.get(obj);
        } catch (RuntimeException e) {
            throw e;
        } catch (IllegalAccessException e) {
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

    public boolean isTransient() {
        return Modifier.isTransient(field.getModifiers());
    }

    public T getType() {
        return typeDescriptors.get(field.getGenericType());
    }

    @Override
    public Field getElement() {
        return field;
    }

    public final int hashCode() {
        return 31 * typeDescriptors.hashCode() + field.hashCode();
    }

    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof AbstractFieldDescriptor) {
            @SuppressWarnings("unchecked")
            F other = (F) obj;
            return this.typeDescriptors.equals(other.typeDescriptors)
                    && field.equals(other.field);
        } else {
            return false;
        }
    }

    public String getName() {
        return field.getName();
    }

    public String toString() {
        return field.getDeclaringClass().getCanonicalName() + "." + getName();
    }

    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }
}
