package org.javersion.json;

import static com.google.common.collect.Maps.newLinkedHashMap;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class JsonSerializer {

    public Map<PropertyPath, JsonToken<?>> toPropertyMap(String json) {
        Map<PropertyPath, JsonToken<?>> map = newLinkedHashMap();
        try (JsonReader jsonReader = new JsonReader(new StringReader(json))) {
            toMap(PropertyPath.ROOT, jsonReader, map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public String fromPropertyMap(Map<PropertyPath, JsonToken<?>> map) {
        PropertyTree tree = PropertyTree.build(map.keySet());
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = new JsonWriter(stringWriter)) {
            toJson(tree, map, jsonWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringWriter.toString();
    }

    private void toJson(PropertyTree tree, Map<PropertyPath, JsonToken<?>> map, JsonWriter writer) throws IOException {
        JsonToken<?> value = map.get(tree.path);
        switch (value.type()) {
            case STRING:
                writer.value(((JsonToken.Str) value).value());
                break;
            case NULL:
                writer.nullValue();
                break;
            case BOOLEAN:
                writer.value(((JsonToken.Bool) value).value());
                break;
            case NUMBER:
                writer.value(((JsonToken.Nbr) value).value());
                break;
            case ARRAY:
                writer.beginArray();
                for (PropertyTree child : tree.getChildren()) {
                    toJson(child, map, writer);
                }
                writer.endArray();
                break;
            case OBJECT:
                writer.beginObject();
                for (PropertyTree child : tree.getChildren()) {
                    writer.name(child.getName());
                    toJson(child, map, writer);
                }
                writer.endObject();
                break;
        }
    }

    private void toMap(PropertyPath path, JsonReader reader, Map<PropertyPath, JsonToken<?>> map) throws IOException {
        switch (reader.peek()) {
            case BEGIN_OBJECT:
                map.put(path, JsonToken.Obj.VALUE);
                reader.beginObject();
                while (reader.hasNext()) {
                    toMap(path.property(reader.nextName()), reader, map);
                }
                reader.endObject();
                break;
            case BEGIN_ARRAY:
                map.put(path, JsonToken.Arr.VALUE);
                reader.beginArray();
                int i=0;
                while (reader.hasNext()) {
                    toMap(path.index(i++), reader, map);
                }
                reader.endArray();
                break;
            case STRING:
                map.put(path, new JsonToken.Str(reader.nextString()));
                break;
            case NUMBER:
                map.put(path, new JsonToken.Nbr(reader.nextString()));
                break;
            case BOOLEAN:
                if (reader.nextBoolean()) {
                    map.put(path, JsonToken.Bool.TRUE);
                } else {
                    map.put(path, JsonToken.Bool.FALSE);
                }
                break;
            case NULL:
                reader.nextNull();
                map.put(path, JsonToken.Nil.VALUE);
                break;
            case END_ARRAY:
            case END_OBJECT:
            case NAME:
            case END_DOCUMENT:
                break;
        }
    }

}
