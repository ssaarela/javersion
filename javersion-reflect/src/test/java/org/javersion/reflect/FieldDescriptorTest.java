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

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.reflect.TypeDescriptorTest.STATIC_FIELDS;
import static org.javersion.reflect.TypeDescriptorTest.TYPES;

import java.lang.reflect.Field;

import org.junit.Test;

public class FieldDescriptorTest {

    private static String staticField;

    private String privateField;

    private transient String transientField;

    @Deprecated
    private String deprecatedField;

    private static TypeDescriptor type = TYPES.get(FieldDescriptorTest.class);

    @Test
    public void Get_Success() {
        FieldDescriptor fieldDescriptor = getTYPESDescriptor();
        assertThat((TypeDescriptors) fieldDescriptor.getStatic()).isSameAs(TYPES);
    }

    @Test(expected=ReflectionException.class)
    public void Set_Final_Value() {
        getTYPESDescriptor().setStatic(null);
    }

    private FieldDescriptor getTYPESDescriptor() {
        FieldDescriptor fieldDescriptor = STATIC_FIELDS.get(TypeDescriptorTest.class).getField("TYPES");
        return fieldDescriptor;
    }

    @Test
    public void set_value() {
        FieldDescriptor fieldDescriptor = type.getField("privateField");
        fieldDescriptor.set(this, "Magic!");
        assertThat(privateField).isEqualTo("Magic!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void get_value_from_wrong_type() {
        FieldDescriptor fieldDescriptor = type.getField("privateField");
        fieldDescriptor.get(new Object());
    }

    @Test
    public void basic_private_field() {
        FieldDescriptor fieldDescriptor = type.getField("privateField");
        assertThat(fieldDescriptor.getName()).isEqualTo("privateField");
        assertThat(fieldDescriptor.getType().getRawType()).isEqualTo(String.class);
        assertThat(fieldDescriptor.getAnnotations()).isEmpty();
        assertThat(fieldDescriptor.isTransient()).isFalse();
        assertThat(fieldDescriptor.isStatic()).isFalse();
    }

    @Test
    public void element_is_field() {
        FieldDescriptor fieldDescriptor = type.getField("privateField");
        assertThat(fieldDescriptor.getElement()).isSameAs(fieldDescriptor.getElement());
    }

    @Test
    public void wraps_java_reflect_Field() throws NoSuchFieldException {
        Field field = FieldDescriptorTest.class.getDeclaredField("privateField");
        FieldDescriptor fieldDescriptor = type.getField("privateField");
        assertThat(fieldDescriptor.getElement()).isEqualTo(field);
        assertThat(fieldDescriptor.toString())
                .isEqualTo("org.javersion.reflect.FieldDescriptorTest.privateField");
    }

    @Test(expected = ReflectionException.class)
    public void illegal_access() {
        FieldDescriptor fieldDescriptor = type.getField("privateField");
        try {
            fieldDescriptor.getElement().setAccessible(false);
            fieldDescriptor.get(this);
        } finally {
            fieldDescriptor.getElement().setAccessible(true);
        }
    }

    @Test
    public void field_descriptor_from_another_TypeDescriptors_is_not_equal() {
        FieldDescriptor fieldDescriptor = type.getField("privateField");
        FieldDescriptor other = new TypeDescriptors().get(FieldDescriptorTest.class).getField("privateField");
        assertThat(fieldDescriptor.equals(other)).isFalse();
    }

    @Test
    public void equals() {
        FieldDescriptor fieldDescriptor = type.getField("privateField");
        assertThat(fieldDescriptor.equals(fieldDescriptor)).isTrue();
        assertThat(fieldDescriptor.equals(new Object())).isFalse();

        FieldDescriptor other = type.getField("transientField");
        assertThat(fieldDescriptor.equals(other)).isFalse();
    }

    @Test
    public void set_static_field() {
        staticField = null;
        FieldDescriptor fieldDescriptor = STATIC_FIELDS.get(FieldDescriptorTest.class).getField("staticField");
        fieldDescriptor.setStatic("static");
        assertThat(staticField).isEqualTo("static");
    }

    @Test
    public void inspect_annotations() {
        FieldDescriptor field = type.getField("deprecatedField");
        assertThat(field.getAnnotation(Deprecated.class)).isNotNull();
        assertThat(field.hasAnnotation(Deprecated.class)).isTrue();
        assertThat(field.getAnnotation(SuppressWarnings.class)).isNull();
        assertThat(field.hasAnnotation(SuppressWarnings.class)).isFalse();
        assertThat(field.getAnnotations()).hasSize(1);
    }
}
