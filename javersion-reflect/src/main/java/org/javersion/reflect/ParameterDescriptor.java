/*
 * Copyright 2015 Samppa Saarela
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

import com.thoughtworks.paranamer.Paranamer;
import org.javersion.util.Check;

import javax.annotation.Nonnull;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Parameter;
import java.util.stream.Collectors;

public final class ParameterDescriptor extends MemberDescriptor {

    @Nonnull
    private final AbstractMethodDescriptor methodDescriptor;

    @Nonnull
    private final Parameter parameter;

    private final int index;

    public ParameterDescriptor(AbstractMethodDescriptor methodDescriptor, Parameter parameter, int index) {
        super(methodDescriptor.getDeclaringType());
        this.methodDescriptor = methodDescriptor;
        this.parameter = Check.notNull(parameter, "parameter");
        this.index = index;
    }

    @Override
    Parameter getElement() {
        return parameter;
    }

    public TypeDescriptor getType() {
        return resolveType(parameter.getParameterizedType());
    }

    public String getName() {
        Param param = getAnnotation(Param.class);
        if (param != null) {
            return param.value();
        }
        if (parameter.isNamePresent()) {
            return parameter.getName();
        }
        Paranamer paranamer = getTypeDescriptors().getParanamer();
        String[] names = paranamer.lookupParameterNames(getAccessibleObject());
        return names.length > index ? names[index] : "arg" + index;
    }

    public int getIndex() {
        return index;
    }

    AbstractMethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    AccessibleObject getAccessibleObject() {
        return methodDescriptor.getElement();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParameterDescriptor that = (ParameterDescriptor) o;

        if (!methodDescriptor.equals(that.methodDescriptor)) return false;
        return parameter.equals(that.parameter);
    }

    @Override
    public int hashCode() {
        int result = methodDescriptor.hashCode();
        result = 31 * result + parameter.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return methodDescriptor.toString(index);
    }

}
