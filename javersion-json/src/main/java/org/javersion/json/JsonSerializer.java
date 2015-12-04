/*
 * Copyright 2014 Samppa Saarela
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javersion.json;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.javersion.core.Persistent;
import org.javersion.path.PropertyPath;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyTree;
import org.javersion.path.Schema;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Properties starting with _ are considered metadata.
 * {
 *     "_type": "Type",
 *     "_revs": ["qwer1234-qwer1234", "asdf5678-asdf5678"],
 *     "_id": "12345677889"
 * }
 */
public class JsonSerializer {

    public static class JsonPaths {
        public Map<PropertyPath, Object> meta = new LinkedHashMap<>();
        public Map<PropertyPath, Object> properties = new LinkedHashMap<>();
    }

    public static class Config {
        public boolean serializeNulls = true;
        public boolean lenient = false;
        public String indent = "";

        public Config() {}

        public Config(boolean serializeNulls, boolean lenient, String indent) {
            this.serializeNulls = serializeNulls;
            this.lenient = lenient;
            this.indent = indent;
        }
    }

    public static final String TYPE_FIELD = "_type";

    public static final String META_PREFIX = "_";

    private final Schema<?> schemaRoot;

    private final Config config;

    public JsonSerializer() {
        this(null);
    }

    public JsonSerializer(Schema<Persistent.Type> schemaRoot) {
        this(new Config(), schemaRoot);
    }

    public JsonSerializer(Config config, Schema<?> schemaRoot) {
        this.config = config;
        this.schemaRoot = schemaRoot;
    }

    public JsonPaths parse(String json) {
        JsonPaths paths = new JsonPaths();
        try (JsonReader jsonReader = newJsonReader(json)) {
            toMap(PropertyPath.ROOT, jsonReader, paths.meta, paths.properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return paths;
    }

    private JsonReader newJsonReader(String json) {
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(config.lenient);
        return reader;
    }

    public String serialize(Map<PropertyPath, Object> map) {
        PropertyTree tree = PropertyTree.build(map.keySet());
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = newJsonWriter(stringWriter)) {
            toJson(tree, map, jsonWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringWriter.toString();
    }

    private JsonWriter newJsonWriter(StringWriter stringWriter) {
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent(config.indent);
        writer.setLenient(config.lenient);
        writer.setSerializeNulls(config.serializeNulls);
        return writer;
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
                Map<NodeId, PropertyTree> childrenMap = tree.getChildrenMap();
                int nonNullElements = 0;
                for (int i=0; nonNullElements < childrenMap.size(); i++) {
                    PropertyTree child = childrenMap.get(NodeId.valueOf(i));
                    if (child != null) {
                        nonNullElements++;
                        toJson(child, map, writer);
                    } else {
                        writer.nullValue();
                    }
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
                    NodeId nodeId = child.getNodeId();
                    writer.name(nodeId.toString());
                    toJson(child, map, writer);
                }
                writer.endObject();
                break;
        }
    }

    private void toMap(PropertyPath path, JsonReader reader, Map<PropertyPath, Object> meta, Map<PropertyPath, Object> properties) throws IOException {
        switch (reader.peek()) {
            case BEGIN_OBJECT:
                reader.beginObject();
                boolean map = isMap(path);
                while (reader.hasNext()) {
                    String property = reader.nextName();
                    PropertyPath propertyPath;
                    if (map) {
                        propertyPath = path.key(property);
                    } else {
                        propertyPath = path.propertyOrKey(property);
                    }

                    if (property.startsWith(META_PREFIX)) {
                        toMap(propertyPath, reader, meta, meta);
                    } else {
                        toMap(propertyPath, reader, meta, properties);
                    }
                }
                String type = getType(path, meta);
                properties.put(path, Persistent.object(type));
                reader.endObject();
                break;
            case BEGIN_ARRAY:
                properties.put(path, Persistent.array());
                reader.beginArray();
                int i=0;
                while (reader.hasNext()) {
                    toMap(path.index(i++), reader, meta, properties);
                }
                reader.endArray();
                break;
            case STRING:
                properties.put(path, reader.nextString());
                break;
            case NUMBER:
                properties.put(path, new BigDecimal(reader.nextString()));
                break;
            case BOOLEAN:
                properties.put(path, reader.nextBoolean());
                break;
            case NULL:
                reader.nextNull();
                properties.put(path, null);
                break;
            default: // others ignored
        }
    }

    // TODO: Use Schema for real!
    private boolean isMap(PropertyPath path) {
        if (schemaRoot != null) {
            Schema schema = this.schemaRoot.find(path);
            return schema != null && (schema.hasChild(NodeId.ANY_KEY) || schema.hasChild(NodeId.ANY));
        }
        return false;
    }

    private String getType(PropertyPath path, Map<PropertyPath, Object> meta) {
        Object typeObject = meta.get(path.property(TYPE_FIELD));
        if (typeObject != null) {
            return typeObject.toString();
        } else {
            return Persistent.GENERIC_TYPE;
        }
    }

}
