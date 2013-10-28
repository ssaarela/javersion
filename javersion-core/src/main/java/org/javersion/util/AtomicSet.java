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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

import org.javersion.util.CompareAndSet.AtomicFunction;
import org.javersion.util.CompareAndSet.AtomicVoidFunction;
import org.javersion.util.CompareAndSet.Result;

public class AtomicSet<E> extends AbstractSet<E> {

    private CompareAndSet<PersistentSet<E>> atomicSet;
    
    public AtomicSet() {
        this(new PersistentSet<E>());
    }
    
    public AtomicSet(PersistentSet<E> set) {
        atomicSet = new CompareAndSet<PersistentSet<E>>(set);
    }
    
    public PersistentSet<E> getPersistentSet() {
        return atomicSet.get();
    }

    @Override
    public Iterator<E> iterator() {
        return atomicSet.get().iterator();
    }

    @Override
    public int size() {
        return atomicSet.get().size();
    }

    @Override
    public boolean contains(Object o) {
        return atomicSet.get().contains(o);
    }

    @Override
    public boolean add(final E e) {
        return atomicSet.invoke(new AtomicFunction<PersistentSet<E>, Boolean>() {
            @Override
            public PersistentSet<E> invoke(PersistentSet<E> set, Result<Boolean> result) {
                PersistentSet<E> newSet = set.conj(e);
                result.set(set != newSet);
                return newSet;
            }
        });
    }

    @Override
    public boolean remove(final Object o) {
        return atomicSet.invoke(new AtomicFunction<PersistentSet<E>, Boolean>() {
            @Override
            public PersistentSet<E> invoke(PersistentSet<E> set, Result<Boolean> result) {
                PersistentSet<E> newSet = set.disjoin(o);
                result.set(set != newSet);
                return newSet;
            }
        });
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return atomicSet.invoke(new AtomicFunction<PersistentSet<E>, Boolean>() {
            @Override
            public PersistentSet<E> invoke(PersistentSet<E> set, Result<Boolean> result) {
                PersistentSet<E> newSet = set.conjAll(c);
                result.set(set != newSet);
                return newSet;
            }
        });
    }

    @Override
    public void clear() {
        atomicSet.invoke(new AtomicVoidFunction<PersistentSet<E>>() {
            @Override
            public PersistentSet<E> invoke(PersistentSet<E> set) {
                PersistentSet.Builder<E> builder = PersistentSet.builder(set);
                for (E e : set) {
                    builder.remove(e);
                }
                return builder.build();
            }
        });
    }
    
}