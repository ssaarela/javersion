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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

import org.javersion.util.Check;

import com.google.common.collect.ImmutableList;

public abstract class ElementDescriptor<
        F extends AbstractFieldDescriptor<F, T, Ts>, 
        T extends AbstractTypeDescriptor<F, T, Ts>,
        Ts extends AbstractTypeDescriptors<F, T, Ts>> {

    protected final Ts typeDescriptors;

    public ElementDescriptor(Ts typeDescriptors) {
        this.typeDescriptors = Check.notNull(typeDescriptors, "typeDescriptors");
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

    public Ts getTypeDescriptors() {
        return typeDescriptors;
    }
    
    public abstract AnnotatedElement getElement();

    public abstract boolean equals(Object obj);
    
    public abstract int hashCode();

}
