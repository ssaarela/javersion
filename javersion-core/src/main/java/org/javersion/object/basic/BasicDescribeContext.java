package org.javersion.object.basic;

import static org.javersion.reflect.TypeDescriptors.getTypeDescriptor;

import org.javersion.object.DescribeContext;
import org.javersion.object.ValueMapping;

public class BasicDescribeContext extends DescribeContext<Object> {
    
    public BasicDescribeContext() {
        this(new BasicValueTypes());
    }
    public BasicDescribeContext(BasicValueTypes valueTypes) {
        super(valueTypes);
    }
    
    public static ValueMapping<Object> describe(Class<?> clazz) {
        return new BasicDescribeContext().describe(getTypeDescriptor(clazz));
    }

}
