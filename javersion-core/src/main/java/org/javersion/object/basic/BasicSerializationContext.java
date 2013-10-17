package org.javersion.object.basic;

import org.javersion.object.ValueMapping;
import org.javersion.object.SerializationContext;

public class BasicSerializationContext extends SerializationContext<Object> {

    public BasicSerializationContext(ValueMapping<Object> rootDescriptor) {
        super(rootDescriptor);
    }

}
