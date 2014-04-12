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

import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;

import com.google.common.collect.Maps;

public class ReadContext {

    private final Map<PropertyPath, Object> properties;
    
    private final SchemaRoot schemaRoot;
    
    private final PropertyTree rootNode;

    private final Deque<PropertyTree> queue = new ArrayDeque<>();
    
    private final Map<PropertyPath, Object> objects = Maps.newHashMap();
    
//    private QueueItem<PropertyPath, Object> currentItem;
    
    protected ReadContext(SchemaRoot schemaRoot, Map<PropertyPath, Object> properties) {
        this.properties = properties;
        this.schemaRoot = schemaRoot;
        this.rootNode = PropertyTree.build(properties.keySet());
    }
    
    public Object getObject() {
        try {
            Object value = properties.get(rootNode.path);
            Object result = schemaRoot.valueType.instantiate(rootNode, value, this);
            objects.put(rootNode.path, result);
            if (result != null && rootNode.hasChildren()) {
                schemaRoot.valueType.bind(rootNode, result, this);
                while (!queue.isEmpty()) {
                    PropertyTree propertyTree = queue.pop();
                    Schema schema = schemaRoot.get(propertyTree.path);
                    Object target = objects.get(propertyTree.path);
                    schema.valueType.bind(propertyTree, target, this);
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object getObject(PropertyPath path) {
        PropertyTree propertyTree = rootNode.get(path);
        return propertyTree != null ? getObject(propertyTree) : null;
    }

    public Object getObject(PropertyTree propertyTree) {
        if (objects.containsKey(propertyTree.path)) {
            return objects.get(propertyTree.path);
        } else {
            Schema schema = schemaRoot.get(propertyTree.path);
            Object value = properties.get(propertyTree.path);
            if (value == null) {
                objects.put(propertyTree.path, null);
                return null;
            } else {
                try {
                    Object result = schema.valueType.instantiate(propertyTree, value, this);
                    objects.put(propertyTree.path, result);
                    if (result != null && schema.hasChildren()) {
                        queue.add(propertyTree);
                    }
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
