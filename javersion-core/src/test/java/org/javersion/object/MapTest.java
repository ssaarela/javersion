package org.javersion.object;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.javersion.path.PropertyPath;
import org.junit.Test;

import com.google.common.collect.Maps;

public class MapTest {

    @Versionable(alias="kv", reference = true)
    public static class KeyValue {
        @Id public int id;
        @SuppressWarnings("unused")
        private KeyValue() {}
        KeyValue(int id) {
            this.id = id;
        }
        public int hashCode() {
            return id;
        }
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof KeyValue) {
                return this.id == ((KeyValue) obj).id;
            } else {
                return false;
            }
        }
        public String toString() {
            return "KeyValue#" + id;
        }
    }

    @Versionable
    public static class Mab {
        public Map<String, Integer> primitives = Maps.newLinkedHashMap();
        public Map<KeyValue, KeyValue> objects = Maps.newLinkedHashMap();
    }

    private final ObjectSerializer<Mab> serializer = new ObjectSerializer<>(Mab.class);

    @Test
    public void Write_Read_Map() {
        Mab map = new Mab();
        map.primitives.put("123", 456);
        map.primitives.put("null", null);

        KeyValue kv = new KeyValue(567);
        map.objects.put(kv, kv);
        map.objects.put(new KeyValue(789), new KeyValue(234));
        map.objects.put(new KeyValue(890), null);

        Map<PropertyPath, Object> properties = serializer.toPropertyMap(map);

        map = serializer.fromPropertyMap(properties);
        assertThat(map.primitives, equalTo(map("123", 456, "null", null)));
        assertThat(map.objects, equalTo(map(kv, kv, new KeyValue(789), new KeyValue(234), new KeyValue(890), null)));
    }

    @SuppressWarnings("unused")
    private static <K, V> Map<K, V> map(K k, V v) {
        Map<K, V> map = Maps.newLinkedHashMap();
        map.put(k, v);
        return map;
    }

    private static <K, V> Map<K, V> map(K k1, V v1, K k2, V v2) {
        Map<K, V> map = Maps.newLinkedHashMap();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    private static <K, V> Map<K, V> map(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = Maps.newLinkedHashMap();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }
}
