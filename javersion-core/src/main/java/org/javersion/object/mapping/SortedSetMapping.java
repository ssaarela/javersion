package org.javersion.object.mapping;

import java.util.SortedSet;

import org.javersion.object.types.IdentifiableType;
import org.javersion.object.types.SortedSetType;
import org.javersion.object.types.ValueType;

public class SortedSetMapping extends SetTypeMapping {

    public SortedSetMapping() {
        super(SortedSet.class);
    }

    @Override
    protected ValueType newSetType(IdentifiableType valueType) {
        return new SortedSetType(valueType);
    }
}
