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

import java.util.Set;

import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class ObjectMapping<O> implements TypeMapping {

    private static final String REFERENCES = "@REF@";
    
    private final SubPath targetSchemaPath;
    
    private final Set<TypeDescriptor> types;

    private final IdMapper<?> idMapper;
    
    private final Class<? extends O> rootType;
    
    public ObjectMapping(Class<? extends O> rootType, Iterable<TypeDescriptor> types) {
        this(rootType, types, null, null);
    }
    public ObjectMapping(
            Class<? extends O> rootType, 
            Iterable<TypeDescriptor> types,
            IdMapper<?> idMapper,
            String alias) {
        this.rootType = Check.notNull(rootType, "rootType");
        Check.notNullOrEmpty(types, "types");

        this.types = ImmutableSet.copyOf(types);
        this.idMapper = idMapper;
        
        if (idMapper != null) {
            this.targetSchemaPath = PropertyPath.ROOT.property(REFERENCES).property(alias).index("");
        } else {
            this.targetSchemaPath = null;
        }
    }

    @Override
    public boolean applies(TypeMappingKey mappingKey) {
        return types.contains(mappingKey.typeDescriptor);
    }
    
    @Override
    public  synchronized ValueType describe(DescribeContext context) {
        PropertyPath path = context.getCurrentPath();
        if (isReferencePath(path)) {
            return describeReferenceType(context);
        } else {
            return describeEntityType(context);
        }
    }

    private boolean isReferencePath(PropertyPath path) {
        return targetSchemaPath != null && !targetSchemaPath.equals(path);
    }

    private ValueType describeReferenceType(DescribeContext context) {
        context.describe(targetSchemaPath, new TypeMappingKey(context.getCurrentType()));
        return new ObjectReferenceType<>(idMapper, targetSchemaPath.parent);
    }

    private ValueType describeEntityType(DescribeContext context) {
        PropertyPath path = context.getCurrentPath();
        Set<FieldDescriptor> uniqueFields = Sets.newHashSet();
        for (TypeDescriptor type : types) {
            for (FieldDescriptor fieldDescriptor : type.getFields().values()) {
                if (uniqueFields.add(fieldDescriptor)) {
                    TypeMappingKey mappingKey = new TypeMappingKey(fieldDescriptor);
                    context.describe(path.property(fieldDescriptor.getName()), mappingKey);
                }
            }
        }
        return new ObjectType<>(rootType, types);
    }
    
}