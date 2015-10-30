package org.javersion.object.mapping;

import org.javersion.object.LocalTypeDescriptor;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

public class SimpleValueMapping implements TypeMapping {

    public final Class<?> type;

    public final ValueType valueType;

    public SimpleValueMapping(Class<?> type, ValueType valueType) {
        this.type = Check.notNull(type, "type");
        this.valueType = Check.notNull(valueType, "valueType");
    }

    @Override
    public boolean applies(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
        TypeDescriptor typeDescriptor = localTypeDescriptor.typeDescriptor;
        return typeDescriptor.getRawType().equals(type);
    }

    @Override
    public ValueType getValueType() {
        return valueType;
    }

}
