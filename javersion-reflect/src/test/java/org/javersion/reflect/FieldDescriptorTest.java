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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.javersion.reflect.TypeDescriptorTest.STATIC_FIELDS;
import static org.javersion.reflect.TypeDescriptorTest.TYPES;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class FieldDescriptorTest {
    
    private String thisIs;

    @Test
    public void Get_Success() {
        FieldDescriptor fieldDescriptor = getTYPESDescriptor();
        assertThat((TypeDescriptors) fieldDescriptor.getStatic(), sameInstance(TYPES));
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
    public void Set() {
        FieldDescriptor fieldDescriptor = TYPES.get(FieldDescriptorTest.class).getField("thisIs");
        fieldDescriptor.set(this, "Magic!");
        assertThat(thisIs, equalTo("Magic!"));
    }
    
}
