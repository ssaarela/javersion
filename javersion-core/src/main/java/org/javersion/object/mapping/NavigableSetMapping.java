package org.javersion.object.mapping;

import java.util.NavigableSet;

import org.javersion.object.types.IdentifiableType;
import org.javersion.object.types.NavigableSetType;
import org.javersion.object.types.ValueType;

public class NavigableSetMapping extends SetTypeMapping {

    public NavigableSetMapping() {
        super(NavigableSet.class);
    }

    @Override
    protected ValueType newSetType(IdentifiableType valueType) {
        return new NavigableSetType(valueType);
    }
}
