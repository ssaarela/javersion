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

import javax.annotation.Nullable;

import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

import com.google.common.base.Objects;

public final class LocalTypeDescriptor {
    
    @Nullable 
    private final ElementDescriptor<?, ?, ?> parent;
    
    public final TypeDescriptor typeDescriptor;

    public LocalTypeDescriptor(TypeDescriptor typeDescriptor) {
        this(null, typeDescriptor, true);
    }
    
    public LocalTypeDescriptor(FieldDescriptor fieldDescriptor) {
        this(fieldDescriptor, fieldDescriptor.getType(), true);
    }

    public LocalTypeDescriptor(TypeDescriptor parentType, TypeDescriptor typeDescriptor) {
        this(parentType, typeDescriptor, true);
    }

    private LocalTypeDescriptor(ElementDescriptor<?, ?, ?> parent, TypeDescriptor typeDescriptor, boolean x) {
        this.parent = parent;
        this.typeDescriptor = Check.notNull(typeDescriptor, "typeDescriptor");
    }
    
    public int hashCode() {
        return 31*typeDescriptor.hashCode() + (parent != null ? parent.hashCode() : 0);
    }
    
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof LocalTypeDescriptor) {
            LocalTypeDescriptor key = (LocalTypeDescriptor) obj; 
            return this.typeDescriptor.equals(key.typeDescriptor)
                    && Objects.equal(this.parent, key.parent);
        } else {
            return false;
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (parent != null) {
            sb.append(parent).append(": ");
        }
        sb.append(typeDescriptor);
        return sb.toString();
    }
}