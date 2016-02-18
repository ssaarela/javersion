/*
 * Copyright 2013 Samppa Saarela
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
package org.javersion.util;

import static java.lang.String.format;
import static java.lang.System.arraycopy;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

public final class Check {

    public static final String NOT_NULL_FMT = "%s should not be null";

    public static final String NOT_EMPTY_FMT = "%s should not be empty";

    public static final Predicate<Boolean> IS_TRUE = v -> v;

    public static final Predicate<Object> NOT_NULL = v -> v != null;

    public static final Predicate<String> NOT_EMPTY_STRING = s -> !s.isEmpty();

    public static final Predicate<Iterable<?>> NOT_EMPTY_ITERABLE = i -> i.iterator().hasNext();

    public static final Predicate<Collection<?>> NOT_EMPTY_COLLECTION = c -> !c.isEmpty();

    public static final Predicate<Map<?, ?>> NOT_EMPTY_MAP = m -> !m.isEmpty();

    private Check() {}

    @Nonnull
    public static <T> T notNull(T object, String fieldName) {
        return that(object, NOT_NULL, NOT_NULL_FMT, fieldName);
    }

    @Nonnull
    public static String notNullOrEmpty(String string, String fieldName) {
        notNull(string, fieldName);
        return that(string, NOT_EMPTY_STRING, NOT_EMPTY_FMT, fieldName);
    }

    @Nonnull
    public static <T, I extends Iterable<T>> I notNullOrEmpty(I iterable, String fieldName) {
        notNull(iterable, fieldName);
        return that(iterable, NOT_EMPTY_ITERABLE, NOT_EMPTY_FMT, fieldName);
    }

    @Nonnull
    public static <T, C extends Collection<T>> C notNullOrEmpty(C collection, String fieldName) {
        notNull(collection, fieldName);
        return that(collection, NOT_EMPTY_COLLECTION, NOT_EMPTY_FMT, fieldName);
    }

    @Nonnull
    public static <K, V, M extends Map<K, V>> M notNullOrEmpty(M map, String fieldName) {
        notNull(map, fieldName);
        return that(map, NOT_EMPTY_MAP, NOT_EMPTY_FMT, fieldName);
    }

    public static void that(boolean expression, String message) {
        that(expression, IS_TRUE, message);
    }

    public static void that(boolean expression, String messageFormat, Object arg1) {
        that(expression, IS_TRUE, messageFormat, arg1);
    }

    public static void that(boolean expression, String messageFormat, Object arg1, Object arg2) {
        that(expression, IS_TRUE, messageFormat, arg1, arg2);
    }

    public static void that(boolean expression, String messageFormat, Object arg1, Object arg2, Object arg3, Object... rest) {
        that(expression, IS_TRUE, messageFormat, arg1, arg2, arg3, rest);
    }

    public static <T> T that(T value, Predicate<? super T> predicate, String message) {
        if (!predicate.test(value)) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static <T> T that(T value, Predicate<? super T> predicate, String messageFormat, Object arg1) {
        if (!predicate.test(value)) {
            throw new IllegalArgumentException(format(messageFormat, arg1));
        }
        return value;
    }

    public static <T> T that(T value, Predicate<? super T> predicate, String messageFormat, Object arg1, Object arg2) {
        if (!predicate.test(value)) {
            throw new IllegalArgumentException(format(messageFormat, arg1, arg2));
        }
        return value;
    }

    public static <T> T that(T value, Predicate<? super T> predicate, String messageFormat, Object arg1, Object arg2, Object arg3, Object... rest) {
        if (!predicate.test(value)) {
            Object[] args = new Object[3 + rest.length];
            args[0] = arg1;
            args[1] = arg2;
            args[2] = arg3;
            arraycopy(rest, 0, args, 3, rest.length);
            throw new IllegalArgumentException(format(messageFormat, args));
        }
        return value;
    }

}
