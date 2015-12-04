package org.javersion.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.core.Persistent.Type.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import org.javersion.core.Persistent.Array;
import org.javersion.core.Persistent.Object;
import org.javersion.core.Persistent.Type;
import org.junit.Test;

public class PersistentTest {

    @Test
    public void types() {
        assertThat(Type.of(null)).isEqualTo(TOMBSTONE);
        assertThat(Type.of(Persistent.NULL)).isEqualTo(NULL);
        assertThat(Type.of(Persistent.object())).isEqualTo(OBJECT);
        assertThat(Type.of(Persistent.array())).isEqualTo(ARRAY);
        assertThat(Type.of("")).isEqualTo(STRING);
        assertThat(Type.of(true)).isEqualTo(BOOLEAN);
        assertThat(Type.of(1l)).isEqualTo(LONG);
        assertThat(Type.of(1.0)).isEqualTo(DOUBLE);
        assertThat(Type.of(BigDecimal.ONE)).isEqualTo(BIG_DECIMAL);
    }

    @Test
    public void array_identity() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Array a1 = Persistent.array();
        Array a2 = Persistent.array();
        assertThat(a1).isSameAs(a2);
        assertThat(a1).isEqualTo(a2);
        assertThat(a1).isNotEqualTo("Array()");

        Constructor<Array> constructor = Array.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Array a3 = constructor.newInstance();
        assertThat(a3).isNotSameAs(a2);
        assertThat(a3).isEqualTo(a2);
        assertThat(a3.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    public void array_to_string() {
        assertThat(Persistent.array().toString()).isEqualTo("Array()");
    }

    @Test
    public void object_identity() {
        Object o1 = Persistent.object();
        Object o2 = Persistent.object();
        assertThat(o1).isSameAs(o2);
        assertThat(o1).isEqualTo(o2);
        assertThat(o1).isNotEqualTo(Persistent.GENERIC_TYPE);

        Object o3 = Persistent.object(Persistent.GENERIC_TYPE);
        assertThat(o3).isNotSameAs(o2);
        assertThat(o3).isEqualTo(o2);
        assertThat(o3.hashCode()).isEqualTo(o2.hashCode());

        Object o4 = Persistent.object("MyType");
        assertThat(o4).isNotEqualTo(o3);
        assertThat(o4.hashCode()).isNotEqualTo(o3.hashCode());
    }

    @Test
    public void object_to_string() {
        assertThat(Persistent.object().toString()).isEqualTo("Object(Map)");
        assertThat(Persistent.object("MyType").toString()).isEqualTo("Object(MyType)");
    }

    @Test
    public void generic_object() {
        assertThat(Persistent.object().isGeneric()).isTrue();
        assertThat(Persistent.object("MyType").isGeneric()).isFalse();
    }
}
