package org.javersion.reflect;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.javersion.util.Check;

import com.google.common.collect.ImmutableList;

public final class MethodSignature {

    @Nonnull
    public final String name;

    @Nonnull
    public final List<Class<?>> parameterTypes;

    public MethodSignature(Method method) {
        this(method.getName(), method.getParameterTypes());
    }

    public MethodSignature(String name, Class<?>... parameterTypes) {
        this(name, ImmutableList.copyOf(parameterTypes));
    }

    public MethodSignature(String name, List<Class<?>> parameterTypes) {
        this.name = Check.notNullOrEmpty(name, "name");
        Check.notNull(parameterTypes, "parameterTypes");
        this.parameterTypes = ImmutableList.copyOf(parameterTypes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodSignature)) return false;

        MethodSignature that = (MethodSignature) o;

        if (!name.equals(that.name)) return false;
        return parameterTypes.equals(that.parameterTypes);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + parameterTypes.hashCode();
        return result;
    }

    public String toString() {
        return name + parameterTypes.stream()
                .map(TypeDescriptor::getSimpleName)
                .collect(Collectors.joining(",", "(", ")"));
    }

}
