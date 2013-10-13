package org.javersion.reflect;

import java.lang.reflect.Field;

public class FieldDescriptor extends AbstractFieldDescriptor<FieldDescriptor, TypeDescriptor> {

    public FieldDescriptor(AbstractTypeDescriptors<FieldDescriptor, TypeDescriptor> typeDescriptors, Field field) {
        super(typeDescriptors, field);
    }

}