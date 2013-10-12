package org.javersion.core.object;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.javersion.object.PropertyPath.ROOT;
import static org.junit.Assert.assertThat;

import org.javersion.object.PropertyPath;
import org.junit.Test;

public class PropertyPathTest {

    @Test
    public void Path_Equals() {
        assertThat(children_0(), equalTo(children_0()));
        assertThat(children_0(), not(equalTo(children_0_name())));
        assertThat(_0, not(equalTo(_1)));
    }
    
    @Test
    public void Hash_Code() {
        assertThat(children_0().hashCode(), equalTo(children_0().hashCode()));
        assertThat(children_0().hashCode(), not(equalTo(children_0_name().hashCode())));
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
        assertThat(ROOT.index("index containing \\[\\]..").toString(), 
                equalTo("[index containing \\\\[\\\\\\]\\.\\.]"));
    }
    
    @Test
    public void Peculiar_Property() {
        assertThat(ROOT.property("property containing \\[\\].").toString(), 
                equalTo("property containing \\\\[\\\\\\]\\."));
    }
    
    @Test
    public void Schema_Path() {
        assertThat(children_0_name().toSchemaPath().toString(), equalTo("children[].name"));
        assertThat(_0.toSchemaPath(), equalTo(_1.toSchemaPath()));
    }
    
    public static PropertyPath _0 = ROOT.index("0");
    
    public static PropertyPath _1 = ROOT.index("1");
    
    public static PropertyPath _1_0 = _1.index("0");
    
    public static PropertyPath children() {
        return ROOT.property("children");
    }
    public static PropertyPath children_0() {
        return children().index("0");
    }

    public static PropertyPath children_0_name() {
        return children_0().property("name");
    }

}
