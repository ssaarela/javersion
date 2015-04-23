package org.javersion.object.mapping;

import java.util.Optional;

import org.javersion.object.DescribeContext;
import org.javersion.object.LocalTypeDescriptor;
import org.javersion.object.types.PropertyPathType;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;

public class PropertyPathTypeMapping implements TypeMapping {

    @Override
    public boolean applies(Optional<PropertyPath> path, LocalTypeDescriptor localTypeDescriptor) {
        return localTypeDescriptor.typeDescriptor.getRawType().equals(PropertyPath.class);
    }

    @Override
    public ValueType describe(Optional<PropertyPath> path, TypeDescriptor type, DescribeContext context) {
        return new PropertyPathType();
    }

}
