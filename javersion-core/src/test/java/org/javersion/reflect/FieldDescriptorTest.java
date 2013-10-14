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
