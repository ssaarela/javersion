package org.javersion.object.basic;

import java.util.Map;

import org.javersion.object.DeserializationContext;
import org.javersion.object.RootMapping;
import org.javersion.path.PropertyPath;

public class BasicDeserializationContext extends DeserializationContext<Object> {

    protected BasicDeserializationContext(RootMapping<Object> rootMapping, Map<PropertyPath, Object> properties) {
        super(rootMapping, properties);
    }

}
