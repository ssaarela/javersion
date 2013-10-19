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

import java.util.List;
import java.util.Map;

import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.util.Check;

import com.google.common.collect.Maps;

public final class ValueMapping<V> {
    
    public ValueType<V> valueType;
    
    private Map<String, ValueMapping<V>> children = Maps.newHashMap();
    
    public ValueMapping() {}
            
    public ValueMapping(ValueType<V> valueType) {
        this.valueType = Check.notNull(valueType, "valueType");
    }

    public ValueMapping<V> getChild(String name) {
        return children.get(name);
    }
    
    private ValueMapping<V> getOrAppendChild(String name) {
        ValueMapping<V> child = children.get(name);
        if (child == null) {
            child = new ValueMapping<>();
            children.put(name, child);
        }
        return child;
    }
    
    public ValueMapping<V> get(PropertyPath path) {
        Check.notNull(path, "path");
        ValueMapping<V> currentMapping = this;
        List<PropertyPath> pathElements = path.toSchemaPath().path();
        for (PropertyPath currentPath : pathElements.subList(1, pathElements.size())) {
            currentMapping = currentMapping.getChild(currentPath.getName());
            if (currentMapping == null) {
                throw new IllegalArgumentException("Path not found: " + currentPath);
            }
        }
        return currentMapping;
    }
    
    public void set(SubPath path, ValueMapping<V> valueMapping) {
        Check.notNull(path, "path");
        Check.notNull(valueMapping, "valueMapping");
        
        ValueMapping<V> parent = append(path.parent);
        parent.children.put(path.getName(), valueMapping);
    }
    
    public ValueMapping<V> append(PropertyPath path, ValueType<V> valueType) {
        Check.notNull(path, "path");
        Check.notNull(valueType, "valueType");

        ValueMapping<V> valueMapping = append(path);
        valueMapping.valueType = valueType;
        return valueMapping;
    }
    
    private ValueMapping<V> append(PropertyPath path) {
        ValueMapping<V> currentMapping = this;
        List<PropertyPath> pathElements = path.toSchemaPath().path();
        for (PropertyPath currentPath : pathElements.subList(1, pathElements.size())) {
            currentMapping = currentMapping.getOrAppendChild(currentPath.getName());
        }
        return currentMapping;
    }
    
    public boolean hasChildren() {
        return !children.isEmpty();
    }
    
    public Map<String, ValueMapping<V>> getChildren() {
        return children;
    }
        
}
