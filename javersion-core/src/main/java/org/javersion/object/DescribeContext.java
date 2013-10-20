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
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;

import com.google.common.collect.Maps;

public class DescribeContext<V> {
    
    private final Map<ValueMappingKey, ValueMapping<V>> mappings = Maps.newHashMap();

    private final ValueTypes<V> valueTypes;
    
    private final Deque<QueueItem<SubPath, ValueMappingKey>> stack = new ArrayDeque<>();

    private RootMapping<V> rootMapping; 
    
    private QueueItem<? extends PropertyPath, ValueMappingKey> currentItem;
    
    public DescribeContext(ValueTypes<V> valueTypes) {
        this.valueTypes = valueTypes;
    }

    public ValueMapping<V> describe(TypeDescriptor rootType) {
        ValueMappingKey mappingKey = new ValueMappingKey(rootType);
        currentItem = new QueueItem<PropertyPath, ValueMappingKey>(PropertyPath.ROOT, mappingKey);
        rootMapping = new RootMapping<>(createValueType(mappingKey));
        processSubMappings();
        return rootMapping;
    }
    
    private void processSubMappings() {
        while ((currentItem = stack.poll()) != null) {
            ValueMappingKey mappingKey = currentItem.value;
            ValueMapping<V> valueMapping = mappings.get(mappingKey);
            if (valueMapping != null) {
                rootMapping.set((SubPath) currentItem.key, valueMapping);
            } else {
                valueMapping = rootMapping.append(currentItem.key, createValueType(mappingKey));
                mappings.put(mappingKey, valueMapping);
            }
        }
    }
    
    private ValueType<V> createValueType(ValueMappingKey mappingKey) {
        ValueTypeFactory<V> valueTypeFactory = valueTypes.getFactory(mappingKey);
        return valueTypeFactory.describe(this);
    }
   
    public void describe(SubPath path, ValueMappingKey mappingKey) {
        stack.add(new QueueItem<SubPath, ValueMappingKey>(path, mappingKey));
    }
    
    public ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> getCurrentParent() {
        return currentItem.value.parent;
    }
    
    public TypeDescriptor getCurrentType() {
        return currentItem.value.typeDescriptor;
    }

    public PropertyPath getCurrentPath() {
        return currentItem.key;
    }
}
