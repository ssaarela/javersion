package org.javersion.object.types;

import java.util.Map;
import java.util.TreeMap;

public class SortedMapType extends MapType {

    public SortedMapType(ScalarType keyType) {
        super(keyType);
    }

    @Override
    protected Map<Object, Object> newMap(int size) {
        return new TreeMap<>();
    }
}
