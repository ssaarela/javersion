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

import javax.annotation.Nullable;

import org.javersion.core.Persistent;
import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.Property;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;


public class BasicObjectType implements ObjectType {

    public static BasicObjectType of(TypeDescriptor type) {
        return of(type, type.getSimpleName(), null);
    }

    public static BasicObjectType of(TypeDescriptor type, @Nullable  Set<String> fieldNames) {
        return of(type, type.getSimpleName(), fieldNames);
    }

    public static BasicObjectType of(TypeDescriptor type, String alias) {
        return of(type, alias, null);
    }

    public static BasicObjectType of(TypeDescriptor type, String alias, @Nullable Set<String> fieldNames) {
        Map<String, FieldDescriptor> fields = type.getFields();
        if (fields != null) {
            fields = Maps.filterEntries(fields, entry -> fieldNames.contains(entry.getKey()));
        }
        return of(type, alias, new ObjectConstructor(type), null, fields);
    }

    public static BasicObjectType of(TypeDescriptor type,
                                     String alias,
                                     ObjectConstructor constructor,
                                     ObjectIdentifier identifier,
                                     Map<String, ? extends Property> properties) {
        if (identifier != null) {
            return new Identifiable(type, alias, constructor, identifier, properties);
        } else {
            return new BasicObjectType(type, alias, constructor, null, properties);
        }
    }

    private final Map<String, Property> properties;

    private final ObjectConstructor constructor;

    private final ObjectIdentifier identifier;

    private final String alias;

    private final TypeDescriptor type;

    private BasicObjectType(TypeDescriptor type,
                            String alias,
                            ObjectConstructor constructor,
                            ObjectIdentifier identifier,
                            Map<String, ? extends Property> properties) {
        this.type = Check.notNull(type, "type");
        this.alias = Check.notNullOrEmpty(alias, "alias");
        this.constructor = constructor;
        this.identifier = identifier;
        this.properties = ImmutableMap.copyOf(properties);
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object valueObject, ReadContext context) throws Exception {
        Object[] params = constructor.newParametersArray();
        int i=0;
        for (String param : constructor.getParameters()) {
            PropertyTree child = propertyTree.get(NodeId.property(param));
            Object value = child != null ? context.getObject(child) : null;
            params[i++] = value;
        }
        if (constructor.hasParameters()) {
            // Ensure that constructor parameters are bound
            context.bindAll();
        }
        Object object = constructor.newInstance(params);
        if (identifier != null && !constructor.hasParameter(identifier.name) && identifier.property.isWritable()) {
            PropertyTree child = propertyTree.get(NodeId.property(identifier.name));
            if (child != null) {
                Object value = context.getObject(child);
                if (value != null) {
                    identifier.property.set(object, value);
                }
            }
        }
        return object;
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
        for (PropertyTree child : propertyTree.getChildren()) {
            NodeId nodeId = child.getNodeId();
            if (nodeId.isKey() && !constructor.hasParameter(nodeId.getKey())) {
                Property property = properties.get(nodeId.getKey());
                if (property != null && property.isWritable()) {
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

        if (identifier != null && identifier.property.isWritable()) {
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


    private static class Identifiable extends BasicObjectType implements IdentifiableType {
        private Identifiable(TypeDescriptor type,
                             String alias,
                             ObjectConstructor constructor,
                             ObjectIdentifier identifier,
                             Map<String, ? extends Property> properties) {
            super(type, alias, constructor, identifier, properties);
        }
    }

}
