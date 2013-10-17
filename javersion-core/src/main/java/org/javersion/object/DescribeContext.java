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

import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;

import com.google.common.collect.Maps;

public class DescribeContext<V> {
    
    private final Object lock = new Object();
    
    private final Map<ValueMappingKey, ValueMapping<V>> mappings = Maps.newHashMap();

    private final ValueTypes<V> valueTypes;
    
    private Deque<ValueMappingKey> stack = new ArrayDeque<>();
    
    public DescribeContext(ValueTypes<V> valueTypes) {
        this.valueTypes = valueTypes;
    }

    public ValueMapping<V> describe(TypeDescriptor typeDescriptor) {
        return describe(new ValueMappingKey(typeDescriptor));
    }
    
    public ValueMapping<V> describe(ValueMappingKey mappingKey) {
        synchronized (lock) {
            ValueMapping<V> valueMapping = mappings.get(mappingKey);
            if (valueMapping == null) {
                stack.addLast(mappingKey);
                try {
                    ValueType<V> valueType = valueTypes.get(mappingKey);
                    valueMapping = new ValueMapping<>(valueType);
                    mappings.put(mappingKey, valueMapping);
                    valueMapping.lock(valueType.describe(this));
                } finally {
                    stack.removeLast();
                }
            }
            return valueMapping;
        }
    }
    
    public ElementDescriptor<FieldDescriptor, TypeDescriptor, TypeDescriptors> getCurrentParent() {
        return stack.getLast().parent;
    }
    
    public TypeDescriptor getCurrentType() {
        return stack.getLast().typeDescriptor;
    }

}
