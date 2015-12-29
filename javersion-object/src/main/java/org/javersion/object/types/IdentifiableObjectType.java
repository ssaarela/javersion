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

import java.util.Map;

import org.javersion.object.WriteContext;
import org.javersion.path.NodeId;
import org.javersion.reflect.Property;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

import com.google.common.collect.BiMap;

public class IdentifiableObjectType<O> extends ObjectType<O> implements IdentifiableType {

    private final Property idProperty;

    private final IdentifiableType idType;

    public IdentifiableObjectType(BiMap<String, TypeDescriptor> typesByAlias, Map<String, Property> properties,
                                  Property idProperty, IdentifiableType idType) {
        super(typesByAlias, properties);
        this.idProperty = Check.notNull(idProperty, "idProperty");
        this.idType = Check.notNull(idType, "idType");
    }

    @Override
    public NodeId toNodeId(Object object, WriteContext writeContext) {
        return idType.toNodeId(idProperty.get(object), writeContext);
    }

}
