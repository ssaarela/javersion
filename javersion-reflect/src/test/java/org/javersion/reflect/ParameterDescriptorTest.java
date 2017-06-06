package org.javersion.reflect;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

import static org.javersion.reflect.TypeDescriptors.DEFAULT;

public class ParameterDescriptorTest {

    static class MyClass {
        public MyClass(String name, Integer integer) {}
        public void method(MyClass myClass) {}
    }

    @Test
    public void constructor_parameter_toString() {
        TypeDescriptor type = DEFAULT.get(MyClass.class);
        ConstructorDescriptor constructor = type.getConstructors().get(new ConstructorSignature(String.class, Integer.class));
        List<ParameterDescriptor> parameters = constructor.getParameters();
        assertThat(parameters.get(0).toString()).isEqualTo("org.javersion.reflect.ParameterDescriptorTest$MyClass(*java.lang.String name*,java.lang.Integer)");
        assertThat(parameters.get(1).toString()).isEqualTo("org.javersion.reflect.ParameterDescriptorTest$MyClass(java.lang.String,*java.lang.Integer integer*)");
    }

    @Test
    public void method_parameter_toString() {
        TypeDescriptor type = DEFAULT.get(MyClass.class);
        MethodDescriptor method = type.getMethods().get(new MethodSignature("method", MyClass.class));
        List<ParameterDescriptor> parameters = method.getParameters();
        assertThat(parameters.get(0).toString()).isEqualTo("org.javersion.reflect.ParameterDescriptorTest$MyClass.method(*org.javersion.reflect.ParameterDescriptorTest$MyClass myClass*)");
    }

}
