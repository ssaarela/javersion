package org.javersion.object.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.reflect.ConstructorSignature.STRING_CONSTRUCTOR;
import static org.javersion.reflect.TypeDescriptors.DEFAULT;

import org.javersion.object.PolymorphismTest.Pet;
import org.javersion.reflect.TypeDescriptor;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class BasicObjectTypeTest {

    @SuppressWarnings("unused")
    private String foo;

    @SuppressWarnings("unused")
    private String bar;

    @Test(expected = UnsupportedOperationException.class)
    public void toNodeId_throws_exception_for_non_identifiable() {
        TypeDescriptor type = DEFAULT.get(Pet.class);
        BasicObjectType objectType = BasicObjectType.of(
                type,
                "Pet",
                new ObjectConstructor(type.getConstructors().get(STRING_CONSTRUCTOR)),
                null,
                ImmutableMap.of()
        );


        objectType.toNodeId("foo", null);
    }

    @Test
    public void default_constructor() {
        TypeDescriptor type = DEFAULT.get(BasicObjectTypeTest.class);
        ObjectConstructor objectConstructor = new ObjectConstructor(type);
        assertThat(objectConstructor.getParameters()).isEmpty();
    }

    @Test
    public void filter_default_properties() {
        TypeDescriptor type = DEFAULT.get(BasicObjectTypeTest.class);

        BasicObjectType objectType = BasicObjectType.of(type,  ImmutableSet.of("foo"));
        assertThat(objectType.getProperties().keySet()).isEqualTo(ImmutableSet.of("foo"));
    }
}
