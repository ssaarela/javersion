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

import static org.javersion.object.mapping.ObjectTypeMapping.DEFAULT_FILTER;

import java.util.Optional;

import org.javersion.object.DescribeContext;
import org.javersion.object.LocalTypeDescriptor;
import org.javersion.object.Versionable;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;

import com.google.common.collect.ImmutableBiMap;

public class VersionableTypeMapping implements TypeMapping {

    @Override
    public boolean applies(Optional<PropertyPath> path, LocalTypeDescriptor localTypeDescriptor) {
        return path.isPresent() && localTypeDescriptor.typeDescriptor.hasAnnotation(Versionable.class);
    }


    @Override
    public  ValueType describe(Optional<PropertyPath> path, TypeDescriptor type, DescribeContext context) {
        String alias = getAlias(type.getAnnotation(Versionable.class), type);
        ObjectTypeMapping objectTypeMapping = new ObjectTypeMapping(ImmutableBiMap.of(alias, type), DEFAULT_FILTER);
        return objectTypeMapping.describe(path, type, context);
    }

    static String getAlias(Versionable versionable, TypeDescriptor type) {
        return ObjectTypeMapping.getAlias(versionable.alias(), type);
    }

}