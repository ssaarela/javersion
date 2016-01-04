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

import java.util.List;
import java.util.Map;

import org.javersion.core.Persistent;
import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.object.mapping.ObjectType;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;
import org.javersion.reflect.Property;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableMap;


public class PolymorphicObjectType implements ObjectType {

    public static PolymorphicObjectType of(BasicObjectType root,
                                           List<BasicObjectType> subclasses) {
        if (root.getIdentifier() != null) {
            return new Identifiable(root, subclasses);
        } else {
            return new PolymorphicObjectType(root, subclasses);
        }
    }

    private static class Identifiable extends PolymorphicObjectType implements IdentifiableType {
        private Identifiable(BasicObjectType root, List<BasicObjectType> subclasses) {
            super(root, subclasses);
        }
    }

    protected final BasicObjectType root;

    protected final Map<String, BasicObjectType> typesByAlias;

    protected final Map<Class<?>, BasicObjectType> typesByClass;

    private PolymorphicObjectType(BasicObjectType root, List<BasicObjectType> subclasses) {
        this.root = Check.notNull(root, "root");
        Check.notNullOrEmpty(subclasses, "subclasses");

        ImmutableMap.Builder<String, BasicObjectType> aliases = ImmutableMap.builder();
        ImmutableMap.Builder<Class<?>, BasicObjectType> classes = ImmutableMap.builder();

        aliases.put(root.getAlias(), root);
        classes.put(root.getType().getRawType(), root);

        for (BasicObjectType subclass : subclasses) {
            if (!subclass.getType().isSubTypeOf(root.getType())) {
                throw new IllegalArgumentException("Expected " + subclass.getType() +
                        " to be a subclass of the root entity " + root.getType());
            }
            aliases.put(subclass.getAlias(), subclass);
            classes.put(subclass.getType().getRawType(), subclass);
        }
        typesByAlias = aliases.build();
        typesByClass = classes.build();
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object valueObject, ReadContext context) throws Exception {
        String alias = ((Persistent.Object) valueObject).type;
        BasicObjectType objectType = typesByAlias.get(alias);
        if (objectType == null) {
            throw new IllegalArgumentException("Alias not found: " + alias);
        }
        return objectType.instantiate(propertyTree, valueObject, context);
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
        BasicObjectType objectType = getObjectType(object);
        objectType.bind(propertyTree, object, context);
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        BasicObjectType objectType = getObjectType(object);
        objectType.serialize(path, object, context);
    }

    @Override
    public ObjectIdentifier getIdentifier() {
        return root.getIdentifier();
    }

    @Override
    public Map<String, Property> getProperties() {
        return root.getProperties();
    }

    public NodeId toNodeId(Object object, WriteContext writeContext) {
        return getObjectType(object).toNodeId(object, writeContext);
    }

    public String toString() {
        return "PolymorphicObjectType of " + typesByAlias.values();
    }

    private BasicObjectType getObjectType(Object object) {
        BasicObjectType objectType = typesByClass.get(object.getClass());
        if (objectType == null) {
            throw new IllegalArgumentException("Versionable class not found: " + object.getClass());
        }
        return objectType;
    }

}
