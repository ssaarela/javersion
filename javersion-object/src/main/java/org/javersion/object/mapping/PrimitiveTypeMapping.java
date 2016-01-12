/*
 * Copyright 2014 Samppa Saarela
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

import javax.annotation.Nullable;

import org.javersion.object.DescribeContext;
import org.javersion.object.TypeContext;
import org.javersion.object.types.PrimitiveValueType;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;

public final class PrimitiveTypeMapping implements TypeMapping {

    private final Class<?> primitiveType;

    private final Class<?> wrapperType;

    private final ValueType valueType;

    private PrimitiveTypeMapping(Class<?> wrapperType, Class<?> primitiveType, ValueType valueType) {
        this.wrapperType = wrapperType;
        this.primitiveType = primitiveType;
        this.valueType = valueType;
    }

    @Override
    public boolean applies(PropertyPath path, TypeContext typeContext) {
        Class<?> clazz = typeContext.type.getRawType();
        return wrapperType.equals(clazz) || primitiveType.equals(clazz);
    }

    @Override
    public ValueType describe(@Nullable PropertyPath path, TypeDescriptor type, DescribeContext context) {
        return valueType;
    }

    public static final PrimitiveTypeMapping LONG = new PrimitiveTypeMapping(Long.class, long.class, PrimitiveValueType.LONG);

    public static final PrimitiveTypeMapping INT = new PrimitiveTypeMapping(Integer.class, int.class, PrimitiveValueType.INT);

    public static final PrimitiveTypeMapping SHORT = new PrimitiveTypeMapping(Short.class, short.class, PrimitiveValueType.SHORT);

    public static final PrimitiveTypeMapping BYTE = new PrimitiveTypeMapping(Byte.class, byte.class, PrimitiveValueType.BYTE);

    public static final PrimitiveTypeMapping BOOLEAN = new PrimitiveTypeMapping(Boolean.class, boolean.class, PrimitiveValueType.BOOLEAN);

    public static final PrimitiveTypeMapping DOUBLE = new PrimitiveTypeMapping(Double.class, double.class, PrimitiveValueType.DOUBLE);

    public static final PrimitiveTypeMapping FLOAT = new PrimitiveTypeMapping(Float.class, float.class, PrimitiveValueType.FLOAT);

    public static final PrimitiveTypeMapping CHAR = new PrimitiveTypeMapping(Character.class, char.class, PrimitiveValueType.CHAR);

}