package org.javersion.reflect;

import com.google.common.reflect.TypeToken;

public class TypeDescriptor extends AbstractTypeDescriptor<FieldDescriptor, TypeDescriptor>{

    public TypeDescriptor(AbstractTypeDescriptors<FieldDescriptor, TypeDescriptor> typeDescriptors, TypeToken<?> typeToken) {
        super(typeDescriptors, typeToken);
    }
    
}