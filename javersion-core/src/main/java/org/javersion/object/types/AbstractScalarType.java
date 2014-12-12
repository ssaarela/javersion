package org.javersion.object.types;

import org.javersion.object.ReadContext;
import org.javersion.path.PropertyTree;

public abstract class AbstractScalarType implements ScalarType, IdentifiableType {

    @Override
    public final void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {}

    @Override
    public final boolean isReference() {
        return false;
    }

    @Override
    public String toString(Object object) {
        return object.toString();
    }

}
