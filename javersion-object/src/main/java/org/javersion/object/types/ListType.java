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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.javersion.core.Persistent;
import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;

public class ListType implements ValueType {

    private final static Persistent.Array CONSTANT = Persistent.array();

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        SortedMap<NodeId, PropertyTree> children = propertyTree.getChildrenMap();
        List<Object> list = new ArrayList<>(children.size());
        for (Map.Entry<NodeId, PropertyTree> entry : children.entrySet()) {
            NodeId nodeId = entry.getKey();
            if (nodeId.isIndex()) {
                int index = (int) nodeId.getIndex();
                assert index >= list.size();
                Object element = context.getObject(entry.getValue());
                if (element != null) {
                    ensureSize(list, index);
                    list.add(index, element);
                }
            }
        }
        return list;
    }

    private void ensureSize(List<?> list, int targetSize) {
        while (list.size() < targetSize) {
            list.add(null);
        }
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {}

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        @SuppressWarnings("rawtypes")
        List list = (List) object;
        context.put(path, CONSTANT);
        int i=0;
        for (Object element : list) {
            context.serialize(path.index(i++), element);
        }
    }

}
