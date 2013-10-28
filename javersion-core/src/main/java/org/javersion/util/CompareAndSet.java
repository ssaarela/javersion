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

import java.util.concurrent.atomic.AtomicReference;

public class CompareAndSet<V> {
    
    public static interface Result<V> {
        void set(V result);
    }
    
    public static interface AtomicVoidFunction<V> {
        V invoke(V currentValue);
    }
    
    public static interface AtomicFunction<V, T> {
        V invoke(V currentValue, Result<T> result);
    }
    
    public CompareAndSet(V initialValue) {
        this.ref = new AtomicReference<V>(initialValue);
    }
    
    public V get() {
        return ref.get();
    }
    
    public <T> T invoke(AtomicFunction<V, T> f) {
        ThreadLocalResult<T> result = new ThreadLocalResult<>();
        try {
            V currentValue;
            V newValue;
            do {
                result.set(null);
                currentValue = ref.get();
                newValue = f.invoke(currentValue, result);
            } while (!ref.compareAndSet(currentValue, newValue));
            return result.get();
        } finally {
            result.set(null);
        }
    }
    
    public void invoke(AtomicVoidFunction<V> f) {
        V currentValue;
        V newValue;
        do {
            currentValue = ref.get();
            newValue = f.invoke(currentValue);
        } while (!ref.compareAndSet(currentValue, newValue));
    }

    private final AtomicReference<V> ref;

    private static final class ThreadLocalResult<T> implements Result<T> {
        
        private static final ThreadLocal<Object> returnValue = new ThreadLocal<>();
        
        public void set(T result) {
            returnValue.set(result);
        }

        @SuppressWarnings("unchecked")
        T get() {
            return (T) returnValue.get();
        }
        
    }
}
