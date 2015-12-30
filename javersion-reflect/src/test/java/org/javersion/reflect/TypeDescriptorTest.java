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
package org.javersion.reflect;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.*;

import org.junit.Test;

public class TypeDescriptorTest {

    static final TypeDescriptors TYPES = new TypeDescriptors();

    @SuppressWarnings("unused")
    static final Map<String, Integer> MAP = new HashMap<>();

    static final TypeDescriptors STATIC_FIELDS =
            new TypeDescriptors(input -> Modifier.isStatic(input.getModifiers()));

    @Deprecated
    public static class Cycle {
        Cycle cycle;
        public Cycle() {
            throw new IllegalArgumentException();
        }
    }

    public enum E {
        EVAL
    }

    public static class Generic {
        Map<String, Long> map;
        Map<String, Map<String, Long>> mapOfMaps;
    }

    @Test
    public void Get_Fields() {
        TypeDescriptor type = TYPES.get(ArrayList.class);
        assertThat(type.getFields().keySet()).isEqualTo((Set<String>) newHashSet(
                "elementData", "size",
                "modCount"));
    }

    @Test
    public void to_string() {
        TypeDescriptor type = TYPES.get(Cycle.class);
        assertThat(type.toString()).isEqualTo("org.javersion.reflect.TypeDescriptorTest$Cycle");

        type = TYPES.get(TypeDescriptorTest.class);
        assertThat(type.toString()).isEqualTo("org.javersion.reflect.TypeDescriptorTest");
    }

    @Test
    public void simple_name() {
        TypeDescriptor type = TYPES.get(Cycle.class);
        assertThat(type.getSimpleName()).isEqualTo("TypeDescriptorTest$Cycle");

        type = TYPES.get(TypeDescriptorTest.class);
        assertThat(type.getSimpleName()).isEqualTo("TypeDescriptorTest");
    }

    @Test
    public void raw_type_equality_check() {
        TypeDescriptor setType = TYPES.get(Set.class);
        assertThat(setType.equalTo(Set.class)).isTrue();
        assertThat(setType.equalTo(Collection.class)).isFalse();
    }

    @Test
    public void field_existence_check() {
        TypeDescriptor type = TYPES.get(FieldDescriptorTest.class);
        assertThat(type.hasField("privateField")).isTrue();
        assertThat(type.hasField("foobar")).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void field_not_found() {
        TypeDescriptor type = TYPES.get(FieldDescriptorTest.class);
        type.getField("foobar");
    }

    @Test
    public void get_element_returns_raw_type() {
        assertThat(TYPES.get(Set.class).getRawType()).isEqualTo(Set.class);
    }

    @Test
    public void create_instance() {
        TypeDescriptor type = TYPES.get(TypeDescriptorTest.class);
        TypeDescriptorTest instance = (TypeDescriptorTest) type.newInstance();
        assertThat(instance).isNotSameAs(this);
    }

    @Test(expected = ReflectionException.class)
    public void constructor_not_found() {
        TypeDescriptor type = TYPES.get(FieldDescriptor.class);
        type.newInstance();
    }

    @Test(expected = ReflectionException.class)
    public void construction_exception() {
        TypeDescriptor type = TYPES.get(Cycle.class);
        type.newInstance();
    }

    @Test
    public void enum_check() {
        TypeDescriptor type = TYPES.get(FieldDescriptor.class);
        assertThat(type.isEnum()).isFalse();

        type = TYPES.get(E.class);
        assertThat(type.isEnum()).isTrue();
    }

    @Test
    public void super_type_check() {
        TypeDescriptor setType = TYPES.get(Set.class);
        assertThat(setType.isSuperTypeOf(Collection.class)).isFalse();
        assertThat(setType.isSuperTypeOf(Set.class)).isTrue();
        assertThat(setType.isSuperTypeOf(SortedSet.class)).isTrue();
        assertThat(setType.isSuperTypeOf(TreeSet.class)).isTrue();
    }

    @Test
    public void sub_type_check() {
        TypeDescriptor setType = TYPES.get(Set.class);
        assertThat(setType.isSubTypeOf(Collection.class)).isTrue();
        assertThat(setType.isSubTypeOf(Set.class)).isTrue();
        assertThat(setType.isSubTypeOf(SortedSet.class)).isFalse();
    }

    @Test
    public void not_equal() {
        assertThat(TypeDescriptors.getTypeDescriptor(Object.class)).isNotEqualTo(new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void get_type_descriptor_of_null() {
        TypeDescriptors.getTypeDescriptor(null);
    }

    @Test
    public void primitive_or_wrapper() {
        TypeDescriptor type = TYPES.get(Cycle.class);
        assertThat(type.isPrimitiveOrWrapper()).isFalse();

        type = TYPES.get(int.class);
        assertThat(type.isPrimitiveOrWrapper()).isTrue();

        type = TYPES.get(Boolean.class);
        assertThat(type.isPrimitiveOrWrapper()).isTrue();

        type = TYPES.get(String.class);
        assertThat(type.isPrimitiveOrWrapper()).isFalse();
    }

    @Test
    public void Recursive_Identity() {
        TypeDescriptor type = TYPES.get(Cycle.class);
        FieldDescriptor field = type.getField("cycle");

        TypeDescriptor fieldType = field.getType();
        FieldDescriptor fieldTypeField = fieldType.getField("cycle");

        assertThat(type.hashCode()).isEqualTo(fieldType.hashCode());
        assertThat(field.hashCode()).isEqualTo(fieldTypeField.hashCode());

        assertThat(type).isEqualTo(fieldType);
        assertThat(field).isEqualTo(fieldTypeField);
    }

    @Test
    public void Generic_Identity() {
        TypeDescriptor type = TYPES.get(Generic.class);
        FieldDescriptor mapField = type.getField("map");
        FieldDescriptor mapOfMapsField = type.getField("mapOfMaps");

        assertThat(mapField.getType()).isNotEqualTo(mapOfMapsField.getType());

        TypeDescriptor mapOfMapsValueType = mapOfMapsField.getType().resolveGenericParameter(Map.class, 1);
        assertThat(mapField.getType()).isEqualTo(mapOfMapsValueType);
    }

    @Test(expected=RuntimeException.class)
    public void Unmodifiable_Fields() {
        TYPES.get(ArrayList.class).getFields().remove("elementData");
    }

    @Test
    public void Resolve_Generic_Parameter() {
        FieldDescriptor fieldDescriptor = STATIC_FIELDS.get(getClass()).getField("MAP");

        TypeDescriptor fieldType = fieldDescriptor.getType();

        assertThat(fieldType.resolveGenericParameter(Map.class, 0))
            .isEqualTo(STATIC_FIELDS.get(String.class));

        assertThat(fieldType.resolveGenericParameter(Map.class, 1))
                .isEqualTo(STATIC_FIELDS.get(Integer.class));
    }

    @Test
    public void annotations() {
        TypeDescriptor type = TYPES.get(Cycle.class);
        assertThat(type.hasAnnotation(Deprecated.class)).isTrue();
        assertThat(type.getAnnotation(Deprecated.class)).isInstanceOf(Deprecated.class);

        List<Annotation> annotations = type.getAnnotations();
        assertThat(annotations).hasSize(1);
        assertThat(annotations.get(0)).isInstanceOf(Deprecated.class);
    }
}
