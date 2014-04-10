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

import static java.util.Collections.unmodifiableMap;
import static org.javersion.reflect.TypeDescriptors.getTypeDescriptor;

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

public class DescribeContext {
    
    public static final DescribeContext DEFAULT = new DescribeContext(ValueTypes.DEFAULT);
    
    private final Map<TypeMappingKey, ValueMapping> typeMappings = Maps.newHashMap();
    
    private final ValueTypes valueTypes;
    
    private final Deque<QueueItem<SubPath, TypeMappingKey>> stack = new ArrayDeque<>();

    
    private Map<PropertyPath, TypeMappingKey> pathMappings;

    private RootMapping rootMapping;
    
    private QueueItem<? extends PropertyPath, TypeMappingKey> currentItem;
    
    public DescribeContext(ValueTypes valueTypes) {
        this.valueTypes = valueTypes;
    }

    public RootMapping describe(Class<?> clazz) {
        return describe(getTypeDescriptor(clazz));
    }

    public synchronized RootMapping describe(TypeDescriptor rootType) {
        TypeMappingKey mappingKey = new TypeMappingKey(rootType);

        if (typeMappings.containsKey(mappingKey)) {
            return (RootMapping) typeMappings.get(mappingKey);
        }
            
        pathMappings = Maps.newHashMap();
        
        currentItem = new QueueItem<PropertyPath, TypeMappingKey>(PropertyPath.ROOT, mappingKey);
        
        pathMappings.put(PropertyPath.ROOT, mappingKey);
        ValueType valueType = createValueType(mappingKey);
        rootMapping = new RootMapping(valueType, unmodifiableMap(typeMappings));
        registerMapping(currentItem, rootMapping);

        processSubMappings();
        
        lockMappings();
        
        try {
            return rootMapping;
        } finally {
            pathMappings = null;
            rootMapping = null;
        }
    }
    
    private void lockMappings() {
        for (ValueMapping mapping : typeMappings.values()) {
            if (!mapping.isLocked()) {
                mapping.lock();
            }
        }
    }

    private void registerMapping(QueueItem<? extends PropertyPath, TypeMappingKey> item, ValueMapping mapping) {
        typeMappings.put(item.value, mapping);

        // Add to parent
        if (item.key instanceof SubPath) {
            PropertyPath parentPath = ((SubPath) item.key).parent;
            ValueMapping parentMapping = getValueMapping(parentPath);
            if (parentMapping == null) {
                parentMapping = rootMapping.addPath(parentPath);
            }
            parentMapping.addChild(item.key.getName(), mapping);
        }
    }
    
    private ValueMapping getValueMapping(PropertyPath path) {
        return typeMappings.get(pathMappings.get(path));
    }
    
    private void processSubMappings() {
        while ((currentItem = stack.poll()) != null) {
            TypeMappingKey mappingKey = currentItem.value;
            ValueMapping mapping= typeMappings.get(mappingKey);
            PropertyPath path = currentItem.key;
            if (mapping == null || (mapping.isReference() && !pathMappings.containsKey(path))) {
                pathMappings.put(path, mappingKey);
                mapping = new ValueMapping(createValueType(mappingKey));
            }
            registerMapping(currentItem, mapping);
        }
    }
    
    public synchronized ValueType createValueType(TypeMappingKey mappingKey) {
        TypeMapping valueTypeFactory = valueTypes.getMapping(mappingKey);
        return valueTypeFactory.describe(this);
    }
   
    public synchronized void describe(SubPath path, TypeMappingKey mappingKey) {
        stack.add(new QueueItem<SubPath, TypeMappingKey>(path, mappingKey));
    }
    
    public synchronized ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> getCurrentParent() {
        return currentItem.value.parent;
    }
    
    public synchronized TypeMappingKey getCurrentMappingKey() {
        return currentItem.value;
    }
    
    public synchronized TypeDescriptor getCurrentType() {
        return currentItem.value.typeDescriptor;
    }

    public synchronized PropertyPath getCurrentPath() {
        return currentItem.key;
    }
}
