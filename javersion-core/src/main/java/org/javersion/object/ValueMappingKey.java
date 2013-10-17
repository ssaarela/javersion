package org.javersion.object;

import javax.annotation.Nullable;

import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;
import org.javersion.util.Check;

import com.google.common.base.Objects;

public final class ValueMappingKey {
    
    @Nullable 
    public final ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> parent;
    
    public final TypeDescriptor typeDescriptor;

    public ValueMappingKey(TypeDescriptor typeDescriptor) {
        this(null, typeDescriptor);
    }

    public ValueMappingKey(ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> parent, TypeDescriptor typeDescriptor) {
        this.parent = parent;
        this.typeDescriptor = Check.notNull(typeDescriptor, "typeDescriptor");
    }
    
    public int hashCode() {
        return 31*typeDescriptor.hashCode() + (parent != null ? parent.hashCode() : 0);
    }
    
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ValueMappingKey) {
            ValueMappingKey key = (ValueMappingKey) obj; 
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