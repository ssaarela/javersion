package org.javersion.object.mapping;

import java.util.SortedMap;

import org.javersion.object.types.IdentifiableType;
import org.javersion.object.types.ScalarType;
import org.javersion.object.types.SortedMapType;
import org.javersion.object.types.ValueType;

public class SortedMapMapping extends MapTypeMapping {

    public SortedMapMapping() {
        super(SortedMap.class);
    }

    @Override
    protected ValueType newMapType(ScalarType keyType) {
        return new SortedMapType(keyType);
    }

}
