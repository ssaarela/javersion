package org.javersion.reflect;

import java.lang.reflect.Field;

import com.google.common.base.Predicate;
import com.google.common.reflect.TypeToken;

public class TypeDescriptors extends AbstractTypeDescriptors<FieldDescriptor, TypeDescriptor> {

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