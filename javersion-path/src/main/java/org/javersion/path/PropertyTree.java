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
import static java.util.Collections.unmodifiableSortedMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.Maps;

public class PropertyTree {

    public static PropertyTree build(PropertyPath...paths) {
        return build(Arrays.asList(paths));
    }

    public static PropertyTree build(Collection<PropertyPath> paths) {
        Map<PropertyPath, PropertyTree> nodes = Maps.newHashMapWithExpectedSize(paths.size());
        for (PropertyPath path : paths) {
            PropertyTree parentTree = getOrCreate(PropertyPath.ROOT, nodes);
            for (PropertyPath subPath : path) {
                PropertyTree childTree = getOrCreate(subPath, nodes);
                parentTree.children.put(subPath.getNodeId(), childTree);
                parentTree = childTree;
            }
        }
        return nodes.get(PropertyPath.ROOT);
    }

    private static PropertyTree getOrCreate(PropertyPath path, Map<PropertyPath, PropertyTree> nodes) {
        PropertyTree childTree = nodes.get(path);
        if (childTree == null) {
            childTree = new PropertyTree(path);
            nodes.put(path, childTree);
        }
        return childTree;
    }

    public final PropertyPath path;

    private SortedMap<NodeId, PropertyTree> children = new TreeMap<>();

    private PropertyTree(PropertyPath path) {
        this.path = path;
    }

    public NodeId getNodeId() {
        return path.getNodeId();
    }

    public Collection<PropertyTree> getChildren() {
        return unmodifiableCollection(children.values());
    }

    public SortedMap<NodeId, PropertyTree> getChildrenMap() {
        return unmodifiableSortedMap(children);
    }

    public PropertyTree get(NodeId childNode) {
        return children.get(childNode);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public PropertyTree get(PropertyPath path) {
        PropertyTree match = this;
        for (PropertyPath node : path) {
            match = match.get(node.getNodeId());
            if (match == null) {
                throw new IllegalArgumentException("path not found: " + path);
            }
        }
        return match;
    }

    public String toString() {
        return path.toString();
    }
}
