package org.javersion.object.mapping;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.MethodDescriptor;
import org.javersion.reflect.ParameterDescriptor;
import org.javersion.reflect.StaticExecutable;
import org.javersion.reflect.TypeDescriptor;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

public class CompositeMappingResolver implements MappingResolver {

    private final List<MappingResolver> resolvers;

    public CompositeMappingResolver(MappingResolver... resolvers) {
        this(ImmutableList.copyOf(resolvers));
    }

    public CompositeMappingResolver(List<MappingResolver> resolvers) {
        this.resolvers = ImmutableList.copyOf(resolvers);
    }

    @Nonnull
    @Override
    public Result<MethodDescriptor> delegateValue(MethodDescriptor method) {
        return find(resolver -> resolver.delegateValue(method));
    }

    @Nonnull
    @Override
    public <T extends StaticExecutable & ElementDescriptor> Result<StaticExecutable> creator(T methodOrConstructor) {
        return find(resolver -> resolver.creator(methodOrConstructor));
    }

    @Nonnull
    @Override
    public Result<String> alias(TypeDescriptor type) {
        return find(resolver -> resolver.alias(type));
    }

    @Nonnull
    @Override
    public Result<Map<TypeDescriptor, String>> subclasses(TypeDescriptor type) {
        return find(resolver -> resolver.subclasses(type));
    }

    @Nonnull
    @Override
    public Result<String> name(ParameterDescriptor parameter) {
        return find(resolver -> resolver.name(parameter));
    }

    <T, P> Result<T> find(Function<MappingResolver, Result<T>> function) {
        for (int i=0; i < resolvers.size(); i++) {
            Result<T> result = function.apply(resolvers.get(i));
            if (result != null && result.isPreset()) {
                return result.withPriority(i);
            }
        }
        return Result.notFound();
    }
}
