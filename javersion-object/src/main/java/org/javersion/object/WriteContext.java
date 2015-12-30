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
package org.javersion.object;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static org.javersion.path.PropertyPath.ROOT;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.path.Schema;
import org.javersion.util.Check;

import com.google.common.collect.Maps;

@NotThreadSafe
public final class WriteContext {

    private final Object root;

    private final Schema<ValueType> schemaRoot;

    private final Deque<QueueItem<PropertyPath, Object>> queue = new ArrayDeque<>();

    private final IdentityHashMap<Object, PropertyPath> objects = Maps.newIdentityHashMap();

    private final Map<PropertyPath, Object> properties = Maps.newLinkedHashMap();

    public WriteContext(Schema<ValueType> schemaRoot, @Nullable Object root) {
        this.schemaRoot = Check.notNull(schemaRoot, "schemaRoot");
        this.root = root;
    }

    public void serialize(PropertyPath path, Object object) {
        queue.add(new QueueItem<>(path, object));
    }

    public Map<PropertyPath, Object> getMap() {
        serialize(ROOT, root);
        QueueItem<PropertyPath, Object> currentItem;
        while ((currentItem = queue.pollFirst()) != null) {
            PropertyPath path = currentItem.key;
            Object value = currentItem.value;
            if (!properties.containsKey(path)) {
                if (value == null) {
                    put(path, null);
                } else {
                    Schema<ValueType> schema = getSchema(path);
                    ValueType valueType = schema.getValue();
                    if (schema.hasChildren()  // Composite (not scalar)?
                        && !valueType.isReference()) { // Not a reference - multiple references to same object are allowed
                        checkIllegalReference(path, value);
                    }
                    valueType.serialize(path, currentItem.value, this);
                }
            }
        }
        return unmodifiableMap(properties);
    }

    public boolean isMappedPath(PropertyPath path) {
        return schemaRoot.find(path) != null;
    }

    private Schema<ValueType> getSchema(PropertyPath path) {
        return schemaRoot.get(path);
    }

    private void checkIllegalReference(PropertyPath path, Object value) {
        Object oldPath = objects.put(value, path);

        if (oldPath != null) {
            throw new IllegalArgumentException(
                    format("Multiple references to the same object: %s = %s = %s", oldPath, path, value));
        }
    }

    public void put(PropertyPath path, Object value) {
        if (properties.containsKey(path)) {
            throw new IllegalArgumentException("Duplicate value for " + path);
        }
        properties.put(path, value);
    }

    public Schema<ValueType> getRootMapping() {
        return schemaRoot;
    }

}
