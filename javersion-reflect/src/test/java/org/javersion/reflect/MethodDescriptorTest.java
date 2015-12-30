package org.javersion.reflect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.reflect.TypeDescriptors.DEFAULT;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

import nl.jqno.equalsverifier.EqualsVerifier;

public class MethodDescriptorTest {

    static class MySuperClass<T> {
        Set<T> method(T first, @Param("rest") Set<T> second) {
            return null;
        }
    }

    static class MyClass<T> extends MySuperClass<T> {
        @Deprecated
        @Override
        Set<T> method(T first, @Param("rest") Set<T> second) {
            second.add(first);
            return second;
        }

        @SuppressWarnings("unused")
        private void privateMethod() {}

    }

    @SuppressWarnings("unused")
    private MyClass<Integer> myClass;

    @Test
    public void return_type() {
        MethodDescriptor method = getMethodDescriptor();
        TypeDescriptor returnType = method.getReturnType();
        assertThat(returnType.getRawType()).isEqualTo(Set.class);
        assertThat(returnType.resolveGenericParameter(Set.class, 0).getRawType()).isEqualTo(Integer.class);
    }

    @Test
    public void first_parameter_type() {
        MethodDescriptor method = getMethodDescriptor();
        ParameterDescriptor parameter = method.getParameters().get(0);
        assertThat(parameter.getType().getRawType()).isEqualTo(Integer.class);
    }

    @Test
    public void second_parameter_type() {
        MethodDescriptor method = getMethodDescriptor();
        ParameterDescriptor parameter = method.getParameters().get(1);
        TypeDescriptor type = parameter.getType();
        assertThat(type.getRawType()).isEqualTo(Set.class);
        assertThat(type.resolveGenericParameter(Set.class, 0).getRawType()).isEqualTo(Integer.class);
    }

    @Test
    public void first_parameter_name() {
        MethodDescriptor method = getMethodDescriptor();
        ParameterDescriptor parameter = method.getParameters().get(0);
        assertThat(parameter.getName()).isEqualTo("first");
    }

    @Test
    public void second_parameter_name() {
        MethodDescriptor method = getMethodDescriptor();
        ParameterDescriptor parameter = method.getParameters().get(1);
        assertThat(parameter.getName()).isEqualTo("rest");
    }

    @Test
    public void invoke() {
        MyClass<Integer> myClass = new MyClass<>();
        MethodDescriptor method = getMethodDescriptor();
        @SuppressWarnings("unchecked")
        Set<Integer> result = (Set<Integer>) method.invoke(myClass, 123, new HashSet<>());
        assertThat(result).isEqualTo(ImmutableSet.of(123));
    }

    @Test
    public void identity() {
        Map<MethodSignature, MethodDescriptor> methods = getTypeDescriptor().getMethods();
        EqualsVerifier.forClass(MethodDescriptor.class)
                .withPrefabValues(Method.class,
                        methods.get(new MethodSignature("equals", Object.class)).getElement(),
                        methods.get(new MethodSignature("hashCode")).getElement())
                .verify();
    }

    @Test
    public void parameter_identity() {
        List<ParameterDescriptor> parameters = getMethodDescriptor().getParameters();
        EqualsVerifier.forClass(ParameterDescriptor.class)
                .withPrefabValues(Parameter.class, parameters.get(0).getElement(), parameters.get(1).getElement())
                .verify();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invoke_without_parameters() {
        MyClass<Integer> myClass = new MyClass<>();
        getMethodDescriptor().invoke(myClass);
    }

    @Test(expected = NullPointerException.class)
    public void invoke_without_object() {
        getMethodDescriptor().invoke(null, 123, new HashSet<>());
    }

    @Test
    public void method_name() {
        assertThat(getMethodDescriptor().getName()).isEqualTo("method");
    }

    @Test
    public void applies() {
        assertThat(getMethodDescriptor().applies(getTypeDescriptor())).isTrue();
        assertThat(getMethodDescriptor().applies(DEFAULT.get(MethodDescriptorTest.class))).isFalse();
    }

    @Test(expected = ReflectionException.class)
    public void illegal_access() {
        MethodDescriptor method = getTypeDescriptor().getMethods().get(new MethodSignature("privateMethod"));
        try {
            method.getElement().setAccessible(false);
            method.invoke(null, 123, new HashSet<>());
        } finally {
            method.getElement().setAccessible(true);
        }
    }

    @Test
    public void method_annotations() {
        MethodDescriptor method = getMethodDescriptor();
        method.hasAnnotation(Deprecated.class);
        assertThat(method.getAnnotation(Deprecated.class)).isInstanceOf(Deprecated.class);
        List<Annotation> annotations = method.getAnnotations();
        assertThat(annotations).hasSize(1);
        assertThat(annotations.get(0)).isInstanceOf(Deprecated.class);

    }

    @Test
    public void to_string() {
        String str = getMethodDescriptor().toString();
        assertThat(str).isEqualTo("MethodDescriptorTest$MyClass.method(Integer,Set)");
    }

    @Test
    public void MethodSignature_toString() {
        String str = new MethodSignature(getMethodDescriptor().getElement()).toString();
        assertThat(str).isEqualTo("method(Object,Set)");
    }

    private static MethodDescriptor getMethodDescriptor() {
        return getTypeDescriptor().getMethods().get(new MethodSignature("method", Object.class, Set.class));
    }

    private static TypeDescriptor getTypeDescriptor() {
        TypeDescriptor type = DEFAULT.get(MethodDescriptorTest.class);
        FieldDescriptor myClassField = type.getField("myClass");
        return myClassField.getType();
    }
}
