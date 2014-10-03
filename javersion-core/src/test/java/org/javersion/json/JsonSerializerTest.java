package org.javersion.json;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.javersion.json.JsonToken.Obj;
import static org.javersion.path.PropertyPath.ROOT;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.javersion.path.PropertyPath;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonSerializerTest {

    private Gson gson = new GsonBuilder().serializeNulls().create();

    private JsonSerializer serializer = new JsonSerializer();

    @Test
    public void empty_object() {
        Map<PropertyPath, JsonToken<?>> map = serializer.toPropertyMap("{}");
        assertThat(map, equalTo(map(ROOT, Obj.VALUE)));
        assertThat(serializer.fromPropertyMap(map), equalTo("{}"));
    }

    @Test
    public void object_with_primitive_properties() {
        final String json = toJson(map());
        assertSerializationRoundTrip(json);
    }

    private Map<String, Object> map() {
        return map("string", "str", "number", 123, "boolean", true, "nil", null);
    }

    @Test
    public void array_with_primitive_properties() {
        final String json = toJson(array());
        assertSerializationRoundTrip(json);
    }

    private List<? extends Serializable> array() {
        return asList("str", 123, true, null);
    }

    @Test
    public void empty_array() {
        assertSerializationRoundTrip("[]");
    }

    @Test
    public void nested_structure() {
        final String json = toJson(asList("str", 123, map(), true, array(), null, asList(asList(map(), array()))));
        assertSerializationRoundTrip(json);
    }

    private void assertSerializationRoundTrip(String json) {
        Map<PropertyPath, JsonToken<?>> map = serializer.toPropertyMap(json);
        assertThat(serializer.fromPropertyMap(map), equalTo(json));
    }

    private String toJson(Object src) {
        return gson.toJson(src);
    }

    private static Map<PropertyPath, JsonToken<?>> map(PropertyPath k1, JsonToken<?> v1) {
        Map<PropertyPath, JsonToken<?>> map = Maps.newHashMap();
        map.put(k1, v1);
        return map;
    }

    private static Map<String, Object> map(Object... keysAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i=0; i < keysAndValues.length; i+=2) {
            map.put(String.valueOf(keysAndValues[i]), keysAndValues[i+1]);
        }
        return map;
    }

}
