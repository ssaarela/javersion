package org.javersion.object.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.path.PropertyPath.parse;
import static org.javersion.reflect.ConstructorSignature.STRING_CONSTRUCTOR;
import static org.javersion.reflect.TypeDescriptors.DEFAULT;

import java.util.HashMap;
import java.util.Map;

import org.javersion.object.Id;
import org.javersion.object.ObjectSerializer;
import org.javersion.object.PolymorphismTest.Pet;
import org.javersion.object.VersionConstructor;
import org.javersion.object.Versionable;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class BasicObjectTypeTest {

    @Versionable
    static class MyClass {
        @SuppressWarnings("unused")
        @Id
        public String foo;

        @SuppressWarnings("unused")
        public String bar;

        @JsonCreator
        private MyClass() {}

        public MyClass(String foo, String bar) {
            this.foo = foo;
            this.bar = bar;
        }

        @VersionConstructor
        public MyClass(@JsonProperty("bar") String bar) {
            this.foo = foo;
            this.bar = bar;
        }
    }

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
        TypeDescriptor type = DEFAULT.get(MyClass.class);
        ObjectConstructor objectConstructor = new ObjectConstructor(type);
        assertThat(objectConstructor.getParameters()).isEmpty();
    }

    @Test
    public void filter_default_properties() {
        TypeDescriptor type = DEFAULT.get(MyClass.class);

        BasicObjectType objectType = BasicObjectType.of(type,  ImmutableSet.of("foo"));
        assertThat(objectType.getProperties().keySet()).isEqualTo(ImmutableSet.of("foo"));
    }

    @Test
    public void null_constructor_parameter() {
        ObjectSerializer<MyClass> serializer = new ObjectSerializer<>(MyClass.class);
        Map<PropertyPath, Object> properties = serializer.toPropertyMap(new MyClass(null, null));

        MyClass instance = serializer.fromPropertyMap(properties);
        assertThat(instance.foo).isNull();
        assertThat(instance.bar).isNull();

        properties = new HashMap<>(properties);
        properties.remove(parse("foo"));
        properties.remove(parse("bar"));

        instance = serializer.fromPropertyMap(properties);
        assertThat(instance.foo).isNull();
        assertThat(instance.bar).isNull();
    }
}
