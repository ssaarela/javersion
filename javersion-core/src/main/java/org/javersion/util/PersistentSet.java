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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.javersion.util.PersistentMap.Version;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;


public class PersistentSet<E> implements Iterable<E> {
    
    private final PersistentMap<E, Object> map;
    
    private static final Object PRESENT = new Object(); 
    
    public PersistentSet() {
        this(new PersistentMap<E, Object>());
    }
    
    private PersistentSet(PersistentMap<E, Object> map) {
        this.map = map;
    }

    public MutableSet<E> toMutableSet() {
        return new MutableSet<>(this);
    }
    
    public AtomicSet<E> toAtomicSet() {
        return new AtomicSet<>(this);
    }
    
    public PersistentSet<E> conj(E element) {
        return doReturn(map.assoc(element, PRESENT));
    }

    PersistentSet<E> conj(Version version, E element) {
        return doReturn(map.assoc(version, new PersistentMap.Entry<E, Object>(element, PRESENT)));
    }
    
    private PersistentSet<E> doReturn(PersistentMap<E, Object> newMap) {
        if (newMap == map) {
            return this;
        }
        return new PersistentSet<>(newMap);
    }

    public PersistentSet<E> conjAll(Collection<? extends E> elements) {
        Version version = new Version(elements.size());
        PersistentSet<E> result = this;
        for (E e : elements) {
            result = result.conj(version, e);
        }
        return result;
    }
    
    public PersistentSet<E> disjoin(Object element) {
        return doReturn(map.dissoc(element));
    }
    
    public int size() {
        return map.size();
    }
    
    public Iterator<E> iterator() {
        return Iterators.transform(map.iterator(), new Function<Map.Entry<E, Object>, E>() {

            @Override
            public E apply(java.util.Map.Entry<E, Object> input) {
                return input.getKey();
            }
            
        });
    }

    public boolean contains(Object o) {
        return map.containsKey(o);
    }
    
    public AtomicSet<E> asSet() {
        return new AtomicSet<E>(this);
    }
}
