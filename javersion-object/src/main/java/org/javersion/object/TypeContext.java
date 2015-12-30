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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.javersion.reflect.BeanProperty;
import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

@Immutable
public final class TypeContext {

    @Nullable
    public final ElementDescriptor parent;

    @Nonnull
    public final TypeDescriptor type;

    public TypeContext(TypeDescriptor typeDescriptor) {
        this(null, typeDescriptor);
    }

    public TypeContext(FieldDescriptor fieldDescriptor) {
        this(fieldDescriptor, fieldDescriptor.getType());
    }

    public TypeContext(BeanProperty beanProperty) {
        this(beanProperty.getReadMethod(), beanProperty.getType());
    }

    public TypeContext(ElementDescriptor parent, TypeDescriptor typeDescriptor) {
        this.parent = parent;
        this.type = Check.notNull(typeDescriptor, "typeDescriptor");
    }

    public int hashCode() {
        return 31* type.hashCode() + (parent != null ? parent.hashCode() : 0);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof TypeContext) {
            TypeContext key = (TypeContext) obj;
            return this.type.equals(key.type)
                    && Objects.equals(this.parent, key.parent);
        } else {
            return false;
        }
    }

}