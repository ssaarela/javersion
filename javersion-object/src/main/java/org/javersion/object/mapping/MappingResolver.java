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

import static java.lang.Integer.MAX_VALUE;

import java.util.Map;

import javax.annotation.Nonnull;

import org.javersion.reflect.ElementDescriptor;
import org.javersion.reflect.MethodDescriptor;
import org.javersion.reflect.ParameterDescriptor;
import org.javersion.reflect.StaticExecutable;
import org.javersion.reflect.TypeDescriptor;
import org.javersion.util.Check;

public interface MappingResolver {

    @Nonnull
    Result<MethodDescriptor> delegateValue(MethodDescriptor method);

    @Nonnull
    <T extends StaticExecutable & ElementDescriptor> Result<StaticExecutable> creator(T methodOrConstructor);

    @Nonnull
    Result<String> alias(TypeDescriptor type);

    @Nonnull
    Result<Map<TypeDescriptor,String>> subclasses(TypeDescriptor type);

    @Nonnull
    Result<String> name(ParameterDescriptor parameter);

    final class Result<T> {

        public static <T> Result<T> of(T value) {
            return new Result<>(Check.notNull(value, "value"), 0);
        }

        @SuppressWarnings("unchecked")
        public static <T> Result<T> notFound() {
            return (Result<T>) NOT_FOUND;
        }

        public final T value;
        private final int priority;

        private Result(T value, int priority) {
            this.value = value;
            this.priority = priority;
        }

        public boolean isPreset() {
            return value != null;
        }

        public boolean isAbsent() {
            return value == null;
        }

        Result<T> withPriority(int priority) {
            return new Result<>(value, priority);
        }

        @SuppressWarnings("unchecked")
        private static Result NOT_FOUND = new Result(null, MAX_VALUE);
    }

    static <T> Result<T> higherOf(Result<T> result1, Result<T> result2) {
        return higherOf(result1, result2, null);
    }

    static <T> Result<T> higherOf(Result<T> result1, Result<T> result2, String samePriorityErrorMessage) {
        if (result1.isAbsent()) {
            return result2;
        }
        if (result2.isAbsent()) {
            return result1;
        }
        if (samePriorityErrorMessage != null && result1.priority == result2.priority) {
            throw new IllegalArgumentException(samePriorityErrorMessage);
        }
        return result1.priority < result2.priority ? result1 : result2;
    }

}
