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
package org.javersion.object.mapping;

import org.javersion.object.DescribeContext;
import org.javersion.object.LocalTypeDescriptor;
import org.javersion.object.types.SimpleValueType;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

public class SimpleTypeMapping implements TypeMapping {

    public final Class<?> type;
    
    public final ValueType valueType;
    
    public SimpleTypeMapping(Class<?> type) {
        this.type = Check.notNull(type, "type");
        this.valueType = new SimpleValueType();
    }

    @Override
    public boolean applies(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
        TypeDescriptor typeDescriptor = localTypeDescriptor.typeDescriptor;
        return typeDescriptor.getRawType().equals(type);
    }

    @Override
    public ValueType describe(PropertyPath path, TypeDescriptor type, DescribeContext context) {
        return valueType;
    }
    
    public ValueType getValueType() {
        return valueType;
    }

}
