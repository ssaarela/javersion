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

public class ValueTypes<V> {

    private final Iterable<ValueType<V>> types;
    
    public ValueTypes(Iterable<ValueType<V>> types) {
        this.types = types;
    }

    public ValueType<V> get(ValueMappingKey mappingKey) {
        for (ValueType<V> valueType : types) {
            if (valueType.applies(mappingKey)) {
                return valueType;
            }
        }
        throw new IllegalArgumentException("ValueType not found for " + mappingKey);
    }
    
}
