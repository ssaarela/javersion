package org.javersion.object.types;

import org.javersion.reflect.AccessibleProperty;
import org.javersion.util.Check;

public class ObjectIdentifier {

    public final String name;

    public final AccessibleProperty property;

    public final IdentifiableType idType;

    public ObjectIdentifier(AccessibleProperty property, IdentifiableType idType, String name) {
        this.property = Check.notNull(property, "property");
        this.idType = Check.notNull(idType, "idType");
        this.name = Check.notNullOrEmpty(name, "name");
    }

}
