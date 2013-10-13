package org.javersion.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

import com.google.common.collect.ImmutableList;

public abstract class AbstractElement {

    public AbstractElement() {
    }

    public List<Annotation> getAnnotations() {
        return ImmutableList.copyOf(getElement().getAnnotations());
    }
    
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return getElement().getAnnotation(annotationClass);
    }

    public <A extends Annotation> boolean hasAnnotation(Class<A> annotationClass) {
        return getElement().isAnnotationPresent(annotationClass);
    }

    public abstract AnnotatedElement getElement();

}
