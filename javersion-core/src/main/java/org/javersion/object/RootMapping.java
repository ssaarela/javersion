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

import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.util.Check;

public class RootMapping<V> extends ValueMapping<V> {

    public RootMapping(ValueType<V> valueType) {
        super(valueType);
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

}
