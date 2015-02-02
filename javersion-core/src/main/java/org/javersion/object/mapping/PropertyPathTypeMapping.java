package org.javersion.object.mapping;

import org.javersion.object.DescribeContext;
import org.javersion.object.LocalTypeDescriptor;
import org.javersion.object.types.PropertyPathType;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;

public class PropertyPathTypeMapping implements TypeMapping {

    @Override
    public boolean applies(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
        return localTypeDescriptor.typeDescriptor.getRawType().equals(PropertyPath.class);
    }

    @Override
    public ValueType describe(PropertyPath path, TypeDescriptor type, DescribeContext context) {
        return new PropertyPathType();
    }

}
