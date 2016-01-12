package org.javersion.object.mapping;

import java.util.Map;

import javax.annotation.Nonnull;

import org.javersion.reflect.*;

public class DefaultMappingResolver implements MappingResolver {

    @Nonnull
    @Override
    public Result<MethodDescriptor> delegateValue(MethodDescriptor method) {
        return Result.notFound();
    }

    @Nonnull
    @Override
    public <T extends StaticExecutable & ElementDescriptor> Result<StaticExecutable> creator(T methodOrConstructor) {
        if (methodOrConstructor instanceof ConstructorDescriptor) {
            ConstructorDescriptor constructor = (ConstructorDescriptor) methodOrConstructor;
            if (constructor.getParameters().isEmpty()) {
                return Result.of(constructor);
            }
        }
        return Result.notFound();
    }

    @Nonnull
    @Override
    public Result<String> alias(TypeDescriptor type) {
        return Result.of(type.getSimpleName());
    }

    @Nonnull
    @Override
    public Result<Map<TypeDescriptor, String>> subclasses(TypeDescriptor type) {
        return Result.notFound();
    }

    @Nonnull
    @Override
    public Result<String> name(ParameterDescriptor parameter) {
        String name = parameter.getName();
        if (name != null) {
            return Result.of(name);
        }
        return Result.notFound();
    }

}
