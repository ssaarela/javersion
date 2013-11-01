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
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Function;

public class AtomicSet<E> extends AbstractSet<E> {

    private AtomicReference<PersistentSet<E>> atomicSet;
    
    public AtomicSet() {
        this(new PersistentSet<E>());
    }
    
    public AtomicSet(PersistentSet<E> set) {
        atomicSet = new AtomicReference<PersistentSet<E>>(set);
    }
    
    public PersistentSet<E> toPersistentSet() {
        return atomicSet.get();
    }
    
    public MutableSet<E> toMutableSet() {
        return new MutableSet<>(atomicSet.get());
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
        return apply(new Function<MutableSet<E>, Boolean>() {

            @Override
            public Boolean apply(MutableSet<E> input) {
                return input.add(e);
            }
            
        });
    }

    @Override
    public boolean remove(final Object o) {
        return apply(new Function<MutableSet<E>, Boolean>() {

            @Override
            public Boolean apply(MutableSet<E> input) {
                return input.remove(o);
            }
            
        });
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return apply(new Function<MutableSet<E>, Boolean>() {

            @Override
            public Boolean apply(MutableSet<E> input) {
                return input.addAll(c);
            }
            
        });
    }

    @Override
    public void clear() {
        apply(new Function<MutableSet<E>, Void>() {

            @Override
            public Void apply(MutableSet<E> input) {
                input.clear();
                return null;
            }
            
        });
    }
    
    public <T> T apply(final Function<MutableSet<E>, T> f) {
        PersistentSet<E> currentValue;
        MutableSet<E> mutableSet;
        T result;
        do {
            currentValue = atomicSet.get();
            mutableSet = currentValue.toMutableSet();
            result = f.apply(mutableSet);
        } while (!atomicSet.compareAndSet(currentValue, mutableSet.persistentValue()));
        return result;
    }
    
}