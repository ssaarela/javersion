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
package org.javersion.object;

import java.util.List;

import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.Index;
import org.javersion.path.PropertyTree;

import com.google.common.collect.Lists;

public class ListType implements ValueType {

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        int size = (Integer) value;
        Object[] values = new Object[size];
        for (PropertyTree child : propertyTree.getChildren()) {
            int index = Integer.valueOf(((Index) child.path).index);
            Object element = context.getObject(child);
            values[index] = element;
        }
        return Lists.newArrayList(values);
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {}

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        @SuppressWarnings("rawtypes")
        List list = (List) object;
        context.put(path, list.size());
        for (int i=0; i < list.size(); i++) {
            context.serialize(path.index(i), list.get(i));
        }
    }

}
