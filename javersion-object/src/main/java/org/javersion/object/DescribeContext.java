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

import javax.annotation.concurrent.NotThreadSafe;

import org.javersion.object.mapping.TypeMapping;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.path.Schema;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;

import com.google.common.collect.Maps;

@NotThreadSafe
public class DescribeContext {

    private final Map<LocalTypeDescriptor, Schema.Builder<ValueType>> schemaMappings = Maps.newHashMap();

    private final TypeMappings typeMappings;

    private final Deque<QueueItem<SubPath, LocalTypeDescriptor>> queue = new ArrayDeque<>();


    private Schema.Builder<ValueType> schemaRoot;

    public DescribeContext(TypeMappings typeMappings) {
        this.typeMappings = typeMappings;
    }

    public Schema describeSchema(Class<?> rootClass) {
        return describeSchema(getTypeDescriptor(rootClass));
    }

    public Schema describeSchema(TypeDescriptor rootType) {
        schemaRoot = new Schema.Builder<>();
        LocalTypeDescriptor localTypeDescriptor = new LocalTypeDescriptor(rootType);

        schemaRoot.setValue(createValueType(PropertyPath.ROOT, localTypeDescriptor));

        processMappings();

        return schemaRoot.build();
    }

    public void describeAsync(SubPath path, FieldDescriptor fieldDescriptor) {
        queue.add(new QueueItem<>(path, new LocalTypeDescriptor(fieldDescriptor)));
    }

    public void describeAsync(SubPath path, TypeDescriptor typeDescriptor) {
        queue.add(new QueueItem<>(path, new LocalTypeDescriptor(typeDescriptor)));
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
        return registerMapping(path, localTypeDescriptor);
    }

    private void processMappings() {
        QueueItem<SubPath, LocalTypeDescriptor> currentItem;
        while ((currentItem = queue.poll()) != null) {
            registerMapping(currentItem.key, currentItem.value);
        }
    }

    private ValueType registerMapping(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
        if (path == null) {
            return createValueType(null, localTypeDescriptor);
        }
        Schema.Builder<ValueType> schema = schemaMappings.get(localTypeDescriptor);
        if (schema == null) {
            schema = schemaRoot.getOrCreate(path);
            if (schema.getValue() == null) {
                ValueType valueType = createValueType(path, localTypeDescriptor);
                schema.setValue(valueType);

                if (!valueType.isReference()) {
                    schemaMappings.put(localTypeDescriptor, schema);
                }
            }
        } else {
            schemaRoot.connect((SubPath) path, schema);
        }
        return schema.getValue();
    }

    private synchronized ValueType createValueType(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
        TypeMapping typeMapping = typeMappings.getTypeMapping(path, localTypeDescriptor);
        return typeMapping.describe(path, localTypeDescriptor.typeDescriptor, this);
    }

}
