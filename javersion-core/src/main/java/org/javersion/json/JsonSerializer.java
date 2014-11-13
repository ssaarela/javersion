package org.javersion.json;

import static com.google.common.collect.Maps.newLinkedHashMap;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Map;

import org.javersion.object.Persistent;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class JsonSerializer {

    public static final String TYPE_FIELD = "_type";

    public Map<PropertyPath, Object> toPropertyMap(String json) {
        Map<PropertyPath, Object> map = newLinkedHashMap();
        try (JsonReader jsonReader = new JsonReader(new StringReader(json))) {
            toMap(PropertyPath.ROOT, jsonReader, map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public String fromPropertyMap(Map<PropertyPath, Object> map) {
        PropertyTree tree = PropertyTree.build(map.keySet());
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = new JsonWriter(stringWriter)) {
            toJson(tree, map, jsonWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringWriter.toString();
    }

    private void toJson(PropertyTree tree, Map<PropertyPath, Object> map, JsonWriter writer) throws IOException {
        Object value = map.get(tree.path);
        switch (JsonType.getType(value)) {
            case NULL:
                writer.nullValue();
                break;
            case STRING:
                writer.value((String) value);
                break;
            case BOOLEAN:
                writer.value((Boolean) value);
                break;
            case NUMBER:
                writer.value((Number) value);
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
                String typeAlias = ((Persistent.Object) value).type;
                if (!Persistent.GENERIC_TYPE.equals(typeAlias)) {
                    writer.name(TYPE_FIELD).value(typeAlias);
                }
                for (PropertyTree child : tree.getChildren()) {
                    writer.name(child.getName());
                    toJson(child, map, writer);
                }
                writer.endObject();
                break;
        }
    }

    private void toMap(PropertyPath path, JsonReader reader, Map<PropertyPath, Object> map) throws IOException {
        switch (reader.peek()) {
            case BEGIN_OBJECT:
                map.put(path, Persistent.object());
                reader.beginObject();
                while (reader.hasNext()) {
                    String property = reader.nextName();
                    PropertyPath propertyPath = path.property(property);

                    toMap(propertyPath, reader, map);
                    if (TYPE_FIELD.equals(property)) {
                        Object objectType = map.get(propertyPath);
                        if (objectType instanceof String) {
                            map.remove(propertyPath);
                            map.put(path, Persistent.object(objectType.toString()));
                        }
                    }
                }
                reader.endObject();
                break;
            case BEGIN_ARRAY:
                map.put(path, Persistent.array());
                reader.beginArray();
                int i=0;
                while (reader.hasNext()) {
                    toMap(path.index(i++), reader, map);
                }
                reader.endArray();
                break;
            case STRING:
                map.put(path, reader.nextString());
                break;
            case NUMBER:
                map.put(path, new BigDecimal(reader.nextString()));
                break;
            case BOOLEAN:
                map.put(path, reader.nextBoolean());
                break;
            case NULL:
                reader.nextNull();
                map.put(path, null);
                break;
        }
    }

}
