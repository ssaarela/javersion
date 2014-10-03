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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

import org.javersion.path.PropertyPath;

import com.google.common.collect.Maps;

public class WriteContext {

    private final Object root;

    private final SchemaRoot schemaRoot;

    private final Deque<QueueItem<PropertyPath, Object>> queue = new ArrayDeque<>();

    private final IdentityHashMap<Object, PropertyPath> objects = Maps.newIdentityHashMap();

    private final Map<PropertyPath, Object> properties = Maps.newLinkedHashMap();

    protected WriteContext(SchemaRoot schemaRoot, Object root) {
        this.root = root;
        this.schemaRoot = schemaRoot;
    }

    public void serialize(PropertyPath path, Object object) {
        queue.add(new QueueItem<PropertyPath, Object>(path, object));
    }

    public Map<PropertyPath, Object> getMap() {
        serialize(PropertyPath.ROOT, root);
        QueueItem<PropertyPath, Object> currentItem;
        while ((currentItem = queue.pollFirst()) != null) {
            PropertyPath path = currentItem.key;
            Object value = currentItem.value;
            if (!properties.containsKey(path)) {
                if (value == null) {
                    put(path, null);
                } else {
                    Schema schema = getSchema(path);
                    if (schema.hasChildren()  // Composite (not scalar)?
                            && !schema.isReference() // Not a reference - multiple references to same object are allowed
                            && objects.put(value, path) != null) { // First time for this object?
                        illegalReferenceException(path, value);
                    }
                    schema.serialize(path, currentItem.value, this);
                }
            }
        }
        return unmodifiableMap(properties);
    }

    private Schema getSchema(PropertyPath path) {
        return schemaRoot.get(path);
    }

    private void illegalReferenceException(PropertyPath path, Object value) {
        throw new IllegalArgumentException(format(
                "Multiple references to the same object: \"%s\"@\"%s\"",
                value,
                path));
    }

    public void put(PropertyPath path, Object value) {
        if (properties.containsKey(path)) {
            throw new IllegalArgumentException("Duplicate value for " + path);
        }
        properties.put(path, value);
    }

    public SchemaRoot getRootMapping() {
        return schemaRoot;
    }

}
