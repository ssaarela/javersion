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

import java.util.Map;

import org.javersion.object.types.MapType;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.NodeId;
import org.javersion.path.PropertyTree;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class Schema implements ValueType {

    private ValueType valueType;

    private Map<NodeId, Schema> children = Maps.newHashMap();

    Schema() {
        this.valueType = null;
    }
    Schema(ValueType valueType) {
        this.valueType = Check.notNull(valueType, "valueType");
    }

    public ValueType getValueType() {
        return valueType;
    }

    public Schema getChild(NodeId nodeId) {
        return children.get(nodeId);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    Schema addChild(NodeId nodeId, Schema child) {
        children.put(nodeId, child);
        return child;
    }

    void setValueType(ValueType valueType) {
        Check.that(this.valueType == null, "valueType has been set already");
        this.valueType = Check.notNull(valueType, "valueType");
    }

    void lock() {
        children = ImmutableMap.copyOf(children);
    }

    boolean isLocked() {
        return children instanceof ImmutableMap;
    }

    public boolean isReference() {
        return valueType.isReference();
    }

    public boolean hasChild(NodeId nodeId) {
        return children.containsKey(nodeId);
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        return valueType.instantiate(propertyTree, value, context);
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
        valueType.bind(propertyTree, object, context);
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        valueType.serialize(path, object, context);
    }

}
