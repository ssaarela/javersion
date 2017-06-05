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
package org.javersion.reflect;

import java.lang.reflect.*;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import org.javersion.util.Check;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

import static java.util.Collections.synchronizedMap;

public final class TypeDescriptors {

    public static final Predicate<Member> NON_SYNTHETIC = member -> !member.isSynthetic();

    public static final TypeDescriptors DEFAULT = new TypeDescriptors();

    public static TypeDescriptor getTypeDescriptor(Class<?> clazz) {
        Check.notNull(clazz, "clazz");
        return DEFAULT.get(clazz);
    }

    private final Cache<TypeToken<?>, TypeDescriptor> cache = CacheBuilder.newBuilder()
            .softValues()
            .build();

    protected final Predicate<? super Field> fieldFilter;

    protected final Predicate<? super Method> methodFilter;

    protected final Predicate<? super Constructor> constructorFilter;

    protected final Paranamer paranamer = new CachingParanamer(new BytecodeReadingParanamer());


    public TypeDescriptors() {
        this(NON_SYNTHETIC);
    }

    public TypeDescriptors(Predicate<? super Member> memberFilter) {
        this(memberFilter, memberFilter, memberFilter);
    }

    public TypeDescriptors(Predicate<? super Field> fieldFilter,
                           Predicate<? super Method> methodFilter,
                           Predicate<? super Constructor> constructorFilter) {
        this.fieldFilter = Check.notNull(fieldFilter, "fieldFilter");
        this.methodFilter = Check.notNull(methodFilter, "methodFilter");
        this.constructorFilter = Check.notNull(constructorFilter, "constructorFilter");
    }


    public TypeDescriptor get(Class<?> clazz) {
        return get(TypeToken.of(clazz));
    }

    public TypeDescriptor get(Type type) {
        return get(TypeToken.of(type));
    }

    public TypeDescriptor get(TypeToken<?> typeToken) {
        try {
            return cache.get(typeToken, () -> new TypeDescriptor(this, typeToken));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    Paranamer getParanamer() {
        return paranamer;
    }

}