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
import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.path.PropertyPath.ROOT;
import static org.javersion.path.PropertyPath.parse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.javersion.path.PropertyPath.Key;
import org.javersion.path.PropertyPath.Property;
import org.javersion.path.PropertyPath.SubPath;
import org.junit.Assert;
import org.junit.Test;

public class PropertyPathTest {

    @Test
    public void Path_Equals() {
        assertThat(children_0_name()).isEqualTo(children_0_name());
        assertThat(children_0()).isEqualTo(children_0());
        assertThat(children).isEqualTo(children);
        assertThat(ROOT).isEqualTo(ROOT);

        assertThat(children_0()).isNotEqualTo(children_0_name());
        assertThat(children_0_name()).isNotEqualTo(children_0());
        assertThat(parents_0_name).isNotEqualTo(parents_1_name);
        assertThat(_0).isNotEqualTo(_1);
        assertThat(children_0_name()).isNotEqualTo(parents_0_name);
    }

    @Test
    public void Hash_Code() {
        HashSet<PropertyPath> paths = newHashSet(
                ROOT,
                parents,
                children_0(),
                children_0_name()
                );
        assertThat(paths).isEqualTo(newHashSet(
                ROOT,
                parents,
                children_0(),
                children_0_name()
        ));

        assertThat(paths.contains(parents_0_name)).isFalse();
        assertThat(paths.contains(children)).isFalse();
    }

    @Test
    public void To_String() {
        assertThat(_0.toString()).isEqualTo("[0]");
        assertThat(children_0().toString()).isEqualTo("children[0]");
        assertThat(children_0_name().toString()).isEqualTo("children[0].name");
    }

    @Test
    public void Nested_Indexes() {
        assertThat(_1_0.toString()).isEqualTo("[1][0]");
    }

    @Test
    public void special_characters_in_key() {
        PropertyPath path = ROOT.property("map").key("ä \t \n \u00DC \\ \"'");
        assertThat(path.toString()).isEqualTo("map[\"\\u00E4 \\t \\n \\u00DC \\\\ \\\"\\'\"]");

        assertThat(parse(path.toString())).isEqualTo(path);
    }

    @Test
    public void valid_properties() {
        assertValidProperty("valid");
        assertValidProperty("$");
        assertValidProperty("_123");
        assertValidProperty("ääks");
        assertValidProperty("\u00DCber");
    }

    private void assertValidProperty(String property) {
        PropertyPath path = ROOT.property(property);
        assertThat(path.toString()).isEqualTo(property);
        assertThat(parse(path.toString())).isEqualTo(path);
    }

    @Test
    public void invalid_properties() {
        assertInvalidProperty("not valid");
        assertInvalidProperty("1a");
        assertInvalidProperty("[1]");
        assertInvalidProperty("[\"key\"]");
        assertInvalidProperty("~");
        assertInvalidProperty("a.b");
        assertInvalidProperty("a-b");
    }

    private void assertInvalidProperty(String invalid) {
        try {
            ROOT.property(invalid);
            Assert.fail("succeeded parsing invalid property: " + invalid);
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void any_key() {
        assertThat(parse("map{}")).isEqualTo(ROOT.property("map").anyKey());
    }

    @Test
    public void any_index() {
        assertThat(parse("list[]")).isEqualTo(ROOT.property("list").anyIndex());
    }

    @Test(expected = IllegalArgumentException.class)
    public void property_should_not_start_with_digit() {
        parse("123åäö");
    }

    @Test
    public void property_or_key() {
        assertThat(ROOT.propertyOrKey("åäö123")).isEqualTo(ROOT.property("åäö123"));
        assertThat(ROOT.propertyOrKey("123åäö")).isEqualTo(ROOT.key("123åäö"));
        assertThat(ROOT.propertyOrKey("not valid")).isEqualTo(ROOT.key("not valid"));
        assertThat(ROOT.propertyOrKey("isValid")).isEqualTo(ROOT.property("isValid"));
        assertThat(ROOT.propertyOrKey("a.b")).isEqualTo(ROOT.key("a.b"));
        assertThat(ROOT.propertyOrKey("a-b")).isEqualTo(ROOT.key("a-b"));
    }

    @Test
    public void parse_root() {
        assertThat(parse("")).isEqualTo(ROOT);
    }

    @Test(expected=IllegalArgumentException.class)
    public void Parse_Index() {
        parse("[index]");
    }

    @Test(expected=IllegalArgumentException.class)
    public void Parse_Illegal_Start_2() {
        parse(".property");
    }

    @Test(expected=IllegalArgumentException.class)
    public void Parse_Illegal_Index_1() {
        System.out.println(parse("list[[0]]"));
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
        assertThat(children_0_name().toSchemaPath().toString()).isEqualTo("children[].name");
        assertThat(_0.toSchemaPath()).isEqualTo(_1.toSchemaPath());
        assertThat(children_0().toSchemaPath()).isNotEqualTo(parents_0.toSchemaPath());

        PropertyPath any = ROOT.anyIndex();
        assertThat(any.toSchemaPath()).isSameAs(any);

        any = ROOT.anyKey();
        assertThat(any.toSchemaPath()).isSameAs(any);
    }

    @Test
    public void property_equals_key() {
        Property property = ROOT.property("property");
        Key key = ROOT.key("property");
        assertThat(property).isEqualTo(key);
        assertThat(key).isEqualTo(property);
        assertThat(property.hashCode()).isEqualTo(key.hashCode());
    }

    public void Full_Path() {
        List<SubPath> fullPath = children_0_name().getFullPath();
        assertThat(fullPath).hasSize(3);
        assertThat(fullPath.get(0)).isEqualTo(children);
        assertThat(fullPath.get(0)).isEqualTo((PropertyPath) children_0());
    }

    public static PropertyPath _0 = ROOT.index(0);

    public static PropertyPath _1 = ROOT.index(1);

    public static PropertyPath _1_0 = _1.index(0);

    public static PropertyPath children = ROOT.property("children");

    public static PropertyPath parents = ROOT.property("parents");

    private static final PropertyPath parents_0 = parents.index(0);

    public static PropertyPath parents_0_name = parents_0.property("name");

    public static PropertyPath parents_1_name = parents.index(1).property("name");

    public static SubPath children_0() {
        return children.index(0);
    }

    public static SubPath children_0_name() {
        return children_0().property("name");
    }
}
