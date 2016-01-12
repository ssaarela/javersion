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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.javersion.object.DescribeContext;
import org.javersion.object.SetKey;
import org.javersion.object.TypeContext;
import org.javersion.object.WriteContext;
import org.javersion.object.types.IdentifiableType;
import org.javersion.object.types.ObjectType;
import org.javersion.object.types.SetType;
import org.javersion.object.types.SetType.Key;
import org.javersion.object.types.ValueType;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.Property;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableList;

public class SetTypeMapping implements TypeMapping {

    public static class IdentifiableTypeKey implements Key {

        private final IdentifiableType identifiableType;

        public IdentifiableTypeKey(IdentifiableType identifiableType) {
            this.identifiableType = Check.notNull(identifiableType, "identifiableType");
        }

        @Override
        public NodeId toNodeId(Object object, WriteContext context) {
            return identifiableType.toNodeId(object, context);
        }
    }

    public static class PropertyKey implements Key {

        private final Property property;

        private final IdentifiableType identifiableType;

        public PropertyKey(Property property, IdentifiableType identifiableType) {
            this.property = Check.notNull(property, "property");
            this.identifiableType = Check.notNull(identifiableType, "identifiableType");
        }

        @Override
        public NodeId toNodeId(Object object, WriteContext context) {
            Object keyValue = property.get(object);
            return identifiableType.toNodeId(keyValue, context);
        }
    }

    private final Class<? extends Set> setType;

    public SetTypeMapping() {
        this(Set.class);
    }

    public SetTypeMapping(Class<? extends Set> setType) {
        this.setType = setType;
    }

    @Override
    public boolean applies(PropertyPath path, TypeContext typeContext) {
        return path != null && typeContext.type.getRawType().equals(setType);
    }

    @Override
    public Optional<ValueType> describe(PropertyPath path, TypeContext typeContext, DescribeContext context) {
        ValueType setValueType = null;
        if (applies(path, typeContext)) {
            TypeDescriptor setType = typeContext.type;
            TypeDescriptor elementType = setType.resolveGenericParameter(Set.class, 0);
            SetKey setKey = findSetKey(typeContext.parent, elementType);

            if (setKey == null) {
                ValueType valueType = context.describeNow(path.any(), new TypeContext(setType, elementType));
                setValueType = newSetType(requireIdentifiable(valueType, typeContext));
            } else {
                PropertyPath elementPath = getElementPath(path, setKey.value());
                ObjectType objectType = (ObjectType) context.describeNow((SubPath) elementPath, new TypeContext(setType, elementType));

                if (objectType.getIdentifier() != null) {
                    throw new IllegalArgumentException("Element should not have both @SetKey and @Id: " +
                            (typeContext.parent != null ? typeContext.parent : typeContext.type));
                }

                // Ensure that nested mappings are processed before accessing them
                context.processMappings();

                List<Key> keys = new ArrayList<>();
                for (String idProperty : setKey.value()) {
                    IdentifiableType idType = requireIdentifiable(context.getValueType(elementPath.property(idProperty)), typeContext);
                    Property property = objectType.getProperties().get(idProperty);
                    keys.add(new PropertyKey(property, idType));
                }
                setValueType = newSetType(keys);
            }
        }
        return Optional.ofNullable(setValueType);
    }

    private IdentifiableType requireIdentifiable(ValueType valueType, TypeContext typeContext) {
        if (valueType instanceof IdentifiableType) {
            return (IdentifiableType) valueType;
        }
        throw new IllegalArgumentException(typeContext.toString() + ": expected IdentifiableType, got " + valueType.getClass());
    }

    private PropertyPath getElementPath(PropertyPath path, String[] properties) {
        PropertyPath elementPath = path;
        for (int i = properties.length; i > 0; i--) {
            elementPath = elementPath.any();
        }
        return elementPath;
    }

    private SetKey findSetKey(ElementDescriptor parent, TypeDescriptor elementType) {
        SetKey setKey = null;
        if (parent != null) {
            setKey = parent.getAnnotation(SetKey.class);
        }
        if (setKey == null) {
            setKey = elementType.getAnnotation(SetKey.class);
        }
        return setKey;
    }

    protected final ValueType newSetType(IdentifiableType valueType) {
        return newSetType(ImmutableList.of(new IdentifiableTypeKey(valueType)));
    }

    protected ValueType newSetType(List<Key> valueTypes) {
        return new SetType(valueTypes);
    }

}
