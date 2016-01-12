package org.javersion.object.mapping;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.javersion.object.VersionCreator;
import org.javersion.object.VersionValue;
import org.javersion.object.Versionable;
import org.javersion.object.Versionable.Subclass;
import org.javersion.reflect.*;

public class JaversionMappingResolver implements MappingResolver {

    @Nullable
    @Override
    public Result<MethodDescriptor> delegateValue(MethodDescriptor method) {
        if (method.hasAnnotation(VersionValue.class)) {
            return Result.of(method);
        }
        return Result.notFound();
    }

    @Nullable
    @Override
    public <T extends StaticExecutable & ElementDescriptor> Result<StaticExecutable> creator(T methodOrConstructor) {
        if (methodOrConstructor.hasAnnotation(VersionCreator.class)) {
            return Result.of(methodOrConstructor);
        }
        return Result.notFound();
    }

    @Nullable
    @Override
    public Result<String> alias(TypeDescriptor type) {
        Versionable versionable = type.getAnnotation(Versionable.class);
        if (versionable != null && !isNullOrEmpty(versionable.alias())) {
            return Result.of(versionable.alias());
        }
        return Result.notFound();
    }

    @Nullable
    @Override
    public Result<Map<TypeDescriptor, String>> subclasses(TypeDescriptor type) {
        Versionable versionable = type.getAnnotation(Versionable.class);
        if (versionable != null && versionable.subclasses().length > 0) {
            TypeDescriptors typeDescriptors = type.getTypeDescriptors();
            Map<TypeDescriptor, String> aliasesByTypes = asList(versionable.subclasses()).stream()
                    .collect(toMap(subclass -> typeDescriptors.get(subclass.value()), Subclass::alias));
            return Result.of(aliasesByTypes);
        }
        return Result.notFound();
    }

    @Nonnull
    @Override
    public Result<String> name(ParameterDescriptor parameter) {
        Param param = parameter.getAnnotation(Param.class);
        if (param != null) {
            return Result.of(param.value());
        }
        return Result.notFound();
    }
}
