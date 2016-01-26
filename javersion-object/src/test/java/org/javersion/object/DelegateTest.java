package org.javersion.object;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.path.PropertyPath.ROOT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.javersion.core.Persistent;
import org.javersion.object.types.MapType;
import org.javersion.object.types.SetType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.Param;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

public class DelegateTest {

    static class Wrapper<T> {
        final T value;

        public Wrapper(T value) {
            this.value = value;
        }

        @VersionValue
        public T value() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Wrapper that = (Wrapper) o;

            return value.equals(that.value);

        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    static class Container<T> {
        Set<T> set = new HashSet<>();

        public Container(T... values) {
            this(ImmutableSet.copyOf(values));
        }

        public Container(Set<T> set) {
            this.set = set;
        }

        @VersionCreator
        public static <T> Container<T> toContainer(Set<T> set) {
            return new Container<>(set);
        }

        @VersionValue
        Set<T> toSet() {
            return set;
        }
    }

    @Versionable(alias = "Int")
    static class Int {
        @Id
        final int id;

        @VersionCreator
        public Int(@Param("id") int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Int that = (Int) o;

            return id == that.id;

        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    @Versionable
    static class SetKeyObject {
        final String value;

        @VersionCreator
        public SetKeyObject(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SetKeyObject that = (SetKeyObject) o;

            return value.equals(that.value);

        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    static class SetKeyContainer {
        private Set<SetKeyObject> set;

        public SetKeyContainer(Set<SetKeyObject> set) {
            this.set = set;
        }

        @SetKey("value")
        @VersionValue
        public Set<SetKeyObject> asSet() {
            return set;
        }
    }

    static class BadDelegate1 {

        // No matching constructor
        public BadDelegate1() {}

        @Override
        @VersionValue
        public String toString() {
            return super.toString();
        }
    }

    static class BadDelegate2 {
        // Two version values
        @Override
        @VersionValue
        public String toString() {
            return super.toString();
        }
        @VersionValue
        public String value() {
            return toString();
        }
    }

    static class BadDelegate3 {
        @VersionCreator
        public BadDelegate3(String value) {}
        @VersionCreator
        public static BadDelegate3 valueOf(String value) {
            return null;
        }

        @Override
        @VersionValue
        public String toString() {
            return super.toString();
        }
    }

    static class BadDelegate8 {
        @VersionCreator
        public static BadDelegate8 of(String value) {
            return null;
        }
        @VersionCreator
        public static BadDelegate8 valueOf(String value) {
            return null;
        }

        @Override
        @VersionValue
        public String toString() {
            return super.toString();
        }
    }

    static class BadDelegate4 {
        @VersionValue
        public static int value() {
            return 0;
        }
    }

    static abstract class BadDelegate5 {
        @VersionValue
        public abstract int value();
    }

    static abstract class BadDelegate6 {
        @VersionValue
        public int value(String parameter) {
            return 0;
        }
    }

    static abstract class BadDelegate7 {
        @VersionValue
        public void value() {
        }
    }

    static abstract class BadDelegate9 {
        @VersionCreator
        public static BadDelegate9 of(int value) {
            return null;
        }
        @VersionValue
        @Override
        public String toString() {
            return super.toString();
        }
    }

    static abstract class BadDelegate10 {
        @VersionCreator
        public static BadDelegate9 of(String value) {
            return null;
        }
        @VersionValue
        @Override
        public String toString() {
            return super.toString();
        }
    }

    static abstract class BadDelegate11 {
        @VersionCreator
        public BadDelegate11 of(String value) {
            return null;
        }
        @VersionValue
        @Override
        public String toString() {
            return super.toString();
        }
    }

    @Versionable
    static class VersionAnnotationOverridesJson {

        private final String value;

        VersionAnnotationOverridesJson(String value) {
            this.value = value;
        }

        @VersionCreator
        public static VersionAnnotationOverridesJson fromValue(String value) {
            return new VersionAnnotationOverridesJson(value);
        }

        @JsonCreator
        public static VersionAnnotationOverridesJson fromJson(int value) {
            throw new UnsupportedOperationException();
        }

    }

    @Test
    public void read_write_string_wrapper() {
        ObjectSerializer<Wrapper<String>> serializer =
                new ObjectSerializer<>(new TypeToken<Wrapper<String>>() {});
        Map<PropertyPath, Object> properties = serializer.toPropertyMap(new Wrapper<>("foobar"));
        assertThat(properties).isEqualTo(ImmutableMap.of(ROOT, "foobar"));

        Wrapper wrapper = serializer.fromPropertyMap(properties);
        assertThat(wrapper.value()).isEqualTo("foobar");
    }

    @Test
    public void string_wrapper_component() {
        ObjectSerializer<Container<Wrapper<String>>> serializer =
                new ObjectSerializer<>(new TypeToken<Container<Wrapper<String>>>() {});

        Container<Wrapper<String>> container = new Container<>(new Wrapper<>("foo"));

        Map<PropertyPath, Object> properties = serializer.toPropertyMap(container);
        assertThat(properties).isEqualTo(ImmutableMap.of(
                ROOT, SetType.CONSTANT,
                ROOT.key("foo"), "foo"));

        container = serializer.fromPropertyMap(properties);

        assertThat(container.set).isEqualTo(ImmutableSet.of(new Wrapper<>("foo")));
    }

    @Test
    public void string_wrapper_identifiable_component() {
        ObjectSerializer<Container<Wrapper<Int>>> serializer =
                new ObjectSerializer<>(new TypeToken<Container<Wrapper<Int>>>() {});

        Wrapper<Int> wrapper = new Wrapper<>(new Int(123));
        Container<Wrapper<Int>> container = new Container<>(wrapper);

        Map<PropertyPath, Object> properties = serializer.toPropertyMap(container);
        assertThat(properties).isEqualTo(ImmutableMap.of(
                ROOT, SetType.CONSTANT,
                ROOT.index(123), Persistent.object("Int"),
                ROOT.index(123).property("id"), 123l));

        container = serializer.fromPropertyMap(properties);

        assertThat(container.set).isEqualTo(ImmutableSet.of(wrapper));
    }

    @Test
    public void string_wrapper_scalar() {
        ObjectSerializer<Map<Wrapper<String>, Wrapper<String>>> serializer =
                new ObjectSerializer<>(new TypeToken<Map<Wrapper<String>, Wrapper<String>>>() {});

        Map<Wrapper<String>, Wrapper<String>> map = new HashMap<>();
        map.put(new Wrapper<>("foo"), new Wrapper<>("bar"));

        Map<PropertyPath, Object> properties = serializer.toPropertyMap(map);
        assertThat(properties).isEqualTo(ImmutableMap.of(
                ROOT, MapType.CONSTANT,
                ROOT.key("foo"), "bar"));

        Map<Wrapper<String>, Wrapper<String>> result = serializer.fromPropertyMap(properties);
        assertThat(result).isEqualTo(map);
    }

    @Test
    public void set_delegate_with_SetKey() {
        ObjectSerializer<SetKeyContainer> serializer = new ObjectSerializer<>(SetKeyContainer.class);

        SetKeyContainer container = new SetKeyContainer(ImmutableSet.of(new SetKeyObject("foobar")));

        Map<PropertyPath, Object> properties = serializer.toPropertyMap(container);

        container = serializer.fromPropertyMap(properties);
        assertThat(container.set).isEqualTo(ImmutableSet.of(new SetKeyObject("foobar")));
    }

    @Test
    public void version_annotation_overrides_json() {
        ObjectSerializer<VersionAnnotationOverridesJson> serializer =
                new ObjectSerializer<>(VersionAnnotationOverridesJson.class);
        Map<PropertyPath, Object> properties = serializer.toPropertyMap(new VersionAnnotationOverridesJson("Javersion"));
        VersionAnnotationOverridesJson object = serializer.fromPropertyMap(properties);
        assertThat(object.value).isEqualTo("Javersion");
    }

    @Test(expected = IllegalArgumentException.class)
    public void no_matching_creator() {
        new ObjectSerializer<>(BadDelegate1.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void two_version_value_annotations() {
        new ObjectSerializer<>(BadDelegate2.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void two_version_creators() {
        new ObjectSerializer<>(BadDelegate3.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void two_version_creator_methods() {
        new ObjectSerializer<>(BadDelegate8.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void static_value_method_not_allowed() {
        new ObjectSerializer<>(BadDelegate4.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void abstract_value_method_not_allowed() {
        new ObjectSerializer<>(BadDelegate5.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void value_method_parameters_not_allowed() {
        new ObjectSerializer<>(BadDelegate6.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void value_method_must_not_be_void() {
        new ObjectSerializer<>(BadDelegate7.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void bad_creator_method_parameter_type() {
        new ObjectSerializer<>(BadDelegate9.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void bad_creator_method_return_type() {
        new ObjectSerializer<>(BadDelegate10.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void creator_method_should_not_be_static() {
        new ObjectSerializer<>(BadDelegate11.class);
    }

}
