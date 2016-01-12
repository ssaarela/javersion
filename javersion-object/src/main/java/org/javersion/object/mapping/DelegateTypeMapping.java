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
package org.javersion.object.mapping;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.javersion.object.DescribeContext;
import org.javersion.object.TypeContext;
import org.javersion.object.mapping.MappingResolver.Result;
import org.javersion.object.types.DelegateType;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.ConstructorDescriptor;
import org.javersion.reflect.ConstructorSignature;
import org.javersion.reflect.MethodDescriptor;
import org.javersion.reflect.StaticExecutable;
import org.javersion.reflect.TypeDescriptor;

import com.google.common.collect.ImmutableList;

public class DelegateTypeMapping implements TypeMapping {

    @Override
    public Optional<ValueType> describe(@Nullable PropertyPath path, TypeContext typeContext, DescribeContext context) {
        TypeDescriptor type = typeContext.type;
        MappingResolver mappingResolver = context.getMappingResolver();
        MethodDescriptor valueMethod = getValueMethod(type, mappingResolver);
        if (valueMethod != null) {
            Class<?> rawDelegateType = valueMethod.getRawReturnType();
            StaticExecutable creator = getCreator(type, rawDelegateType, mappingResolver);
            ValueType valueType = context.describeNow(path, new TypeContext(valueMethod));
            return Optional.of(DelegateType.of(valueMethod, creator, valueType));
        }
        return Optional.empty();
    }

    private MethodDescriptor getValueMethod(TypeDescriptor type, MappingResolver mappingResolver) {
        final String samePriorityError = "Found two @VersionValue/@JsonValue methods from " + type;
        Result<MethodDescriptor> valueMethod = Result.notFound();

        for (MethodDescriptor method : type.getMethods().values()) {
            valueMethod = MappingResolver.higherOf(valueMethod, mappingResolver.delegateValue(method), samePriorityError);
        }
        if (valueMethod.isPreset()) {
            validateValueMethod(valueMethod.value);
            return valueMethod.value;
        }
        return null;
    }

    private StaticExecutable getCreator(TypeDescriptor type, Class<?> delegateType, MappingResolver mappingResolver) {
        final String samePriorityError = "Found two @VersionCreator/@JsonCreator methods from " + type;
        Result<StaticExecutable> creator = Result.notFound();

        for (MethodDescriptor method : type.getMethods().values()) {
            creator = MappingResolver.higherOf(creator, mappingResolver.creator(method), samePriorityError);
        }
        ConstructorDescriptor constructor = getConstructor(type, delegateType);
        if (constructor != null) {
            if (creator.isPreset()) {
                creator = MappingResolver.higherOf(creator, mappingResolver.creator(constructor), samePriorityError);
            } else {
                creator = Result.of(constructor);
            }
        }
        if (creator.isAbsent()) {
            throw new IllegalArgumentException("Could not find mathing constructor or creator method for " + type);
        }
        validateCreator(creator.value, type, ImmutableList.of(delegateType));

        return creator.value;
    }

    private void validateCreator(StaticExecutable creator, TypeDescriptor type, List<Class<?>> expectedParameters) {
        if (creator instanceof MethodDescriptor) {
            MethodDescriptor method = (MethodDescriptor) creator;
            if (!method.getParameterTypes().equals(expectedParameters)) {
                throw new IllegalArgumentException("@VersionCreator/@JsonCreator " + method +
                        " parameters should match " + expectedParameters);
            }
            if (!type.equalTo(method.getReturnType())) {
                throw new IllegalArgumentException("@VersionCreator/@JsonCreator " + method +
                        " return type should be " + type);
            }
            if (!method.isStatic()) {
                throw new IllegalArgumentException("@VersionCreator/@JsonCreator method should be static in " + type);
            }
        }
    }

    private ConstructorDescriptor getConstructor(TypeDescriptor type, Class<?> delegateType) {
        ConstructorSignature signature = new ConstructorSignature(delegateType);
        return type.getConstructors().get(signature);
    }

    private static void validateValueMethod(MethodDescriptor method) {
        if (method.isStatic()) {
            throw new IllegalArgumentException("@VersionValue/@JsonValue must not be static");
        }
        if (method.isAbstract()) {
            throw new IllegalArgumentException("@VersionValue/@JsonValue must not be abstract");
        }
        if (method.getParameterCount() > 0) {
            throw new IllegalArgumentException("@VersionValue/@JsonValue cannot have parameters");
        }
        if (method.getRawReturnType() == void.class) {
            throw new IllegalArgumentException("@VersionValue/@JsonValue cannot be void");
        }
    }
}
