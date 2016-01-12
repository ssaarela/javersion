package org.javersion.object.mapping;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;

import java.util.Map;

import javax.annotation.Nonnull;

import org.javersion.reflect.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;

public class JacksonMappingResolver implements MappingResolver {

    @Nonnull
    @Override
    public Result<MethodDescriptor> delegateValue(MethodDescriptor method) {
        JsonValue jsonValue = method.getAnnotation(JsonValue.class);
        if (jsonValue != null && jsonValue.value()) {
            return Result.of(method);
        }
        return Result.notFound();
    }

    @Nonnull
    @Override
    public <T extends StaticExecutable & ElementDescriptor> Result<StaticExecutable> creator(T methodOrConstructor) {
        if (methodOrConstructor.hasAnnotation(JsonCreator.class)) {
            return Result.of(methodOrConstructor);
        }
        return Result.notFound();
    }

    @Nonnull
    @Override
    public Result<String> alias(TypeDescriptor type) {
        JsonTypeName typeName = type.getAnnotation(JsonTypeName.class);
        if (typeName != null && !isNullOrEmpty(typeName.value())) {
            return Result.of(typeName.value());
        }
        return Result.notFound();
    }

    @Nonnull
    @Override
    public Result<Map<TypeDescriptor, String>> subclasses(TypeDescriptor type) {
        JsonSubTypes jsonSubType = type.getAnnotation(JsonSubTypes.class);
        if (jsonSubType != null && jsonSubType.value().length > 0) {
            TypeDescriptors typeDescriptors = type.getTypeDescriptors();
            Map<TypeDescriptor, String> aliasesByTypes = asList(jsonSubType.value()).stream()
                    .collect(toMap(subType -> typeDescriptors.get(subType.value()), Type::name));
            return Result.of(aliasesByTypes);
        }
        return Result.notFound();
    }

    @Nonnull
    @Override
    public Result<String> name(ParameterDescriptor parameter) {
        JsonProperty jsonProperty = parameter.getAnnotation(JsonProperty.class);
        if (jsonProperty != null && !isNullOrEmpty(jsonProperty.value())) {
            return Result.of(jsonProperty.value());
        }
        return Result.notFound();
    }
}
