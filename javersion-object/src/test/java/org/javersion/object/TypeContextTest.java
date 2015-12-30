package org.javersion.object;

import static org.javersion.reflect.TypeDescriptors.DEFAULT;

import java.util.Set;

import org.javersion.reflect.TypeDescriptor;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class TypeContextTest {

    @Test
    public void identity() {
        TypeDescriptor parentType = DEFAULT.get(Set.class);
        TypeDescriptor type = DEFAULT.get(String.class);
        EqualsVerifier.forClass(TypeContext.class)
                .withPrefabValues(TypeDescriptor.class, parentType, type)
                .verify();
    }
}
