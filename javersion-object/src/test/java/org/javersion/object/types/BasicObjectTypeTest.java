package org.javersion.object.types;

import static org.javersion.reflect.ConstructorSignature.STRING_CONSTRUCTOR;
import static org.javersion.reflect.TypeDescriptors.DEFAULT;

import org.javersion.object.PolymorphismTest.Pet;
import org.javersion.reflect.TypeDescriptor;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class BasicObjectTypeTest {

    @Test(expected = UnsupportedOperationException.class)
    public void toNodeId_throws_exception_for_non_identifiable() {
        TypeDescriptor type = DEFAULT.get(Pet.class);
        BasicObjectType objectType = BasicObjectType.of(
                type,
                "Pet",
                type.getConstructors().get(STRING_CONSTRUCTOR),
                null,
                ImmutableMap.of()
        );


        objectType.toNodeId("foo", null);
    }
}
