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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.javersion.util.Check;

import com.google.common.collect.ImmutableList;

public final class ConstructorDescriptor extends AbstractMethodDescriptor<Constructor<?>> implements StaticExecutable {

    @Nonnull
    private final Constructor<?> constructor;

    public ConstructorDescriptor(TypeDescriptor declaringType, Constructor<?> constructor) {
        super(declaringType);
        this.constructor = Check.notNull(constructor, "method");
        constructor.setAccessible(true);
    }

    public List<ParameterDescriptor> getParameters() {
        ImmutableList.Builder<ParameterDescriptor> builder = ImmutableList.builder();
        int index = 0;
        for (Parameter parameter : constructor.getParameters()) {
            builder.add(new ParameterDescriptor(this, parameter, index++));
        }
        return builder.build();
    }

    @Override
    public Object invokeStatic(Object... args) {
        return newInstance(args);
    }

    public Object newInstance(Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ConstructorDescriptor) {
            ConstructorDescriptor other = (ConstructorDescriptor) obj;
            return this.declaringType.equals(other.declaringType) &&
                    this.constructor.equals(other.constructor);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * declaringType.hashCode() + Arrays.hashCode(constructor.getParameterTypes());
    }

    public boolean applies(TypeDescriptor typeDescriptor) {
        return typeDescriptor.equalTo(constructor.getDeclaringClass());
    }

    public String toString() {
        return toString(-1);
    }

    @Override
    String toString(int hilightParameter) {
        return getDeclaringType().toString() +
                getParameters().stream()
                        .map(parameterDescriptor -> parameterToString(parameterDescriptor, hilightParameter))
                        .collect(Collectors.joining(",", "(", ")"));
    }

    @Override
    public Constructor<?> getElement() {
        return constructor;
    }
}
