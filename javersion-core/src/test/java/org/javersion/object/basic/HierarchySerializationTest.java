package org.javersion.object.basic;

import static org.hamcrest.Matchers.*;
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
        public Tree child;
        public Tree() {}
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
        Tree root;
        root = new Tree("root");
        root.child = new Tree("child");
        root.child.child = new Tree("grandchild");
        
        Map<PropertyPath, Object> properties = treeSerializer.toMap(root);
        
        Map<PropertyPath, Object> expectedProperties = properties(
                ROOT, Tree.class,
                property("name"), "root",
                property("child"), Tree.class,
        
                property("child.name"), "child",
                property("child.child"), Tree.class,
        
                property("child.child.name"), "grandchild",
                property("child.child.child"), null
        );
        
        assertThat(properties, equalTo(expectedProperties));
        
        root = treeSerializer.fromMap(properties);
        assertThat(root.name, equalTo("root"));
        assertThat(root.child.name, equalTo("child"));
        assertThat(root.child.child.name, equalTo("grandchild"));
        assertThat(root.child.child.child, nullValue());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void Illegal_Cycle() {
        Tree root;
        root = new Tree("root");
        root.child = new Tree("child");
        root.child.child = root;

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
