package org.javersion.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.List;

import javax.annotation.Nonnull;

import org.javersion.util.Check;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;

public abstract class MemberDescriptor implements ElementDescriptor {

    @Nonnull
    protected final TypeDescriptor declaringType;

    public MemberDescriptor(TypeDescriptor declaringType) {
        this.declaringType = Check.notNull(declaringType, "declaringType");
    }

    @Override
    public List<Annotation> getAnnotations() {
        return ImmutableList.copyOf(getElement().getAnnotations());
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return getElement().getAnnotation(annotationClass);
    }

    @Override
    public <A extends Annotation> boolean hasAnnotation(Class<A> annotationClass) {
        return getElement().isAnnotationPresent(annotationClass);
    }

    public final TypeDescriptor getDeclaringType() {
        return declaringType;
    }

    TypeToken<?> getSourceType() {
        return declaringType.getTypeToken();
    }

    TypeDescriptor resolveType(Type genericType) {
        TypeToken<?> resolveType = getSourceType().resolveType(genericType);
        return getTypeDescriptor(resolveType);
    }

    TypeDescriptor getTypeDescriptor(TypeToken typeToken) {
        return getTypeDescriptors().get(typeToken);
    }

    TypeDescriptors getTypeDescriptors() {
        return declaringType.getTypeDescriptors();
    }

    abstract AnnotatedElement getElement();

    public abstract int hashCode();

    public abstract boolean equals(Object obj);

}
