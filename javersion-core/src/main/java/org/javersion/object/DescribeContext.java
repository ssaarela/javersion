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
    
    private final Map<LocalTypeDescriptor, Schema> schemaMappings = Maps.newHashMap();
    
    private final ValueTypes valueTypes;
    
    private final Deque<QueueItem<SubPath, LocalTypeDescriptor>> queue = new ArrayDeque<>();

    
    private SchemaRoot schemaRoot;
    
    private QueueItem<? extends PropertyPath, LocalTypeDescriptor> currentItem;
    
    public DescribeContext(ValueTypes valueTypes) {
        this.valueTypes = valueTypes;
    }

    public SchemaRoot describeSchema(Class<?> rootClass) {
        return describeSchema(getTypeDescriptor(rootClass));
    }

    public SchemaRoot describeSchema(TypeDescriptor rootType) {
        schemaRoot = new SchemaRoot();
        LocalTypeDescriptor localTypeDescriptor = new LocalTypeDescriptor(rootType);
        currentItem = new QueueItem<PropertyPath, LocalTypeDescriptor>(PropertyPath.ROOT, localTypeDescriptor);
        
        schemaRoot.setValueType(createValueType(PropertyPath.ROOT, localTypeDescriptor));

        processMappings();
        
        lockMappings();
        
        try {
            return schemaRoot;
        } finally {
            schemaRoot = null;
        }
    }
    
    public void describeAsync(SubPath path, FieldDescriptor fieldDescriptor) {
        queue.add(new QueueItem<SubPath, LocalTypeDescriptor>(path, new LocalTypeDescriptor(fieldDescriptor)));
    }
    
    public void describeAsync(SubPath path, TypeDescriptor typeDescriptor) {
        queue.add(new QueueItem<SubPath, LocalTypeDescriptor>(path, new LocalTypeDescriptor(typeDescriptor)));
    }
    
    public ValueType describeNow(SubPath path, FieldDescriptor fieldDescriptor) {
        return describeNow(path, new LocalTypeDescriptor(fieldDescriptor));
    }
    
    public ValueType describeNow(SubPath path, TypeDescriptor typeDescriptor) {
        return describeNow(path, new LocalTypeDescriptor(typeDescriptor));
    }
    
    public ValueType describeComponent(SubPath path, TypeDescriptor parent, TypeDescriptor typeDescriptor) {
        return describeNow(path, new LocalTypeDescriptor(parent, typeDescriptor));
    }
    
    private ValueType describeNow(SubPath path, LocalTypeDescriptor localTypeDescriptor) {
        QueueItem<? extends PropertyPath, LocalTypeDescriptor> stackedItem = currentItem;
        try {
            currentItem = new QueueItem<SubPath, LocalTypeDescriptor>(path, localTypeDescriptor);
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

    private ValueType registerMapping(QueueItem<? extends PropertyPath, LocalTypeDescriptor> item) {
        PropertyPath path = item.key;
        LocalTypeDescriptor localTypeDescriptor = item.value;
        Schema schema = schemaMappings.get(localTypeDescriptor);
        if (schema == null) {
            schema = addSchema(path);
            ValueType valueType = createValueType(path, localTypeDescriptor);
            schema.setValueType(valueType);
            // FIXME: Refactor this check into ValueType
            if (!schema.isReference()) {
                schemaMappings.put(localTypeDescriptor, schema);
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
    
    private synchronized ValueType createValueType(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
        TypeMapping typeMapping = valueTypes.getMapping(path, localTypeDescriptor);
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
