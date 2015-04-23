package org.javersion.json;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.javersion.path.PropertyPath.ROOT;
import static org.javersion.path.PropertyPath.parse;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.javersion.core.Persistent;
import org.javersion.path.PropertyPath;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonSerializerTest {

    private Gson gson = new GsonBuilder().serializeNulls().create();

    private JsonSerializer serializer = new JsonSerializer();

    @Test
    public void empty_object() {
        Map<PropertyPath, Object> map = serializer.parse("{}").properties;
        assertThat(map, equalTo(ImmutableMap.of(ROOT, Persistent.object())));
        assertThat(serializer.serialize(map), equalTo("{}"));
    }

    @Test
    public void type_field() {
        Map<PropertyPath, Object> map = serializer.parse(toJson(map("_type", "MyType"))).properties;
        assertThat(map, equalTo(ImmutableMap.of(ROOT, Persistent.object("MyType"))));
        assertThat(serializer.serialize(map), equalTo("{\"_type\":\"MyType\"}"));
    }

    @Test
    public void sparse_list() {
        Map<PropertyPath, Object> map = ImmutableMap.of(parse(""), Persistent.array(), parse("[1]"), 1l, parse("[3]"), 3l);
        assertThat(serializer.serialize(map), equalTo("[null,1,null,3]"));
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
        Map<PropertyPath, Object> map = serializer.parse("[]").properties;
        assertThat(map, equalTo(ImmutableMap.of(ROOT, Persistent.array())));
        assertThat(serializer.serialize(map), equalTo("[]"));
    }

    @Test
    public void nested_structure() {
        final String json = toJson(asList("str", 123, map(), true, array(), null, asList(asList(map(), array()))));
        assertSerializationRoundTrip(json);
    }

    private void assertSerializationRoundTrip(String json) {
        Map<PropertyPath, Object> map = serializer.parse(json).properties;
        assertThat(serializer.serialize(map), equalTo(json));
    }

    private String toJson(Object src) {
        return gson.toJson(src);
    }

    private static Map<String, Object> map(Object... keysAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i=0; i < keysAndValues.length; i+=2) {
            map.put(String.valueOf(keysAndValues[i]), keysAndValues[i+1]);
        }
        return map;
    }

}
