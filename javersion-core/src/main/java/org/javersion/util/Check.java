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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Map;

public class Check {

    private static final String NOT_NULL_OR_EMPTY_FMT = "%s shoud not be null or empty. Got %s";

    public static <T> T notNull(T reference, String fieldName) {
        return notNull$(reference, "%s should not be null", fieldName);
    }

    public static <T> T notNull$(T reference, String messageFormat, Object... args) {
        return checkNotNull(reference, messageFormat, args);
    }

    public static void that(boolean expression, String messageFormat, Object... args) {
        checkArgument(expression, messageFormat, args);
    }
    
    public static <T extends Iterable<?>> T notNullOrEmpty(T reference, String fieldName) {
        return notNullOrEmpty$(reference, NOT_NULL_OR_EMPTY_FMT, fieldName, reference);
    }
    
    public static <T extends Iterable<?>> T notNullOrEmpty$(T reference, String messageFormat, Object... args) {
        checkArgument(!(reference == null || !reference.iterator().hasNext()), messageFormat, args);
        return reference;
    }
    
    public static <T extends Collection<?>> T notNullOrEmpty(T reference, String fieldName) {
        return notNullOrEmpty$(reference, NOT_NULL_OR_EMPTY_FMT, fieldName, reference);
    }
    
    public static <T extends Collection<?>> T notNullOrEmpty$(T reference, String messageFormat, Object... args) {
        checkArgument(!(reference == null || reference.isEmpty()), messageFormat, args);
        return reference;
    }
    
    public static <K, V, T extends Map<K, V>> T notNullOrEmpty(T reference, String fieldName) {
        return notNullOrEmpty$(reference, NOT_NULL_OR_EMPTY_FMT, fieldName, reference);
    }
    
    public static <K, V, T extends Map<K, V>> T notNullOrEmpty$(T reference, String messageFormat, Object... args) {
        checkArgument(!(reference == null || reference.isEmpty()), messageFormat, args);
        return reference;
    }
    
    public static String notNullOrEmpty(String reference, String fieldName) {
        return notNullOrEmpty$(reference, NOT_NULL_OR_EMPTY_FMT, fieldName, reference);
    }
    
    public static String notNullOrEmpty$(String reference, String messageFormat, Object... args) {
        checkArgument(!(reference == null || reference.length() == 0), messageFormat, args);
        return reference;
    }
    
}
