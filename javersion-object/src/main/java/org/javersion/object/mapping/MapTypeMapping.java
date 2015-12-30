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

import java.util.Map;

import org.javersion.object.DescribeContext;
import org.javersion.object.TypeContext;
import org.javersion.object.types.MapType;
import org.javersion.object.types.ScalarType;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;

public class MapTypeMapping implements TypeMapping {

    private final Class<? extends Map> mapType;

    public MapTypeMapping() {
        this(Map.class);
    }

    public MapTypeMapping(Class<? extends Map> mapType) {
        this.mapType = mapType;
    }

    @Override
    public boolean applies(PropertyPath path, TypeContext descriptor) {
        return path != null && descriptor.type.getRawType().equals(mapType);
    }

    @Override
    public ValueType describe(PropertyPath path, TypeDescriptor mapType, DescribeContext context) {
        TypeDescriptor keyType = mapType.resolveGenericParameter(Map.class, 0);
        TypeDescriptor valueType = mapType.resolveGenericParameter(Map.class, 1);

        context.describeAsync(path.any(), new TypeContext(mapType, valueType));

        ValueType keyValueType = context.describeNow(null, new TypeContext(mapType, keyType));
        if (!(keyValueType instanceof ScalarType)) {
            throw new IllegalArgumentException("Key of " + path + ": " + mapType + " is not a scalar (ScalarType)");
        }
        return newMapType((ScalarType) keyValueType);
    }

    protected ValueType newMapType(ScalarType keyType) {
        return new MapType(keyType);
    }

}
