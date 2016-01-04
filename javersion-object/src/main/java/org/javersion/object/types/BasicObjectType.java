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
import java.util.Set;
import java.util.function.BiConsumer;

import org.javersion.core.Persistent;
import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.object.mapping.ObjectType;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;
import org.javersion.reflect.ConstructorDescriptor;
import org.javersion.reflect.ParameterDescriptor;
import org.javersion.reflect.Property;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;


public class BasicObjectType implements ObjectType {

    public static BasicObjectType of(TypeDescriptor type,
                                     String alias,
                                     ConstructorDescriptor constructor,
                                     ObjectIdentifier identifier,
                                     Map<String, Property> properties) {
        if (identifier != null) {
            return new Identifiable(type, alias, constructor, identifier, properties);
        } else {
            return new BasicObjectType(type, alias, constructor, identifier, properties);
        }
    }

    private static class Identifiable extends BasicObjectType implements IdentifiableType {
        private Identifiable(TypeDescriptor type,
                             String alias,
                             ConstructorDescriptor constructor,
                             ObjectIdentifier identifier,
                             Map<String, Property> properties) {
            super(type, alias, constructor, identifier, properties);
        }
    }

    private final Map<String, Property> properties;

    private final ConstructorDescriptor constructor;

    private final Set<String> constructorParameters;

    private final ObjectIdentifier identifier;

    private final String alias;

    private final TypeDescriptor type;

    private BasicObjectType(TypeDescriptor type,
                            String alias,
                            ConstructorDescriptor constructor,
                            ObjectIdentifier identifier,
                            Map<String, Property> properties) {
        this.type = Check.notNull(type, "type");
        this.alias = Check.notNullOrEmpty(alias, "alias");
        this.constructor = constructor;
        this.properties = ImmutableMap.copyOf(properties);
        this.identifier = identifier;

        ImmutableSet.Builder<String> paramBuilder = ImmutableSet.builder();
        if (constructor != null) {
            for (ParameterDescriptor param : constructor.getParameters()) {
                paramBuilder.add(param.getName());
            }
        }
        this.constructorParameters = paramBuilder.build();
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object valueObject, ReadContext context) throws Exception {
        Object[] params = new Object[constructorParameters.size()];
        int i=0;
        for (String param : constructorParameters) {
            PropertyTree child = propertyTree.get(NodeId.property(param));
            Object value = context.getObject(child);
            params[i++] = value;
        }
        if (!constructorParameters.isEmpty()) {
            // Ensure that constructor parameters are bound
            context.bindAll();
        }
        Object object = constructor.newInstance(params);
        if (identifier != null && !constructorParameters.contains(identifier.name) && identifier.property.isWritableFrom(type)) {
            PropertyTree child = propertyTree.get(NodeId.property(identifier.name));
            Object value = context.getObject(child);
            identifier.property.set(object, value);
        }
        return object;
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
        for (PropertyTree child : propertyTree.getChildren()) {
            NodeId nodeId = child.getNodeId();
            if (nodeId.isKey() && !constructorParameters.contains(nodeId.getKey())) {
                Property property = properties.get(nodeId.getKey());
                if (property != null && property.isWritableFrom(type)) {
                    Object value = context.getObject(child);
                    property.set(object, value);
                }
            }
        }
    }

    public ObjectIdentifier getIdentifier() {
        return identifier;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        context.put(path, Persistent.object(alias));
        BiConsumer<String, Property> propertySerializer = (name, property) -> {
            PropertyPath subPath = path.property(name);
            Object value = property.get(object);
            context.serialize(subPath, value);
        };
        properties.forEach(propertySerializer);

        if (identifier != null && identifier.property.isWritableFrom(type)) {
            propertySerializer.accept(identifier.name, identifier.property);
        }
    }

    public String getAlias() {
        return alias;
    }

    public TypeDescriptor getType() {
        return type;
    }

    public NodeId toNodeId(Object object, WriteContext writeContext) {
        if (identifier == null) {
            throw new UnsupportedOperationException("toNodeId is not supported for composite ids");
        }
        Object id = identifier.property.get(object);
        return identifier.idType.toNodeId(id, writeContext);
    }

    public String toString() {
        return "BasicObjectType of " + type;
    }

}
