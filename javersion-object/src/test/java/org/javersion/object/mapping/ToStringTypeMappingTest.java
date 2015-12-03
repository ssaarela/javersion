package org.javersion.object.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.javersion.object.ObjectSerializer;
import org.javersion.object.TypeMappings;
import org.javersion.object.Versionable;
import org.javersion.path.PropertyPath;
import org.javersion.util.Check;
import org.junit.Test;

public class ToStringTypeMappingTest {

    public static class A {
        final String val;
        public A(String val) {
            this.val = Check.notNull(val, "val");
        }
        public String toString() {
            return val;
        }
    }
    public static class B extends A {
        public B(String val) {
            super(val);
        }
    }

    @Versionable
    public static class Container {
        A a;
        B b;
    }

    private static final ObjectSerializer<Container> serializer = new ObjectSerializer<Container>(Container.class,
            TypeMappings.builder()
                    .withMapping(new ToStringTypeMapping(A.class, true))
                    .build());

    @Test
    public void use_generic_string_based_component_mapping() {
        Container container = new Container();
        container.a = new A("a");
        container.b = new B("b");

        Map<PropertyPath, Object> properties = serializer.toPropertyMap(container);
        container = serializer.fromPropertyMap(properties);

        assertThat(container.a).isInstanceOf(A.class);
        assertThat(container.a.val).isEqualTo("a");

        assertThat(container.b).isInstanceOf(B.class);
        assertThat(container.b.val).isEqualTo("b");
    }

    @Test
    public void polymorphism_not_supported() {
        Container container = new Container();
        container.a = new B("b");

        Map<PropertyPath, Object> properties = serializer.toPropertyMap(container);
        container = serializer.fromPropertyMap(properties);

        assertThat(container.a).isInstanceOf(A.class);
        assertThat(container.a.val).isEqualTo("b");
    }
}
