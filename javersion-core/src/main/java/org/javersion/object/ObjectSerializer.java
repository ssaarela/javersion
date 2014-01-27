package org.javersion.object;

import java.util.Map;

import org.javersion.path.PropertyPath;

public interface ObjectSerializer<B, V> {

    public Map<PropertyPath, V> toMap(B object);

    public B fromMap(Map<PropertyPath, V> properties);

}
