/*
 * Copyright 2017 Samppa Saarela
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
