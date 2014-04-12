package org.javersion.object;

import java.util.List;

import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;

public class ListTypeMapping implements TypeMapping {

    @Override
    public boolean applies(TypeMappingKey mappingKey) {
        return mappingKey.typeDescriptor.getRawType().equals(List.class);
    }

    @Override
    public ValueType describe(DescribeContext context) {
        // Describe element type
        TypeDescriptor listType = context.getCurrentMappingKey().typeDescriptor;
        PropertyPath path = context.getCurrentPath();
        TypeDescriptor elementType = listType.resolveGenericParameter(List.class, 0);
        context.describe(path.index(""), new TypeMappingKey(listType, elementType));
        
        return new ListType();
    }

}
