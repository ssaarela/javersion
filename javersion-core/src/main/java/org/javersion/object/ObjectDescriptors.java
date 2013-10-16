package org.javersion.object;

import java.util.Map;

import javax.annotation.Nullable;

import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;
import org.javersion.util.Check;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class ObjectDescriptors<V> {
    
    private final Object lock = new Object();
    
    private static class ObjectDescriptorKey {
        @Nullable 
        public final ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> parent;
        
        public final TypeDescriptor typeDescriptor;

        public ObjectDescriptorKey(ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> parent, TypeDescriptor typeDescriptor) {
            this.parent = parent;
            this.typeDescriptor = Check.notNull(typeDescriptor, "typeDescriptor");
        }
        
        public int hashCode() {
            return 31*typeDescriptor.hashCode() + (parent != null ? parent.hashCode() : 0);
        }
        
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof ObjectDescriptorKey) {
                ObjectDescriptorKey key = (ObjectDescriptorKey) obj; 
                return this.typeDescriptor.equals(key.typeDescriptor)
                        && Objects.equal(this.parent, key.parent);
            } else {
                return false;
            }
        }
    }
    
    private final Map<ObjectDescriptorKey, ObjectDescriptor<V>> descriptors = Maps.newHashMap();

    private final ValueTypes<V> valueTypes;
    
    public ObjectDescriptors(ValueTypes<V> valueTypes) {
        this.valueTypes = valueTypes;
    }

    public ObjectDescriptor<V> describe(
            @Nullable ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> parent, 
            TypeDescriptor typeDescriptor) {
        synchronized (lock) {
            ObjectDescriptorKey key = new ObjectDescriptorKey(parent, typeDescriptor);
            ObjectDescriptor<V> objectDescriptor = descriptors.get(key);
            if (objectDescriptor == null) {
                ValueType<V> valueType = valueTypes.get(parent, typeDescriptor);
                Map<String, ObjectDescriptor<V>> children = valueType.describe(parent, typeDescriptor, this);
                objectDescriptor = new ObjectDescriptor<V>(valueType, children);
                descriptors.put(key, objectDescriptor);
            }
            return objectDescriptor;
        }
    }
    
}
