package org.javersion.reflect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.reflect.ConstructorSignature.DEFAULT_CONSTRUCTOR;
import static org.javersion.reflect.ConstructorSignature.STRING_CONSTRUCTOR;
import static org.javersion.reflect.TypeDescriptors.DEFAULT;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class ConstructorTest {

    static class Constructors {
        private String name;
        private Constructors() {
            this("default name");
        }
        public Constructors(String name) {
            this.name = name;
        }
    }

    static class SubConstructors extends Constructors {

    }

    @Test
    public void inspect_constructors() {
        Map<ConstructorSignature, ConstructorDescriptor> constructors = getConstructors();
        assertThat(constructors).hasSize(2);

        ConstructorDescriptor constructor = constructors.get(DEFAULT_CONSTRUCTOR);
        assertThat(constructor).isNotNull();
        Constructors c = (Constructors) constructor.newInstance();
        assertThat(c.name).isEqualTo("default name");

        constructor = constructors.get(STRING_CONSTRUCTOR);
        assertThat(constructor).isNotNull();
        c = (Constructors) constructor.newInstance("foobar");
        assertThat(c.name).isEqualTo("foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void instantiate_with_wrong_parameter_type() {
        ConstructorDescriptor constructor = getConstructors().get(STRING_CONSTRUCTOR);
        constructor.newInstance(123);
    }

    @Test
    public void applies() {
        ConstructorDescriptor constructor = getConstructors().get(STRING_CONSTRUCTOR);
        assertThat(constructor.applies(DEFAULT.get(Constructors.class))).isTrue();
        assertThat(constructor.applies(DEFAULT.get(SubConstructors.class))).isFalse();
        assertThat(constructor.applies(DEFAULT.get(Object.class))).isFalse();
    }

    @Test
    public void identity() {
        Map<ConstructorSignature, ConstructorDescriptor> constructors = getConstructors();
        EqualsVerifier.forClass(ConstructorDescriptor.class)
                .withPrefabValues(Constructor.class,
                        constructors.get(DEFAULT_CONSTRUCTOR).getElement(),
                        constructors.get(STRING_CONSTRUCTOR).getElement())
                .verify();
    }

    @Test
    public void parameter_inspection() {
        ConstructorDescriptor constructor = getConstructors().get(STRING_CONSTRUCTOR);
        ParameterDescriptor parameterDescriptor = constructor.getParameters().get(0);
        assertThat(parameterDescriptor.getName()).isEqualTo("name");
        assertThat(parameterDescriptor.getType().getRawType()).isEqualTo(String.class);
    }

    @Test(expected = ReflectionException.class)
    public void illegal_access() {
        ConstructorDescriptor constructor = DEFAULT.get(Constructors.class).getDefaultConstructor();
        try {
            constructor.getElement().setAccessible(false);
            constructor.newInstance();
        } finally {
            constructor.getElement().setAccessible(true);
        }
    }

    @Test
    public void to_string() {
        String str = getConstructors().get(STRING_CONSTRUCTOR).toString();
        assertThat(str).isEqualTo("org.javersion.reflect.ConstructorTest$Constructors(java.lang.String)");
    }

    @Test
    public void signature_to_string() {
        String str = STRING_CONSTRUCTOR.toString();
        assertThat(str).isEqualTo("(String)");
    }

    private static Map<ConstructorSignature, ConstructorDescriptor> getConstructors() {
        return DEFAULT.get(Constructors.class).getConstructors();
    }

}
