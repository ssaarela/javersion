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

import static org.javersion.reflect.TypeDescriptors.getTypeDescriptor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;

import com.google.common.collect.Maps;

public class DescribeContext {
    
    public static final DescribeContext DEFAULT = new DescribeContext(ValueTypes.DEFAULT);
    
    private final Map<ElementDescriptor, Schema> schemaMappings = Maps.newHashMap();
    
    private final ValueTypes valueTypes;
    
    private final Deque<QueueItem<SubPath, ElementDescriptor>> queue = new ArrayDeque<>();

    
    private SchemaRoot schemaRoot;
    
    private QueueItem<? extends PropertyPath, ElementDescriptor> currentItem;
    
    public DescribeContext(ValueTypes valueTypes) {
        this.valueTypes = valueTypes;
    }

    public SchemaRoot describeSchema(Class<?> rootClass) {
        return describeSchema(getTypeDescriptor(rootClass));
    }

    public SchemaRoot describeSchema(TypeDescriptor rootType) {
        schemaRoot = new SchemaRoot();
        ElementDescriptor elementDescriptor = new ElementDescriptor(rootType);
        currentItem = new QueueItem<PropertyPath, ElementDescriptor>(PropertyPath.ROOT, elementDescriptor);
        
        schemaRoot.setValueType(createValueType(PropertyPath.ROOT, elementDescriptor));

        processMappings();
        
        lockMappings();
        
        try {
            return schemaRoot;
        } finally {
            schemaRoot = null;
        }
    }
    
    public void describeAsync(SubPath path, FieldDescriptor fieldDescriptor) {
        queue.add(new QueueItem<SubPath, ElementDescriptor>(path, new ElementDescriptor(fieldDescriptor)));
    }
    
    public void describeAsync(SubPath path, TypeDescriptor typeDescriptor) {
        queue.add(new QueueItem<SubPath, ElementDescriptor>(path, new ElementDescriptor(typeDescriptor)));
    }
    
    public ValueType describeNow(SubPath path, FieldDescriptor fieldDescriptor) {
        return describeNow(path, new ElementDescriptor(fieldDescriptor));
    }
    
    public ValueType describeNow(SubPath path, TypeDescriptor typeDescriptor) {
        return describeNow(path, new ElementDescriptor(typeDescriptor));
    }
    
    private ValueType describeNow(SubPath path, ElementDescriptor elementDescriptor) {
        QueueItem<? extends PropertyPath, ElementDescriptor> stackedItem = currentItem;
        try {
            currentItem = new QueueItem<SubPath, ElementDescriptor>(path, elementDescriptor);
            return registerMapping(this.currentItem);
        } finally {
            currentItem = stackedItem;
        }
    }
    
    public synchronized TypeDescriptor getCurrentType() {
        return currentItem.value.typeDescriptor;
    }

    public synchronized PropertyPath getCurrentPath() {
        return currentItem.key;
    }
    
    private void processMappings() {
        while ((currentItem = queue.poll()) != null) {
            registerMapping(currentItem);
        }
    }

    private ValueType registerMapping(QueueItem<? extends PropertyPath, ElementDescriptor> item) {
        PropertyPath path = item.key;
        ElementDescriptor elementDescriptor = item.value;
        Schema schema = schemaMappings.get(elementDescriptor);
        if (schema == null) {
            schema = addSchema(path);
            ValueType valueType = createValueType(path, elementDescriptor);
            schema.setValueType(valueType);
            // FIXME: Refactor this check into ValueType
            if (!schema.isReference()) {
                schemaMappings.put(elementDescriptor, schema);
            }
        } else {
            connectSchema((SubPath) path, schema);
        }
        return schema.getValueType();
    }
    
    private void connectSchema(SubPath path, Schema schema) {
        Schema parent = addSchema(path.parent);
        parent.addChild(path.getName(), schema);
    }
    
    private Schema addSchema(PropertyPath path) {
        Schema schema = schemaRoot;
        for (PropertyPath pathElement : path.asList()) {
            String name = pathElement.getName();
            Schema child = schema.getChild(name);
            if (child == null) {
                child = new Schema();
                schema.addChild(name, child);
            }
            schema = child;
        }
        return schema;
    }
    
    private synchronized ValueType createValueType(PropertyPath path, ElementDescriptor elementDescriptor) {
        TypeMapping typeMapping = valueTypes.getMapping(path, elementDescriptor);
        return typeMapping.describe(this);
    }
    
    private void lockMappings() {
        for (Schema mapping : schemaMappings.values()) {
            if (!mapping.isLocked()) {
                mapping.lock();
            }
        }
    }
}
