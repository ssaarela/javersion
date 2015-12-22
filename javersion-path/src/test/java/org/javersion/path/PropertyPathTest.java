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
import static java.lang.Long.MIN_VALUE;
import static java.util.Arrays.asList;
import static org.javersion.path.PropertyPath.ROOT;
import static org.javersion.path.PropertyPath.parse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.*;
import org.javersion.path.PropertyPath.Key;
import org.javersion.path.PropertyPath.Property;
import org.javersion.path.PropertyPath.SubPath;
import org.junit.Assert;
import org.junit.Test;

public class PropertyPathTest {

    @Test
    public void Path_Equals() {
        assertThat( children_0_name()).isEqualTo(children_0_name());
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

        Assertions.assertThat(paths.contains(parents_0_name)).isFalse();
        Assertions.assertThat(paths.contains(children)).isFalse();
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

    @Test
    public void any_property() {
        assertThat(parse("object.*")).isEqualTo(ROOT.property("object").anyProperty());
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
    public void asterisk() {
        assertThat(parse("map*.key")).isEqualTo(ROOT.property("map").any().property("key"));
    }

    @Test
    public void path() {
        PropertyPath path = parents_0.path(parents_1_name);
        assertThat(path.toString()).isEqualTo("parents[0].parents[1].name");
    }

    @Test
    public void property_does_not_equal_key() {
        Property property = ROOT.property("property");
        Key key = ROOT.key("property");
        assertThat(property).isNotEqualTo(key);
        assertThat(key).isNotEqualTo(property);
    }

    @Test
    public void key_or_index() {
        assertThat(ROOT.keyOrIndex("key").getNodeId().isKey()).isTrue();
        assertThat(ROOT.keyOrIndex(123).getNodeId().isIndex()).isTrue();
    }

    @Test
    public void any_toString() {
        assertThat(ROOT.any().toString()).isEqualTo("*");
    }

    @Test
    public void any_key_toString() {
        assertThat(ROOT.anyKey().toString()).isEqualTo("{}");
    }

    @Test
    public void any_property_toString() {
        assertThat(ROOT.anyProperty().toString()).isEqualTo(".*");
    }

    @Test
    public void index_id_toString() {
        assertThat(NodeId.index(567).toString()).isEqualTo("567");
    }

    @Test
    public void root_with_parent() {
        Property path = ROOT.property("property");
        assertThat(ROOT.withParent(path)).isEqualTo(path);
    }

    @Test
    public void key_with_parent() {
        PropertyPath key = ROOT.key("key");
        assertThat(ROOT.path(key)).isEqualTo(key);
    }

    @Test
    public void name_of_any_property() {
        PropertyPath key = ROOT.anyProperty().property("name");
        assertThat(ROOT.path(key)).isEqualTo(key);
    }

    @Test
    public void any_property_with_parent() {
        PropertyPath anyProperty = ROOT.anyProperty();
        assertThat(ROOT.path(anyProperty)).isEqualTo(anyProperty);
    }

    @Test
    public void schema_path_of_key() {
        assertThat(ROOT.key("key").toSchemaPath()).isEqualTo(ROOT.anyKey());
    }

    @Test
    public void schema_path_of_property() {
        assertThat(ROOT.property("property").toSchemaPath()).isEqualTo(ROOT.property("property"));
    }

    @Test
    public void schema_path_of_a_schema_path_is_the_same_path() {
        PropertyPath path = ROOT.property("list").index(123).key("key").property("property").toSchemaPath();
        assertThat(path.toString()).isEqualTo("list[]{}.property");
        assertThat(path.toSchemaPath()).isEqualTo(path);
    }

    @Test
    public void any_with_parent() {
        PropertyPath any = ROOT.any();
        Property path = ROOT.property("property");
        assertThat(path.path(any)).isEqualTo(path.any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void key_or_index_of_unsupported_type() {
        ROOT.keyOrIndex(new Object());
    }

    @Test
    public void append_any() {
        assertThat(ROOT.node(NodeId.ANY)).isEqualTo(ROOT.any());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void get_index_of_any_index() {
        NodeId.ANY_INDEX.getIndex();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void get_key_of_any_key() {
        NodeId.ANY_KEY.getKey();
    }

    @Test
    public void key_or_index_of_NodeId() {
        assertThat(NodeId.index(123).getKeyOrIndex()).isEqualTo(123l);
        assertThat(NodeId.keyOrIndex("key").getKeyOrIndex()).isEqualTo("key");
    }

    @Test
    public void negative_long_index() {
        String str = PropertyPath.ROOT.property("numbers").index(MIN_VALUE).toString();
        assertThat(str).isEqualTo("numbers[-9223372036854775808]");
        PropertyPath path = PropertyPath.parse(str);
        assertThat(path.getNodeId()).isEqualTo(NodeId.index(-9223372036854775808l));
    }

    @Test
    public void Full_Path() {
        List<SubPath> fullPath = children_0_name().getFullPath();
        assertThat(fullPath).hasSize(3);
        assertThat(fullPath.get(0)).isEqualTo(children);
        assertThat(fullPath.get(1)).isEqualTo((PropertyPath) children_0());
    }

    @Test
    public void construct_from_basic_nodes() {
        PropertyPath path = ROOT.property("property").key("key").index(1);
        testConstructionFromNodes(path);
    }

    @Test
    public void construct_from_special_nodes() {
        PropertyPath path = ROOT.anyProperty().anyKey().anyIndex().any();
        testConstructionFromNodes(path);
    }

    private void testConstructionFromNodes(PropertyPath path) {
        PropertyPath nodePath = ROOT;
        for (SubPath element : path.asList()) {
            nodePath = nodePath.node(element.nodeId);
        }
        assertThat(nodePath).isEqualTo(path);
        assertThat(nodePath.toString()).isEqualTo(path.toString());
    }

    @Test
    public void compare() {
        PropertyPath[] sorted = {
                parse("b.a"),
                parse("a[]"),
                parse("a{}"),
                parse("a.*"),
                parse("a*"),
                parse("a[2]"),
                parse("a[1]"),
                parse("a.b"),
                parse("a[\"b\"]"),
                parse("a[\"a\"]"),
                parse("a.a"),
                parse("[]"),
                parse("{}"),
                parse(".*"),
                parse("*"),
                parse("[2]"),
                parse("[1]"),
                parse("b"),
                parse("[\"b\"]"),
                parse("[\"a\"]"),
                parse("a")
        };
        Arrays.sort(sorted);

        assertThat(asList(sorted)).isEqualTo(asList(
                parse("*"),
                parse("[]"),
                parse(".*"),
                parse("{}"),
                parse("[1]"),
                parse("[2]"),
                parse("a"),
                parse("a*"),
                parse("a[]"),
                parse("a.*"),
                parse("a{}"),
                parse("a[1]"),
                parse("a[2]"),
                parse("a.a"),
                parse("a.b"),
                parse("a[\"a\"]"),
                parse("a[\"b\"]"),
                parse("b"),
                parse("b.a"),
                parse("[\"a\"]"),
                parse("[\"b\"]")
        ));
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

    public static <K, V> MapAssert<K, V> assertThat(Map<K, V> actual) {
        return Assertions.assertThat(actual);
    }

    public static AbstractCharSequenceAssert<?, String> assertThat(String actual) {
        return Assertions.assertThat(actual);
    }

    public static AbstractObjectAssert<?, Object> assertThat(Object path) {
        return Assertions.<Object>assertThat((Object) path);
    }

    public static <T> AbstractListAssert<?, ? extends List<? extends T>, T> assertThat(List<? extends T> actual) {
        return Assertions.<T>assertThat(actual);
    }

    public static AbstractBooleanAssert<?> assertThat(boolean actual) {
        return Assertions.assertThat(actual);
    }

}
