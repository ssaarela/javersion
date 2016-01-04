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

import static com.google.common.collect.Sets.newHashSetWithExpectedSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.javersion.core.Persistent;
import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;

import com.google.common.collect.ImmutableList;

public class SetType implements ValueType {

    public interface Key {
        NodeId toNodeId(Object object, WriteContext context);
    }

    private final static Persistent.Object CONSTANT = Persistent.object();

    private final List<Key> keys;

    public SetType(List<Key> keys) {
        this.keys = ImmutableList.copyOf(keys);
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        return newSet(propertyTree.getChildren().size());
    }

    protected Set<Object> newSet(int size) {
        return newHashSetWithExpectedSize(size);
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
        @SuppressWarnings("unchecked")
        Set<Object> set = (Set<Object>) object;
        List<Object> elements = new ArrayList<>();
        forEachElement(propertyTree, elementPath -> elements.add(context.getObject(elementPath)));
        context.bindAll();
        set.addAll(elements);
    }

    private void forEachElement(PropertyTree elementPath, Consumer<PropertyTree> action) {
        forEachElement(elementPath, action, 0);
    }

    private void forEachElement(PropertyTree elementPath, Consumer<PropertyTree> action, int currentLevel) {
        if (currentLevel < keys.size()) {
            elementPath.getChildren().forEach(child -> forEachElement(child, action, currentLevel + 1));
        } else {
            action.accept(elementPath);
        }
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        Set<?> set = (Set<?>) object;
        context.put(path, CONSTANT);

        for (Object element : set) {
            PropertyPath elementPath = path;
            for (Key key : keys) {
                NodeId keyNode = key.toNodeId(element, context);
                elementPath = elementPath.node(keyNode);
            }
            context.serialize(elementPath, element);
        }
    }

}
