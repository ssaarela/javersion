package org.javersion.object;

import java.util.Iterator;
import java.util.Map;

import org.javersion.util.Check;

import com.google.common.collect.ImmutableMap;

public class ObjectDescriptor<V> {
    
    public final ValueType<V> valueType;
    
    public final Map<String, ObjectDescriptor<V>> children;

    public ObjectDescriptor(ValueType<V> valueType, Map<String, ObjectDescriptor<V>> children) {
        this.valueType = Check.notNull(valueType, "valueType");
        this.children = ImmutableMap.copyOf(children);
    }

    public ObjectDescriptor<V> getChild(String name) {
        return children.get(name);
    }
    
    public ObjectDescriptor<V> get(PropertyPath path) {
        Check.notNull(path, "path");
        Iterator<PropertyPath> iter = path.toSchemaPath().iterator();
        PropertyPath parent = iter.next(); // Root
        ObjectDescriptor<V> result = this; // Root
        while (iter.hasNext()) {
            parent = iter.next();
            result = result.getChild(parent.getName());
        }
        return result;
    }
    
}
