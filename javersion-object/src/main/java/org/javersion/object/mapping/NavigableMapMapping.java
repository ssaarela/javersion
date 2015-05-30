package org.javersion.object.mapping;

import java.util.NavigableMap;

import org.javersion.object.types.ScalarType;
import org.javersion.object.types.SortedMapType;
import org.javersion.object.types.ValueType;

public class NavigableMapMapping extends MapTypeMapping {

    public NavigableMapMapping() {
        super(NavigableMap.class);
    }

    @Override
    protected ValueType newMapType(ScalarType keyType) {
        return new SortedMapType(keyType);
    }

}
