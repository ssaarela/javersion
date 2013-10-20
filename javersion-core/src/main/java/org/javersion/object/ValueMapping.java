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

import java.util.Map;

import org.javersion.util.Check;

import com.google.common.collect.Maps;

public class ValueMapping<V> {
    
    public final ValueType<V> valueType;
    
    private final Map<String, ValueMapping<V>> children = Maps.newHashMap();
    

    ValueMapping() {
        this.valueType = null;
    }
    ValueMapping(ValueType<V> valueType) {
        this.valueType = Check.notNull(valueType, "valueType");
    }

    public ValueMapping<V> getChild(String name) {
        return children.get(name);
    }
    
    public boolean hasChildren() {
        return !children.isEmpty();
    }
    
    void addChild(String name, ValueMapping<V> chid) {
        children.put(name, chid);
    }

    public boolean isReference() {
        return valueType instanceof ReferenceType;
    }
    
    public boolean hasChild(String name) {
        return children.containsKey(name);
    }

}
