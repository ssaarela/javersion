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

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.javersion.object.DescribeContext;
import org.javersion.object.TypeContext;
import org.javersion.object.Versionable;
import org.javersion.object.mapping.MappingResolver.Result;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

public class VersionableTypeMapping implements TypeMapping {

    @Override
    public boolean applies(PropertyPath path, TypeContext typeContext) {
        return path != null && typeContext.type.hasAnnotation(Versionable.class);
    }


    @Override
    public Optional<ValueType> describe(PropertyPath path, TypeContext typeContext, DescribeContext context) {
        if (!applies(path, typeContext)) {
            return Optional.empty();
        }

        MappingResolver mappingResolver = context.getMappingResolver();
        Map<String, TypeDescriptor> typesByAlias = new LinkedHashMap<>();
        TypeDescriptor type = typeContext.type;
        String alias = Check.notNull(mappingResolver.alias(type).value, "alias");
        typesByAlias.put(alias, type);
        registerSubclasses(mappingResolver, typesByAlias, mappingResolver.subclasses(type));

        ObjectTypeMapping objectTypeMapping = new ObjectTypeMapping(typesByAlias);
        return objectTypeMapping.describe(path, typeContext, context);
    }

    private void registerSubclasses(MappingResolver mappingResolver, Map<String, TypeDescriptor> typesByAlias, Result<Map<TypeDescriptor, String>> subclasses) {
        if (subclasses.isPreset()) {
            for (Map.Entry<TypeDescriptor, String> entry : subclasses.value.entrySet()) {
                TypeDescriptor type = entry.getKey();
                String alias = entry.getValue();
                if (isNullOrEmpty(alias)) {
                    alias = Check.notNull(mappingResolver.alias(type).value, "alias");
                }
                typesByAlias.put(alias, type);
            }
        }
    }

}