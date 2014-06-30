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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Predicate;

public class TypeDescriptorTest {

    static final TypeDescriptors TYPES = new TypeDescriptors();
    
    static final TypeDescriptors STATIC_FIELDS = new TypeDescriptors(new Predicate<Field>() {
        @Override
        public boolean apply(Field input) {
            return Modifier.isStatic(input.getModifiers());
        }
    });

    public static class Cycle {
        Cycle cycle;
    }
    
    public static class Generic {
        Map<String, Long> map;
        Map<String, Map<String, Long>> mapOfMaps;
    }
    
    private final Class<?>[] expectedSuperClasses = {
            LinkedHashMap.class,
            HashMap.class,
            AbstractMap.class,
            Object.class
    };
    
    private final Class<?>[] expectedInterfaces = {
            Map.class,
            Cloneable.class, 
            Serializable.class
    };
    
    @Test
    public void Get_Super_Classes() {
        TypeDescriptor type = TYPES.get(LinkedHashMap.class);
        Set<Class<?>> superClasses = type.getSuperClasses();
        assertThat(superClasses, contains(expectedSuperClasses));
    }
    
    @Test
    public void Get_Interfaces() {
        TypeDescriptor type = TYPES.get(LinkedHashMap.class);
        Set<Class<?>> superClasses = type.getInterfaces();
        assertThat(superClasses, contains(expectedInterfaces));
    }
    
    @Test
    public void Get_Fields() {
        TypeDescriptor type = TYPES.get(ArrayList.class);
        assertThat(type.getFields().keySet(), equalTo((Set<String>) newHashSet(
                "elementData", "size",
                "modCount")));
    }
    
    @Test
    public void Recursive_Identity() {
        TypeDescriptor type = TYPES.get(Cycle.class);
        FieldDescriptor field = type.getField("cycle");
        
        TypeDescriptor fieldType = field.getType();
        FieldDescriptor fieldTypeField = fieldType.getField("cycle");
        
        assertThat(type.hashCode(), equalTo(fieldType.hashCode()));
        assertThat(field.hashCode(), equalTo(fieldTypeField.hashCode()));
        
        assertThat(type, equalTo(fieldType));
        assertThat(field, equalTo(fieldTypeField));
    }
    
    @Test
    public void Generic_Identity() {
        TypeDescriptor type = TYPES.get(Generic.class);
        FieldDescriptor mapField = type.getField("map");
        FieldDescriptor mapOfMapsField = type.getField("mapOfMaps");
        
        assertThat(mapField.getType(), not(equalTo(mapOfMapsField.getType())));
        
        TypeDescriptor mapOfMapsValueType = mapOfMapsField.getType().resolveGenericParameter(Map.class, 1);
        assertThat(mapField.getType(), equalTo(mapOfMapsValueType));
    }
    
    @Test(expected=RuntimeException.class)
    public void Unmodifiable_All_Classes() {
        TYPES.get(LinkedHashMap.class).getAllClasses().add(TypeDescriptorTest.class);
    }
    
    @Test(expected=RuntimeException.class)
    public void Unmodifiable_Interfaces() {
        TYPES.get(LinkedHashMap.class).getInterfaces().add(TypeDescriptorTest.class);
    }
    
    @Test(expected=RuntimeException.class)
    public void Unmodifiable_Super_Classes() {
        TYPES.get(LinkedHashMap.class).getSuperClasses().add(TypeDescriptorTest.class);
    }
    
    @Test(expected=RuntimeException.class)
    public void Unmodifiable_Fields() {
        TYPES.get(ArrayList.class).getFields().remove("elementData");
    }
    
    @Test
    public void Resolve_Generic_Parameter() {
        FieldDescriptor fieldDescriptor = STATIC_FIELDS.get(getClass()).getField("TYPES");
        
        TypeDescriptor fieldType = fieldDescriptor.getType();
        
        assertThat(fieldType.resolveGenericParameter(AbstractTypeDescriptors.class, 0), 
                equalTo(STATIC_FIELDS.get(FieldDescriptor.class)));
        
        assertThat(fieldType.resolveGenericParameter(AbstractTypeDescriptors.class, 1), 
                equalTo(STATIC_FIELDS.get(TypeDescriptor.class)));
    }
    
}
