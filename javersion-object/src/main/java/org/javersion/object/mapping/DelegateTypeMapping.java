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

import static org.javersion.object.TypeMappings.USE_JACKSON_ANNOTATIONS;

import java.util.List;

import javax.annotation.Nullable;

import org.javersion.object.DescribeContext;
import org.javersion.object.TypeContext;
import org.javersion.object.VersionCreator;
import org.javersion.object.VersionValue;
import org.javersion.object.types.DelegateType;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;

public class DelegateTypeMapping implements TypeMapping {

    @Override
    public boolean applies(@Nullable PropertyPath path, TypeContext typeContext) {
        return typeContext.type.getMethods().values().stream().anyMatch(method -> getValuePrecedence(method) > 0);
    }

    @Override
    public ValueType describe(@Nullable PropertyPath path, TypeContext typeContext, DescribeContext context) {
        TypeDescriptor type = typeContext.type;
        MethodDescriptor valueMethod = getValueMethod(type);
        Class<?> rawDelegateType = valueMethod.getRawReturnType();
        StaticExecutable creator = getCreator(type, rawDelegateType);
        ValueType valueType = context.describeNow(path, new TypeContext(valueMethod));
        return DelegateType.of(valueMethod, creator, valueType);
    }

    private MethodDescriptor getValueMethod(TypeDescriptor type) {
        MethodDescriptor valueMethod = null;
        int valuePrecedence = 0;
        for (MethodDescriptor method : type.getMethods().values()) {
            int precedence = getValuePrecedence(method);
            if (precedence > valuePrecedence) {
                valueMethod = method;
                valuePrecedence = precedence;
            }
            else if (precedence > 0 && precedence == valuePrecedence) {
                throw new IllegalArgumentException("Found two @VersionValue/@JsonValue methods from " + type);
            }
        }
        validateValueMethod(valueMethod);
        return valueMethod;
    }

    private StaticExecutable getCreator(TypeDescriptor type, Class<?> delegateType) {
        final List<Class<?>> expectedParameters = ImmutableList.of(delegateType);
        StaticExecutable creator = null;
        int creatorPrecedence = 0;
        for (MethodDescriptor method : type.getMethods().values()) {
            int precedence = getCreatorPrecedence(method);
            if (precedence > creatorPrecedence) {
                creator = method;
                creatorPrecedence = precedence;
            } else if (precedence > 0 && precedence == creatorPrecedence) {
                throw new IllegalArgumentException("Found two @VersionCreator/@JsonCreator methods from " + type);
            }
        }
        ConstructorDescriptor constructor = getConstructor(type, delegateType);
        if (constructor != null) {
            int precedence = getCreatorPrecedence(constructor);
            if (precedence > creatorPrecedence || creator == null) {
                creator = constructor;
            } else if (precedence == creatorPrecedence) {
                throw new IllegalArgumentException("Found two @VersionCreator/@JsonCreator methods from " + type);
            }
        }
        validateCreator(creator, type, expectedParameters);

        return creator;
    }

    private void validateCreator(StaticExecutable creator, TypeDescriptor type, List<Class<?>> expectedParameters) {
        if (creator == null) {
            throw new IllegalArgumentException("Could not find mathing constructor or creator method for " + type);
        }
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
        StaticExecutable creator;ConstructorSignature signature = new ConstructorSignature(delegateType);
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

    private static int getValuePrecedence(MethodDescriptor method) {
        if (method.hasAnnotation(VersionValue.class)) {
            return 10;
        }
        if (USE_JACKSON_ANNOTATIONS && method.hasAnnotation(JsonValue.class)) {
            return 5;
        }
        return 0;
    }

    private static int getCreatorPrecedence(ElementDescriptor creator) {
        if (creator.hasAnnotation(VersionCreator.class)) {
            return 10;
        }
        if (USE_JACKSON_ANNOTATIONS && creator.hasAnnotation(JsonCreator.class)) {
            return 5;
        }
        return 0;
    }
}
