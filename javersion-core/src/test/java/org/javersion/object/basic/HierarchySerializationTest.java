package org.javersion.object.basic;

import static org.hamcrest.Matchers.equalTo;
import static org.javersion.path.PropertyPath.ROOT;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.javersion.object.Versionable;
import org.javersion.path.PropertyPath;
import org.junit.Test;

import com.google.common.collect.Maps;

public class HierarchySerializationTest {
    
    @Versionable
    public static class Tree {
        public String name;
        public Tree parent;
        public Tree(String name) {
            this.name = name;
        }
    }
    
    @Versionable
    public static class BiTree {
        public Tree first;
        public Tree second;
    }
    
    
    private static final BasicObjectSerializer<Tree> treeSerializer = 
            new BasicObjectSerializer<>(Tree.class);
            
    private static final BasicObjectSerializer<BiTree> biTreeSerializer = 
            new BasicObjectSerializer<>(BiTree.class);
    
    @Test
    public void Hierarchy() {
        Tree leaf;
        leaf = new Tree("leaf");
        leaf.parent = new Tree("parent");
        leaf.parent.parent = new Tree("grandparent");
        
        Map<PropertyPath, Object> properties = treeSerializer.toMap(leaf);
        
        Map<PropertyPath, Object> expectedProperties = properties(
                ROOT, Tree.class,
                property("name"), "leaf",
                property("parent"), Tree.class,
        
                property("parent.name"), "parent",
                property("parent.parent"), Tree.class,
        
                property("parent.parent.name"), "grandparent",
                property("parent.parent.parent"), null
        );
        
        assertThat(properties, equalTo(expectedProperties));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void Illegal_Cycle() {
        Tree root;
        root = new Tree("leaf");
        root.parent = new Tree("parent");
        root.parent.parent = root;

        treeSerializer.toMap(root);
    }

    @Test
    public void Null_References_Are_Not_Same() {
        BiTree biTree = new BiTree();

        biTreeSerializer.toMap(biTree);
    }
    
    public static Map<PropertyPath, Object> properties(Object... keysAndValues) {
        Map<PropertyPath, Object> map = Maps.newHashMap();
        for (int i=0; i < keysAndValues.length-1; i+=2) {
            map.put((PropertyPath) keysAndValues[i], keysAndValues[i+1]);
        }
        return map;
    }
    
    private static PropertyPath property(String path) {
        return PropertyPath.parse(path);
    }
}
