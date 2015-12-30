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

import java.lang.reflect.Parameter;

import javax.annotation.Nonnull;

import org.javersion.util.Check;

public final class ParameterDescriptor extends MemberDescriptor {

    @Nonnull
    private final Parameter parameter;

    public ParameterDescriptor(TypeDescriptor declaringType, Parameter parameter) {
        super(declaringType);
        this.parameter = Check.notNull(parameter, "parameter");
    }

    @Override
    Parameter getElement() {
        return parameter;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ParameterDescriptor) {
            ParameterDescriptor other = (ParameterDescriptor) obj;
            return this.parameter.equals(other.parameter) &&
                    this.declaringType.equals(other.declaringType);
        } else {
            return false;
        }
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
        throw new ReflectionException("Accessing parameter names requires either @Param annotation or compiling with -parameters option");
    }

    @Override
    public int hashCode() {
        return 31 * getDeclaringType().hashCode() + parameter.hashCode();
    }
}
