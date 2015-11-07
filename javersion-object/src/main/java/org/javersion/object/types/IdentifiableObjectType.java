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
package org.javersion.object.types;

import org.javersion.object.WriteContext;
import org.javersion.path.NodeId;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptor;

import com.google.common.collect.BiMap;

public class IdentifiableObjectType<O> extends ObjectType<O> implements IdentifiableType {

    private final FieldDescriptor idField;

    private final IdentifiableType idType;

    public IdentifiableObjectType(BiMap<String, TypeDescriptor> typesByAlias, FieldDescriptor idField, IdentifiableType idType) {
        super(typesByAlias);
        this.idField = idField;
        this.idType = idType;
    }

    @Override
    public NodeId toNodeId(Object object, WriteContext writeContext) {
        return idType.toNodeId(idField.get(object), writeContext);
    }

}
