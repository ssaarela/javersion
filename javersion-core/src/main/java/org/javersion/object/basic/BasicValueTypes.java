package org.javersion.object.basic;

import java.util.List;

import org.javersion.object.AbstractEntityType;
import org.javersion.object.ValueType;
import org.javersion.object.ValueTypes;
import org.javersion.reflect.TypeDescriptors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class BasicValueTypes extends ValueTypes<Object> {
    
    private static final TypeDescriptors TYPE_DESCRIPTORS = new TypeDescriptors();
    
    private static final List<ValueType<Object>> DEFAULTS = ImmutableList.<ValueType<Object>>of(
            new AbstractEntityType<Object>(TYPE_DESCRIPTORS) {
                @Override
                protected Object toValue(Object object) {
                    return object.getClass();
                }
            }
    );

    public BasicValueTypes(Iterable<ValueType<Object>> types) {
        super(Iterables.concat(types, DEFAULTS));
    }

}
