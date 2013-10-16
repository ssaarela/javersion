package org.javersion.object;

import java.util.Map;

import javax.annotation.Nullable;

import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;

public interface ValueType<V> {
    
    void serialize(SerializationContext<V> context);
    
    Map<String, ObjectDescriptor<V>> describe(
            @Nullable ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> parent, 
            TypeDescriptor typeDescriptor,
            ObjectDescriptors<V> objectDescriptors);

    boolean applies(
            @Nullable ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> parent, 
            TypeDescriptor typeDescriptor);

}
