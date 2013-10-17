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
package org.javersion.object.basic;

import java.util.Map;

import org.javersion.object.DescribeContext;
import org.javersion.object.SerializationContext;
import org.javersion.object.ValueMapping;
import org.javersion.object.ValueMappingKey;
import org.javersion.object.ValueType;
import org.javersion.reflect.TypeDescriptor;

import com.google.common.collect.ImmutableMap;

public class PrimitivesType implements ValueType<Object> {

    @Override
    public boolean applies(ValueMappingKey mappingKey) {
        TypeDescriptor typeDescriptor = mappingKey.typeDescriptor;
        return typeDescriptor.isPrimitiveOrWrapper() || typeDescriptor.isSuperTypeOf(String.class);
    }

    @Override
    public Map<String, ValueMapping<Object>> describe(DescribeContext<Object> context) {
        return ImmutableMap.of();
    }

    @Override
    public void serialize(SerializationContext<Object> context) {
        context.put(context.getCurrentObject());
    }

    public String toString() {
        return getClass().getSimpleName();
    }
}
