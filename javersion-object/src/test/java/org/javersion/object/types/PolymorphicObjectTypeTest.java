package org.javersion.object.types;

import static org.javersion.reflect.TypeDescriptors.getTypeDescriptor;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class PolymorphicObjectTypeTest {

    static class A {}

    static class B {}

    @Test(expected = IllegalArgumentException.class)
    public void verify_subclasses() {
        PolymorphicObjectType.of(
                BasicObjectType.of(getTypeDescriptor(A.class)),
                ImmutableList.of(BasicObjectType.of(getTypeDescriptor(B.class), "B"))
        );
    }
}
