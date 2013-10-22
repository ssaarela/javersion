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

import org.javersion.path.PropertyPath;
import org.javersion.util.Check;


public final class ReferenceType<V, R> implements IndexableType<V> {
    
    private final IdMapper<R> idMapper;
    
    private final PropertyPath targetRoot;
    
    private final ValueType<V> stringType;
    
    public ReferenceType(IdMapper<R> idMapper, PropertyPath targetRoot, ValueType<V> stringType) {
        this.idMapper = Check.notNull(idMapper, "idMapper");
        this.targetRoot = Check.notNull(targetRoot, "targetRoot");
        this.stringType = Check.notNull(stringType, "stringType");
    }

    @SuppressWarnings("unchecked")
    public String toString(Object object, RootMapping<V> rootMapping) {
        return idMapper.getId((R) object);
    }
    
    @Override
    public void serialize(Object object, SerializationContext<V> context) {
        PropertyPath path = context.getCurrentPath();
        if (object == null) {
            context.put(path, null);
        } else {
            String id = toString(object, null);
            stringType.serialize(id, context);
            context.serialize(targetRoot.index(id), object);
        }
    }

}
