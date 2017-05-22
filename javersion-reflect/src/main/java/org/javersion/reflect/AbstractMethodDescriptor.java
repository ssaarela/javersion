package org.javersion.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

public abstract class AbstractMethodDescriptor<T extends AccessibleObject & AnnotatedElement & Member> extends JavaMemberDescriptor<T> {

    AbstractMethodDescriptor(TypeDescriptor declaringType) {
        super(declaringType);
    }

}
