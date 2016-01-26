package org.javersion.object.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.object.types.PrimitiveValueType.BOOLEAN;
import static org.javersion.reflect.TypeDescriptors.getTypeDescriptor;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.javersion.object.DescribeContext;
import org.javersion.object.TypeContext;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;
import org.junit.Test;

public class TypeMappingTest {

    @Test
    public void default_applies() {
        assertThat(new TypeMapping() {}.applies(null, null)).isFalse();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void default_describe() {
        new TypeMapping() {}.describe(null, getTypeDescriptor(TypeMapping.class), null);
    }

    @Test
    public void default_describe_optional() {
        Optional<ValueType> optional = new TypeMapping() {
            @Override
            public boolean applies(@Nullable PropertyPath path, TypeContext typeContext) {
                return true;
            }

            @Nonnull
            @Override
            public ValueType describe(@Nullable PropertyPath path, TypeDescriptor type, DescribeContext context) {
                return BOOLEAN;
            }
        }.describe(null, new TypeContext(getTypeDescriptor(TypeMapping.class)), null);

        assertThat(optional.get()).isEqualTo(BOOLEAN);
    }

    @Test
    public void default_describe_optional_no_match() {
        Optional<ValueType> optional = new TypeMapping() {
        }.describe(null, new TypeContext(getTypeDescriptor(TypeMapping.class)), null);

        assertThat(optional.isPresent()).isFalse();
    }
}
