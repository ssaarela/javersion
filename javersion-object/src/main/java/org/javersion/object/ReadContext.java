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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;
import org.javersion.path.Schema;

import com.google.common.collect.Maps;

@NotThreadSafe
public class ReadContext {

    private final Map<PropertyPath, Object> properties;

    private final Schema<ValueType> schemaRoot;

    private final PropertyTree rootNode;

    private final Deque<PropertyTree> lowPriorityQueue = new ArrayDeque<>();

    private final Deque<PropertyTree> highPriorityQueue = new ArrayDeque<>();

    private final Map<PropertyPath, Object> objects = Maps.newHashMap();

    protected ReadContext(Schema<ValueType> schemaRoot, Map<PropertyPath, Object> properties) {
        this.properties = properties;
        this.schemaRoot = schemaRoot;
        this.rootNode = PropertyTree.build(properties.keySet());
    }

    public Object getObject() {
        if (rootNode == null) {
            return null;
        }
        try {
            Object value = properties.get(rootNode.path);
            ValueType valueType = schemaRoot.getValue();
            Object result = valueType.instantiate(rootNode, value, this);
            objects.put(rootNode.path, result);
            valueType.bind(rootNode, result, this);
            bindAll();
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void bindAll() throws Exception {
        while (queueIsNotEmpty()) {
            PropertyTree propertyTree = nextQueueItem();
            Schema<ValueType> schema = schemaRoot.get(propertyTree.path);
            ValueType valueType = schema.getValue();
            Object object = objects.get(propertyTree.path);
            valueType.bind(propertyTree, object, this);
        }
    }

    private boolean queueIsNotEmpty() {
        return !(highPriorityQueue.isEmpty() && lowPriorityQueue.isEmpty());
    }

    private PropertyTree nextQueueItem() {
        return !highPriorityQueue.isEmpty() ? highPriorityQueue.removeFirst() : lowPriorityQueue.removeFirst();
    }

    public Object prepareObject(PropertyPath path) {
        PropertyTree propertyTree = rootNode.get(path);
        return prepareObject(propertyTree);
    }

    public boolean isMappedPath(PropertyPath path) {
        return schemaRoot.find(path) != null;
    }

    public Object prepareObject(PropertyTree propertyTree) {
        return getObject(propertyTree, true);
    }

    public Object getObject(PropertyTree propertyTree) {
        return getObject(propertyTree, false);
    }

    private Object getObject(PropertyTree propertyTree, boolean highPriority) {
        if (objects.containsKey(propertyTree.path)) {
            return objects.get(propertyTree.path);
        } else {
            Schema<ValueType> schema = schemaRoot.get(propertyTree.path);
            Object value = properties.get(propertyTree.path);
            if (value == null) {
                objects.put(propertyTree.path, null);
                return null;
            } else {
                try {
                    ValueType valueType = schema.getValue();
                    Object result = valueType.instantiate(propertyTree, value, this);
                    objects.put(propertyTree.path, result);
                    if (result != null && schema.hasChildren()) {
                        if (highPriority) {
                            highPriorityQueue.addLast(propertyTree);
                        } else {
                            lowPriorityQueue.addFirst(propertyTree);
                        }
                    }
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public Object getProperty(PropertyTree propertyTree) {
        return getProperty(propertyTree.path);
    }

    public Object getProperty(PropertyPath path) {
        return properties.get(path);
    }

}
