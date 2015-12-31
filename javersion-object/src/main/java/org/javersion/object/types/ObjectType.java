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
package org.javersion.object.types;

import java.util.Map;
import java.util.function.BiConsumer;

import org.javersion.core.Persistent;
import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;
import org.javersion.reflect.Property;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableMap;


public class ObjectType<O> implements ValueType {

    public static ObjectType of(Map<String, TypeDescriptor> typesByAlias,
                                Map<String, Property> properties,
                                Identifier identifier) {
        if (identifier != null) {
            return new Identifiable<>(typesByAlias, properties, identifier);
        } else {
            return new ObjectType(typesByAlias, properties, identifier);
        }
    }

    private static class Identifiable<O> extends ObjectType<O> implements IdentifiableType {
        private Identifiable(Map<String, TypeDescriptor> typesByAlias, Map<String, Property> properties, Identifier identifier) {
            super(typesByAlias, properties, identifier);
        }
    }

    public static class Identifier {

        final String name;

        final Property property;

        final IdentifiableType idType;

        public Identifier(Property property, IdentifiableType idType, String name) {
            this.property = Check.notNull(property, "property");
            this.idType = Check.notNull(idType, "idType");
            this.name = Check.notNullOrEmpty(name, "name");
        }

    }

    protected final Map<String, TypeDescriptor> typesByAlias;

    protected final Map<Class<?>, String> aliasByClass;

    protected final Map<String, Property> properties;

    protected final Identifier identifier;

    private final TypeDescriptor defaultType;

    private ObjectType(Map<String, TypeDescriptor> typesByAlias,
                      Map<String, Property> properties,
                      Identifier identifier) {
        Check.notNullOrEmpty(typesByAlias, "typesByAlias");
        Check.notNull(properties, "properties");
        this.typesByAlias = ImmutableMap.copyOf(typesByAlias);
        this.properties = ImmutableMap.copyOf(properties);
        this.identifier = identifier;

        aliasByClass = aliasByClass(typesByAlias);
        defaultType = defaultType(typesByAlias);
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object valueObject, ReadContext context) throws Exception {
        TypeDescriptor typeDescriptor;
        Object object;
        if (defaultType != null) {
            typeDescriptor = defaultType;
            object = defaultType.newInstance();
        } else {
            String alias = ((Persistent.Object) valueObject).type;
            typeDescriptor = Check.notNull$(typesByAlias.get(alias), "Unsupported type: %s", alias);
            object = typeDescriptor.newInstance();
        }
        if (identifier != null && identifier.property.isWritableFrom(typeDescriptor)) {
            PropertyTree child = propertyTree.get(NodeId.property(identifier.name));
            Object value = context.getObject(child);
            identifier.property.set(object, value);
        }
        return object;
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
        TypeDescriptor typeDescriptor = getTypeDescriptor(object.getClass());
        for (PropertyTree child : propertyTree.getChildren()) {
            NodeId nodeId = child.getNodeId();
            if (nodeId.isKey()) {
                Property property = properties.get(nodeId.getKey());
                if (property != null && property.isWritableFrom(typeDescriptor)) {
                    Object value = context.getObject(child);
                    property.set(object, value);
                }
            }
        }
    }

    private TypeDescriptor getTypeDescriptor(Class<?> clazz) {
        return typesByAlias.get(aliasByClass.get(clazz));
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        String alias = aliasByClass.get(object.getClass());
        context.put(path, Persistent.object(alias));
        TypeDescriptor typeDescriptor = typesByAlias.get(alias);

        BiConsumer<String, Property> setter = (name, property) -> {
            if (property.isReadableFrom(typeDescriptor)) {
                PropertyPath subPath = path.property(name);
                Object value = property.get(object);
                context.serialize(subPath, value);
            }
        };

        properties.forEach(setter);

        if (identifier != null) {
            setter.accept(identifier.name, identifier.property);
        }
    }

    public NodeId toNodeId(Object object, WriteContext writeContext) {
        if (identifier == null) {
            throw new UnsupportedOperationException("toNodeId is not supported for composite ids");
        }
        Object id = identifier.property.get(object);
        return identifier.idType.toNodeId(id, writeContext);
    }

    private static TypeDescriptor defaultType(Map<String, TypeDescriptor> typesByAlias) {
        if (typesByAlias.size() == 1) {
            return typesByAlias.values().iterator().next();
        } else {
            return null;
        }
    }

    private static ImmutableMap<Class<?>, String> aliasByClass(Map<String, TypeDescriptor> typesByAlias) {
        ImmutableMap.Builder<Class<?>, String> builder = ImmutableMap.builder();
        for (Map.Entry<String, TypeDescriptor> entry : typesByAlias.entrySet()) {
            builder.put(entry.getValue().getRawType(), entry.getKey());
        }
        return builder.build();
    }

    public String toString() {
        return "ObjectType of " + typesByAlias.values();
    }

}
