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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.javersion.util.Check;

import com.google.common.base.Predicate;
import com.google.common.reflect.TypeToken;

public abstract class AbstractTypeDescriptors<
            F extends AbstractFieldDescriptor<F, T, Ts>, 
            T extends AbstractTypeDescriptor<F, T, Ts>,
            Ts extends AbstractTypeDescriptors<F, T, Ts>> {

    public static final Predicate<Field> NON_STATIC_OR_SYNTETHIC_FIELD = new Predicate<Field>() {
        @Override
        public boolean apply(Field field) {
            int mod = field.getModifiers();
            return !(Modifier.isStatic(mod) || field.isSynthetic());
        }
    };
    
    
    private final ConcurrentMap<TypeToken<?>, T> cache = new ConcurrentHashMap<>();

    protected final Predicate<? super Field> fieldFilter;
    
    
    public AbstractTypeDescriptors() {
        this(NON_STATIC_OR_SYNTETHIC_FIELD);
    }
    
    public AbstractTypeDescriptors(Predicate<? super Field> fieldFilter) {
        this.fieldFilter = Check.notNull(fieldFilter, "fieldFilter");
    }
    
    
    public T get(Class<?> clazz) {
        return get(TypeToken.of(clazz));
    }

    public T get(Type type) {
        return get(TypeToken.of(type));
    }
    
    public T get(TypeToken<?> typeToken) {
        T descriptor = cache.get(typeToken);
        if (descriptor == null) {
            descriptor = newTypeDescriptor(typeToken);
            cache.putIfAbsent(typeToken, descriptor);
        }
        return descriptor;
    }

    protected abstract F newFieldDescriptor(Field field);
    
    protected abstract T newTypeDescriptor(TypeToken<?> typeToken);

}
