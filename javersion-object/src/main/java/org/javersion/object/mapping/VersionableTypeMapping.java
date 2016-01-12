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

import static org.javersion.object.TypeMappings.USE_JACKSON_ANNOTATIONS;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.javersion.object.DescribeContext;
import org.javersion.object.TypeContext;
import org.javersion.object.Versionable;
import org.javersion.object.Versionable.Subclass;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;

import com.fasterxml.jackson.annotation.JsonSubTypes;

public class VersionableTypeMapping implements TypeMapping {

    @Override
    public boolean applies(PropertyPath path, TypeContext typeContext) {
        return path != null && typeContext.type.hasAnnotation(Versionable.class);
    }


    @Override
    public  ValueType describe(PropertyPath path, TypeContext typeContext, DescribeContext context) {
        Map<String, TypeDescriptor> typesByAlias = new LinkedHashMap<>();
        TypeDescriptor type = typeContext.type;
        String alias = ObjectTypeMapping.getAlias(type);
        typesByAlias.put(alias, type);

        Map<String, TypeDescriptor> subclasses = getVersionSubclasses(type);
        if (subclasses == null) {
            subclasses = getJacksonSubclasses(type);
        }
        if (subclasses != null) {
            typesByAlias.putAll(subclasses);
        }

        ObjectTypeMapping objectTypeMapping = new ObjectTypeMapping(typesByAlias);
        return objectTypeMapping.describe(path, typeContext, context);
    }

    private Map<String, TypeDescriptor> getVersionSubclasses(TypeDescriptor type) {
        Map<String, TypeDescriptor>  typesByAlias = null;
        Versionable versionable = type.getAnnotation(Versionable.class);
        if (versionable != null && versionable.subclasses().length > 0) {
            typesByAlias = new HashMap<>();
            for (Subclass subclass : versionable.subclasses()) {
                TypeDescriptor subtype = type.getTypeDescriptors().get(subclass.value());
                String alias = getAlias(subclass, subtype);
                typesByAlias.put(alias, subtype);
            }
        }
        return typesByAlias;
    }

    private Map<String, TypeDescriptor> getJacksonSubclasses(TypeDescriptor type) {
        Map<String, TypeDescriptor>  typesByAlias = null;
        if (USE_JACKSON_ANNOTATIONS) {
            JsonSubTypes subTypes = type.getAnnotation(JsonSubTypes.class);
            if (subTypes != null) {
                typesByAlias = new HashMap<>();
                for (JsonSubTypes.Type subType : subTypes.value()) {
                    TypeDescriptor subtype = type.getTypeDescriptors().get(subType.value());
                    String alias = getAlias(subType, subtype);
                    typesByAlias.put(alias, subtype);
                }
            }
        }
        return typesByAlias;
    }

    static String getAlias(Subclass subclass, TypeDescriptor type) {
        return ObjectTypeMapping.getAlias(subclass.alias(), type);
    }

    static String getAlias(JsonSubTypes.Type subclass, TypeDescriptor type) {
        return ObjectTypeMapping.getAlias(subclass.name(), type);
    }

}