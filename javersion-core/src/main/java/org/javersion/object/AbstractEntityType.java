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

import static com.google.common.collect.Maps.uniqueIndex;

import java.util.Map;
import java.util.Set;

import org.javersion.path.PropertyPath;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

import com.google.common.base.Function;

public abstract class AbstractEntityType<V> implements ValueType<V> {

    private static final Function<TypeDescriptor, Class<?>> getRawType = new Function<TypeDescriptor, Class<?>>() {
        @Override
        public Class<?> apply(TypeDescriptor input) {
            Check.notNull(input, "input");
            return input.getRawType();
        }
    };
    
    private final Map<Class<?>, TypeDescriptor> types;
    
    public AbstractEntityType(Set<TypeDescriptor> types) {
        Check.notNullOrEmpty(types, "types");
        this.types = uniqueIndex(types, getRawType);
    }

    @Override
    public void serialize(SerializationContext<V> context) {
        Object object = context.getCurrentObject();
        PropertyPath path = context.getCurrentPath();
        if (object == null) {
            context.put(path, null);
        } else {
            context.put(path, toValue(object));
            TypeDescriptor typeDescriptor = types.get(object.getClass());
            for (FieldDescriptor fieldDescriptor : typeDescriptor.getFields().values()) {
                Object value = fieldDescriptor.get(object);
                PropertyPath subPath = path.property(fieldDescriptor.getName());
                context.serialize(subPath, value);
            }
        }
    }

    protected abstract V toValue(Object object);
    
    public String toString() {
        return "EntityType of " + types.values();
    }

}
