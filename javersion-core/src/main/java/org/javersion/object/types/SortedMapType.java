package org.javersion.object.types;

import java.util.Map;
import java.util.TreeMap;

public class SortedMapType extends MapType {

    public SortedMapType(IdentifiableType keyType) {
        super(keyType);
    }

    @Override
    protected Map<Object, Object> newMap(int size) {
        return new TreeMap<>();
    }
}
