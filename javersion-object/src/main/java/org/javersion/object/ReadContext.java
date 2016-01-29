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
public final class ReadContext {

    private final Map<PropertyPath, Object> properties;

    private final Schema<ValueType> schemaRoot;

    private final PropertyTree rootNode;

    private final Deque<PropertyTree> bindQueue = new ArrayDeque<>();

    private final Map<PropertyPath, Object> objects = Maps.newHashMap();

    public ReadContext(Schema<ValueType> schemaRoot, Map<PropertyPath, Object> properties) {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void bindAll() throws Exception {
        while (!bindQueue.isEmpty()) {
            PropertyTree propertyTree = bindQueue.removeFirst();
            Schema<ValueType> schema = schemaRoot.get(propertyTree.path);
            ValueType valueType = schema.getValue();
            Object object = objects.get(propertyTree.path);
            valueType.bind(propertyTree, object, this);
        }
    }

    public Object getObject(PropertyPath path) {
        return getObject(rootNode.get(path));
    }

    public Object getObject(PropertyTree propertyTree) {
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
                        bindQueue.addFirst(propertyTree);
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
