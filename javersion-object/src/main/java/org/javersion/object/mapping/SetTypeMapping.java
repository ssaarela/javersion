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
import java.util.function.Function;

import org.javersion.object.DescribeContext;
import org.javersion.object.SetKey;
import org.javersion.object.SetKey.None;
import org.javersion.object.TypeContext;
import org.javersion.object.WriteContext;
import org.javersion.object.types.IdentifiableType;
import org.javersion.object.types.ObjectType;
import org.javersion.object.types.SetType;
import org.javersion.object.types.SetType.Key;
import org.javersion.object.types.ValueType;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.AccessibleProperty;
import org.javersion.reflect.ElementDescriptor;
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

    @SuppressWarnings("unchecked")
    public static class FunctionKey implements Key {

        private final Function function;

        private final IdentifiableType identifiableType;

        public FunctionKey(Function function, IdentifiableType identifiableType) {
            this.function = function;
            this.identifiableType = Check.notNull(identifiableType, "identifiableType");
        }

        @Override
        public NodeId toNodeId(Object object, WriteContext context) {
            return identifiableType.toNodeId(function.apply(object), context);
        }

    }

    public static class PropertyKey implements Key {

        private final AccessibleProperty property;

        private final IdentifiableType identifiableType;

        public PropertyKey(AccessibleProperty property, IdentifiableType identifiableType) {
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
        if (applies(path, typeContext)) {
            return Optional.of(new Describe(path, typeContext, context).getValueType());
        }
        return Optional.empty();
    }

    protected final ValueType newSetType(IdentifiableType valueType) {
        return newSetType(ImmutableList.of(new IdentifiableTypeKey(valueType)));
    }

    protected ValueType newSetType(List<Key> valueTypes) {
        return new SetType(valueTypes);
    }

    class Describe {
        private PropertyPath path;
        private TypeContext typeContext;
        private DescribeContext context;
        private TypeDescriptor setType;
        private TypeDescriptor elementType;
        private SetKey setKey;

        Describe(PropertyPath path, TypeContext typeContext, DescribeContext context) {
            this.path = path;
            this.typeContext = typeContext;
            this.context = context;
            setType = typeContext.type;
            elementType = setType.resolveGenericParameter(Set.class, 0);
            setKey = findSetKey(typeContext.parent, elementType);
        }

        public ValueType getValueType() {
            if (setKey == null) {
                return identifiableSetType();
            } else if (setKey.value().length > 0){
                return setKeyPropertiesType();
            } else if (!None.class.equals(setKey.by())) {
                return functionSetType();
            }
            throw new IllegalArgumentException("Elements of a set should be identifiable (e.g. scalars or having @Id property) or have a @SetKey : " +
                    typeContext);
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

        private ValueType identifiableSetType() {
            ValueType valueType = context.describeNow(path.any(), new TypeContext(setType, elementType));
            return newSetType(requireIdentifiable(valueType));
        }

        private ValueType functionSetType() {
            context.describeAsync(path.any(), new TypeContext(setType, elementType));
            TypeDescriptor functionType = setType.getTypeDescriptors().get(setKey.by());
            TypeDescriptor input = functionType.resolveGenericParameter(Function.class, 0);
            TypeDescriptor output = functionType.resolveGenericParameter(Function.class, 1);

            if (!input.isSuperTypeOf(elementType)) {
                throw new IllegalArgumentException("Input type of Function provided by @SetKey(by=+" +
                        input + ") should be super type of Set's element type " + elementType);
            }

            IdentifiableType delegate = requireIdentifiable(context.describeNow(null, new TypeContext(output)));

            return newSetType(ImmutableList.of((new FunctionKey((Function) functionType.newInstance(), delegate))));
        }

        private ValueType setKeyPropertiesType() {
            PropertyPath elementPath = getElementPath(path, setKey.value());
            ObjectType objectType = (ObjectType) context.describeNow(elementPath, new TypeContext(setType, elementType));

            if (objectType.getIdentifier() != null) {
                throw new IllegalArgumentException("Element should not have both @SetKey and @Id: " + typeContext);
            }

            // Ensure that nested mappings are processed before accessing them
            context.processMappings();

            List<Key> keys = new ArrayList<>();
            for (String idProperty : setKey.value()) {
                IdentifiableType idType = requireIdentifiable(context.getValueType(elementPath.property(idProperty)));
                AccessibleProperty property = objectType.getProperties().get(idProperty);
                keys.add(new PropertyKey(property, idType));
            }
            return newSetType(keys);
        }

        private IdentifiableType requireIdentifiable(ValueType valueType) {
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
    }
}
