package org.javersion.object;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.path.PropertyPath.ROOT;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.javersion.object.types.SetType;
import org.javersion.path.PropertyPath;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

public class JacksonDelegateTest {

    static class StringWrapper {
        final String value;

        @JsonCreator
        public StringWrapper(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StringWrapper that = (StringWrapper) o;

            return value.equals(that.value);

        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    static class Container<T> {
        Set<T> set = new HashSet<>();

        public Container() {}

        public Container(Set<T> set) {
            this.set = set;
        }

        @JsonCreator
        public static <T> Container<T> toContainer(Set<T> set) {
            return new Container<>(set);
        }

        @JsonValue
        Set<T> toSet() {
            return set;
        }
    }

    @Test
    public void read_write_string_wrapper() {
        ObjectSerializer<StringWrapper> serializer = new ObjectSerializer<>(StringWrapper.class);
        Map<PropertyPath, Object> properties = serializer.toPropertyMap(new StringWrapper("foobar"));
        assertThat(properties).isEqualTo(ImmutableMap.of(ROOT, "foobar"));

        StringWrapper wrapper = serializer.fromPropertyMap(properties);
        assertThat(wrapper.toString()).isEqualTo("foobar");
    }

    @Test
    public void string_wrapper_component() {
        ObjectSerializer<Container<StringWrapper>> serializer = new ObjectSerializer<>(new TypeToken<Container<StringWrapper>>() {});
        Container<StringWrapper> container = new Container<>();
        container.set.add(new StringWrapper("foo"));

        Map<PropertyPath, Object> properties = serializer.toPropertyMap(container);
        assertThat(properties).isEqualTo(ImmutableMap.of(
                ROOT, SetType.CONSTANT,
                ROOT.key("foo"), "foo"));

        container = serializer.fromPropertyMap(properties);

        assertThat(container.set).isEqualTo(ImmutableSet.of(new StringWrapper("foo")));
    }
}
