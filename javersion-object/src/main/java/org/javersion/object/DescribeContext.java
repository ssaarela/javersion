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
import org.javersion.reflect.TypeDescriptor;

import com.google.common.collect.Maps;

@NotThreadSafe
public final class DescribeContext {

    private final Map<TypeContext, Schema.Builder<ValueType>> schemaMappings = Maps.newHashMap();

    private final TypeMappings typeMappings;

    private final Deque<QueueItem<SubPath, TypeContext>> queue = new ArrayDeque<>();


    private Schema.Builder<ValueType> schemaRoot;

    public DescribeContext(TypeMappings typeMappings) {
        this.typeMappings = typeMappings;
    }

    public Schema describeSchema(Class<?> rootClass) {
        return describeSchema(getTypeDescriptor(rootClass));
    }

    public Schema describeSchema(TypeDescriptor rootType) {
        schemaRoot = new Schema.Builder<>();
        TypeContext typeContext = new TypeContext(rootType);

        schemaRoot.setValue(createValueType(PropertyPath.ROOT, typeContext));

        processMappings();

        return schemaRoot.build();
    }

    public void describeAsync(SubPath path, TypeContext typeContext) {
        queue.add(new QueueItem<>(path, typeContext));
    }

    public ValueType describeNow(SubPath path, TypeContext typeContext) {
        return registerMapping(path, typeContext);
    }

    private void processMappings() {
        QueueItem<SubPath, TypeContext> currentItem;
        while ((currentItem = queue.poll()) != null) {
            registerMapping(currentItem.key, currentItem.value);
        }
    }

    private ValueType registerMapping(PropertyPath path, TypeContext typeContext) {
        if (path == null) {
            return createValueType(null, typeContext);
        }
        Schema.Builder<ValueType> schema = schemaMappings.get(typeContext);
        if (schema == null) {
            schema = schemaRoot.getOrCreate(path);
            if (schema.getValue() == null) {
                ValueType valueType = createValueType(path, typeContext);
                schema.setValue(valueType);

                if (!valueType.isReference()) {
                    schemaMappings.put(typeContext, schema);
                }
            }
        } else {
            schemaRoot.connect((SubPath) path, schema);
        }
        return schema.getValue();
    }

    private synchronized ValueType createValueType(PropertyPath path, TypeContext typeContext) {
        TypeMapping typeMapping = typeMappings.getTypeMapping(path, typeContext);
        return typeMapping.describe(path, typeContext.type, this);
    }

}
