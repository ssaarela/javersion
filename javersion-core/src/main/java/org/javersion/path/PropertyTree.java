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
package org.javersion.path;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PropertyTree {

    public static PropertyTree build(PropertyPath...paths) {
        return build(Arrays.asList(paths));
    }
    public static PropertyTree build(Collection<PropertyPath> paths) {
        Map<PropertyPath, PropertyTree> nodes = Maps.newHashMapWithExpectedSize(paths.size());
        for (PropertyPath path : paths) {
            PropertyTree parentTree = null;
            for (PropertyPath subpath : path) {
                PropertyTree childTree = nodes.get(subpath);
                if (childTree == null) {
                    childTree = new PropertyTree(subpath);
                    nodes.put(subpath, childTree);
                }
                if (parentTree != null) {
                    parentTree.children.put(subpath.getName(), childTree);
                } 
                parentTree = childTree;
            }
        }
        return nodes.get(PropertyPath.ROOT);
    }
    
    public final PropertyPath path;
    
    private Map<String, PropertyTree> children = Maps.newLinkedHashMap();
    
    private PropertyTree(PropertyPath path) {
        this.path = path;
    }
    
    public String getName() {
        return path.getName();
    }
    
    public Collection<PropertyTree> getChildren() {
        return unmodifiableCollection(children.values());
    }
    
    public Map<String, PropertyTree> getChildrenMap() {
        return unmodifiableMap(children);
    }
    
    public PropertyTree get(String childNode) {
        return children.get(childNode);
    }
    public List<PropertyTree> postOrder() {
        List<PropertyTree> postOrder = Lists.newArrayList();
        postOrder.add(this);
        for (int i = 0; i < postOrder.size(); i++) {
            PropertyTree current = postOrder.get(i);
            postOrder.addAll(current.getChildren());
        }
        return Lists.reverse(postOrder);
    }

}
