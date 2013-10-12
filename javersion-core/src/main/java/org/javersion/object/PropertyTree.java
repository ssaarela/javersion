package org.javersion.object;

import static java.util.Collections.unmodifiableMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

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
                    parentTree.children.put(subpath.node(), childTree);
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
    
    public Map<String, PropertyTree> getChildren() {
        return unmodifiableMap(children);
    }
    
    public PropertyTree get(String childNode) {
        return children.get(childNode);
    }

}
