/*
 * Copyright 2016 Samppa Saarela
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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

public abstract class JavaMemberDescriptor<T extends AnnotatedElement & Member> extends MemberDescriptor {

    public JavaMemberDescriptor(TypeDescriptor declaringType) {
        super(declaringType);
    }

    @Override
    public abstract T getElement();

    public final boolean isAbstract() {
        return Modifier.isAbstract(getElement().getModifiers());
    }

    public final boolean isStatic() {
        return Modifier.isStatic(getElement().getModifiers());
    }

    public final boolean isFinal() {
        return Modifier.isFinal(getElement().getModifiers());
    }

    public final boolean isSynthetic() {
        return getElement().isSynthetic();
    }

    public final boolean isPublic() {
        return Modifier.isPublic(getElement().getModifiers());
    }

    public final boolean isProtected() {
        return Modifier.isProtected(getElement().getModifiers());
    }

    public final boolean isPrivate() {
        return Modifier.isPrivate(getElement().getModifiers());
    }

    public final boolean isPackagePrivate() {
        return !isPublic() && !isProtected() && !isPrivate();
    }
}
