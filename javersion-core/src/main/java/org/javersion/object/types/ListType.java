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

import static java.lang.Integer.valueOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;

public class ListType implements ValueType {

    private static final String CONSTANT = "List";

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        Map<String, PropertyTree> children = propertyTree.getChildrenMap();
        List<Object> list = new ArrayList<>(children.size());
        for (Map.Entry<String, PropertyTree> entry : children.entrySet()) {
            PropertyTree child = entry.getValue();
            int index = valueOf(entry.getKey());
            if (index > list.size()) {
                fill(list, index, children, context);
            }
            if (index == list.size()) {
                list.add(index, context.getObject(child));
            }
        }
        return list;
    }

    private void fill(List<Object> list, int targetSize, Map<String, PropertyTree> children, ReadContext context) {
        for (int i=list.size(); i < targetSize; i++) {
            PropertyTree child = children.get(Integer.toString(i));
            if (child == null) {
                list.add(i, null);
            } else {
                list.add(i, context.getObject(child));
            }
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

    @Override
    public boolean isReference() {
        return false;
    }

}
