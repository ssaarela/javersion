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

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.javersion.path.PropertyPath.ROOT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.javersion.path.PropertyPath.SubPath;
import org.junit.Test;

public class PropertyPathTest {

    @Test
    public void Path_Equals() {
        assertThat(children_0_name(), equalTo(children_0_name()));
        assertThat(children_0(), equalTo(children_0()));
        assertThat(children, equalTo(children));
        assertThat(ROOT, equalTo(ROOT));
        
        assertThat(children_0(), not(equalTo(children_0_name())));
        assertThat(children_0_name(), not(equalTo(children_0())));
        assertThat(parents_0_name, not(equalTo(parents_1_name)));
        assertThat(_0, not(equalTo(_1)));
        assertThat(children_0_name(), not(equalTo(parents_0_name)));
    }
    
    @Test
    public void Hash_Code() {
        HashSet<PropertyPath> paths = newHashSet(
                ROOT,
                parents,
                children_0(),
                children_0_name()
                );
        assertThat(paths, equalTo(newHashSet(
                ROOT,
                parents,
                children_0(),
                children_0_name()
                )));

        assertThat(paths, not(hasItem(parents_0_name)));
        assertThat(paths, not(hasItem(children)));
    }

    @Test
    public void To_String() {
        assertThat(_0.toString(), equalTo("[0]"));
        assertThat(children_0().toString(), equalTo("children[0]"));
        assertThat(children_0_name().toString(), equalTo("children[0].name"));
    }
    
    @Test
    public void Nested_Indexes() {
        assertThat(_1_0.toString(), equalTo("[1][0]"));
    }
    
    @Test
    public void Peculiar_Index() {
        PropertyPath path = ROOT.property("list").index("index containing \\ [ ] .");
        assertThat(path.toString(),  equalTo("list[index containing \\\\ \\[ \\] \\.]"));
        
        assertThat(PropertyPath.parse(path.toString()), equalTo(path));
    }
    
    @Test
    public void Peculiar_Property() {
        PropertyPath path = ROOT.property("property containing \\ [ ] .");
        assertThat(path.toString(), equalTo("property containing \\\\ \\[ \\] \\."));
        
        assertThat(PropertyPath.parse(path.toString()), equalTo(path));
    }
    
    public void Parse_Index() {
        PropertyPath.parse("[index]");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void Parse_Illegal_Start_2() {
        PropertyPath.parse(".property");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void Parse_Illegal_Index_1() {
        System.out.println(PropertyPath.parse("list[[index]]"));
    }
    
    @Test
    public void Starts_With() {
        assertTrue(_1.startsWith(ROOT));
        assertTrue(children_0_name().startsWith(children));
        assertTrue(children_0_name().startsWith(ROOT));
        assertTrue(children.startsWith(children));
        
        assertFalse(children.startsWith(children_0_name()));
        assertFalse(ROOT.startsWith(_0));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void Empty_Property_Name() {
        ROOT.property("");
    }
    
    @Test
    public void Schema_Path() {
        assertThat(children_0_name().toSchemaPath().toString(), equalTo("children[].name"));
        assertThat(_0.toSchemaPath(), equalTo(_1.toSchemaPath()));
        assertThat(children_0().toSchemaPath(), not(equalTo(parents_0.toSchemaPath())));
        
        PropertyPath emptyIndex = ROOT.index("");
        assertThat(emptyIndex.toSchemaPath(), sameInstance(emptyIndex));
    }

    public void Full_Path() {
        List<SubPath> fullPath = children_0_name().getFullPath();
        assertThat(fullPath, hasSize(3));
        assertThat(fullPath.get(0), equalTo(children));
        assertThat(fullPath.get(0), equalTo((PropertyPath) children_0()));
    }
    
    public static PropertyPath _0 = ROOT.index("0");
    
    public static PropertyPath _1 = ROOT.index("1");
    
    public static PropertyPath _1_0 = _1.index("0");

    public static PropertyPath children = ROOT.property("children");

    public static PropertyPath parents = ROOT.property("parents");

    private static final PropertyPath parents_0 = parents.index(0);
    
    public static PropertyPath parents_0_name = parents_0.property("name");
    
    public static PropertyPath parents_1_name = parents.index(1).property("name");
    
    public static SubPath children_0() {
        return children.index("0");
    }

    public static SubPath children_0_name() {
        return children_0().property("name");
    }
}
