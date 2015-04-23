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

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.NodeId;
import org.javersion.path.PropertyTree;
import org.javersion.util.Check;


public final class ReferenceType implements ScalarType {

    private final IdentifiableType identifiableType;

    private final PropertyPath targetRoot;

    public ReferenceType(IdentifiableType identifiableType, PropertyPath targetRoot) {
        this.identifiableType = Check.notNull(identifiableType, "keyType");
        this.targetRoot = Check.notNull(targetRoot, "targetRoot");
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        NodeId id = identifiableType.toNodeId(object, context);
        context.put(path, id.getKeyOrIndex());
        context.serialize(targetRoot.keyOrIndex(id), object);
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        return context.prepareObject(targetRoot.keyOrIndex(value));
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {}

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public NodeId toNodeId(Object object, WriteContext writeContext) {
        NodeId nodeId = identifiableType.toNodeId(object, writeContext);
        writeContext.serialize(targetRoot.keyOrIndex(nodeId), object);
        return nodeId;
    }

    @Override
    public Object fromNodeId(NodeId nodeId, ReadContext context) throws Exception {
        return instantiate(null, nodeId.getKeyOrIndex(), context);
    }
}
