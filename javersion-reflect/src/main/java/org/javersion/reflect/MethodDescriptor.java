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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.javersion.util.Check;

import com.google.common.collect.ImmutableList;

public final class MethodDescriptor extends AbstractMethodDescriptor<Method> implements StaticExecutable {

    @Nonnull
    private final Method method;

    public MethodDescriptor(TypeDescriptor declaringType, Method method) {
        super(declaringType);
        this.method = Check.notNull(method, "method");
        method.setAccessible(true);
    }

    public TypeDescriptor getReturnType() {
        return resolveType(method.getGenericReturnType());
    }

    public Class<?> getRawReturnType() {
        return method.getReturnType();
    }

    public List<ParameterDescriptor> getParameters() {
        ImmutableList.Builder<ParameterDescriptor> builder = ImmutableList.builder();
        int index = 0;
        for (Parameter parameter : method.getParameters()) {
            builder.add(new ParameterDescriptor(this, parameter, index++));
        }
        return builder.build();
    }

    public int getParameterCount() {
        return method.getParameterCount();
    }

    public String getName() {
        return method.getName();
    }

    public List<Class<?>> getParameterTypes() {
        return ImmutableList.copyOf(method.getParameterTypes());
    }

    @Override
    public Object invokeStatic(Object... args) {
        return invoke(null, args);
    }

    public Object invoke(Object object, Object... args) {
        try {
            return method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof MethodDescriptor) {
            MethodDescriptor other = (MethodDescriptor) obj;
            return this.declaringType.equals(other.declaringType) &&
                    this.method.equals(other.method);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * declaringType.hashCode() + method.hashCode();
    }

    public boolean applies(TypeDescriptor typeDescriptor) {
        return typeDescriptor.isSubTypeOf(method.getDeclaringClass());
    }

    public String toString() {
        return toString(-1);
    }

    @Override
    String toString(int hilightParameter) {
        return getDeclaringType().getName() + "." + getName() +
                getParameters().stream()
                        .map(parameterDescriptor -> parameterToString(parameterDescriptor, hilightParameter))
                        .collect(Collectors.joining(",", "(", ")"));
    }

    @Override
    public Method getElement() {
        return method;
    }
}
