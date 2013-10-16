package org.javersion.object.basic;

import org.javersion.object.ObjectDescriptor;
import org.javersion.object.SerializationContext;

public class BasicSerializationContext extends SerializationContext<Object> {

    public BasicSerializationContext(ObjectDescriptor<Object> rootDescriptor) {
        super(rootDescriptor);
    }

}
