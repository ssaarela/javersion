package org.javersion.object.types;

import java.util.Set;
import java.util.TreeSet;

public class NavigableSetType extends SetType {

    public NavigableSetType(IdentifiableType identifiableType) {
        super(identifiableType);
    }

    @Override
    protected Set<Object> newSet(int size) {
        return new TreeSet<>();
    }

}
