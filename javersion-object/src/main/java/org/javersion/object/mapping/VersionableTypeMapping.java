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

import java.util.LinkedHashMap;
import java.util.Map;

import org.javersion.object.DescribeContext;
import org.javersion.object.TypeContext;
import org.javersion.object.Versionable;
import org.javersion.object.Versionable.Subclass;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;

public class VersionableTypeMapping implements TypeMapping {

    @Override
    public boolean applies(PropertyPath path, TypeContext typeContext) {
        return path != null && typeContext.type.hasAnnotation(Versionable.class);
    }


    @Override
    public  ValueType describe(PropertyPath path, TypeContext typeContext, DescribeContext context) {
        Map<String, TypeDescriptor> typesByAlias = new LinkedHashMap<>();
        TypeDescriptor type = typeContext.type;
        Versionable versionable = type.getAnnotation(Versionable.class);
        String alias = getAlias(versionable, type);
        typesByAlias.put(alias, type);

        for (Subclass subclass : versionable.subclasses()) {
            TypeDescriptor subtype = type.getTypeDescriptors().get(subclass.value());
            if (subtype.hasAnnotation(Versionable.class)) {
                throw new IllegalArgumentException(subtype.getSimpleName() + "" +
                        " islready mapped in " + type.getSimpleName());
            }
            alias = getAlias(subclass, subtype);
            typesByAlias.put(alias, subtype);
        }

        ObjectTypeMapping objectTypeMapping = new ObjectTypeMapping(typesByAlias);
        return objectTypeMapping.describe(path, typeContext, context);
    }

    static String getAlias(Versionable versionable, TypeDescriptor type) {
        return ObjectTypeMapping.getAlias(versionable.alias(), type);
    }

    static String getAlias(Subclass subclass, TypeDescriptor type) {
        return ObjectTypeMapping.getAlias(subclass.alias(), type);
    }

}