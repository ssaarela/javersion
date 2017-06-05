package org.javersion.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.List;

public abstract class AbstractMethodDescriptor<T extends AccessibleObject & AnnotatedElement & Member> extends JavaMemberDescriptor<T> {

    AbstractMethodDescriptor(TypeDescriptor declaringType) {
        super(declaringType);
    }

    public abstract List<ParameterDescriptor> getParameters();

    abstract String toString(int hilightParameter);

    static String parameterToString(ParameterDescriptor parameter, int hilight) {
        if (parameter.getIndex() == hilight) {
            return "*" + parameter.getType().getSimpleName() + " " + parameter.getName() + "*";
        } else {
            return parameter.getType().getSimpleName();
        }
    }

}
