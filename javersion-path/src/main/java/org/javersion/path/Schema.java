/*
 * Copyright 2013 Samppa Saarela
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
package org.javersion.path;

import java.util.IdentityHashMap;
import java.util.Map;

import org.javersion.path.PropertyPath.NodeId;
import org.javersion.path.PropertyPath.SubPath;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class Schema<T> extends SchemaBase<Schema<T>> {

    private final T value;

    private final Map<NodeId, Schema<T>> children;

    protected Schema(Builder<T> builder) {
        this(builder, new IdentityHashMap<Builder<T>, Schema<T>>());
    }

    private Schema(Builder<T> schemaBuilder, IdentityHashMap<Builder<T>, Schema<T>> schemas) {
        schemas.put(schemaBuilder, this);

        ImmutableMap.Builder<NodeId, Schema<T>> children = ImmutableMap.builder();
        for (Map.Entry<NodeId, Builder<T>> entry : schemaBuilder.children.entrySet()) {
            Builder<T> childBuilder = entry.getValue();
            Schema<T> child = schemas.get(childBuilder);
            if (child == null) {
                child = new Schema<>(childBuilder, schemas);
            }
            children.put(entry.getKey(), child);
        }

        this.value = schemaBuilder.value;
        this.children = children.build();
    }

    public T getValue() {
        return value;
    }

    public Schema<T> getChild(NodeId nodeId) {
        return children.get(nodeId);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public Schema<T> addChild(NodeId nodeId, Schema<T> child) {
        children.put(nodeId, child);
        return child;
    }

    public boolean hasChild(NodeId nodeId) {
        return children.containsKey(nodeId);
    }

    public static class Builder<T> extends SchemaBase<Builder<T>> {

        private T value;

        private Map<NodeId, Builder<T>> children = Maps.newHashMap();

        public Builder() {}

        public Builder(T value) {
            this.value = value;
        }

        @Override
        public Builder<T> getChild(NodeId nodeId) {
            return children.get(nodeId);
        }

        public Builder<T> addChild(NodeId nodeId, Builder<T> child) {
            children.put(nodeId, child);
            return child;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public Schema<T> build() {
            return new Schema<>(this);
        }

        public void connect(SubPath subPath, Builder<T> schema) {
            Builder<T> parent = getOrCreate(subPath.parent);
            parent.addChild(subPath.getNodeId(), schema);
        }

        public Builder<T> getOrCreate(PropertyPath path) {
            return getOrCreate(path, null);
        }

        public Builder<T> getOrCreate(PropertyPath path, T value) {
            Builder<T> schema = this;
            for (PropertyPath pathElement : path.asList()) {
                NodeId nodeId = pathElement.getNodeId();
                Schema.Builder<T> child = schema.getChild(nodeId);
                if (child == null) {
                    child = new Schema.Builder<>(value);
                    schema.addChild(nodeId, child);
                }
                schema = child;
            }
            return schema;
        }
    }

}
