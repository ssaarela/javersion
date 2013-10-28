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


public class PersistentSet<E> implements Iterable<E> {
    
    public static class Builder<E> {
        
        private final PersistentMap.Builder<E, Object> builder;
        
        public Builder() {
            this(new PersistentSet<E>());
        }
        
        public Builder(PersistentSet<E> set) {
            this.builder = PersistentMap.builder(set.map);
        }
        
        public Builder<E> add(E e) {
            builder.put(e, PRESENT);
            return this;
        }
        
        public Builder<E> addAll(Collection<? extends E> elements) {
            for (E e : elements) {
                add(e);
            }
            return this;
        }
        
        public Builder<E> remove(Object e) {
            builder.remove(e);
            return this;
        }
        
        public Builder<E> addAll(PersistentSet<? extends E> elements) {
            for (E e : elements) {
                add(e);
            }
            return this;
        }
        
        public boolean contains(Object e) {
            return builder.containsKey(e);
        }
        
        public PersistentSet<E> build() {
            return new PersistentSet<>(builder.build());
        }
    }

    public static <E> Builder<E> builder() {
        return new Builder<>();
    }

    public static <E> Builder<E> builder(PersistentSet<E> parent) {
        return new Builder<>(parent);
    }
    
    private final PersistentMap<E, Object> map;
    
    private static final Object PRESENT = new Object(); 
    
    public PersistentSet() {
        this(new PersistentMap<E, Object>());
    }
    
    private PersistentSet(PersistentMap<E, Object> map) {
        this.map = map;
    }

    public PersistentSet<E> conj(E element) {
        return doReturn(map.assoc(element, PRESENT));
    }
    
    private PersistentSet<E> doReturn(PersistentMap<E, Object> newMap) {
        if (newMap == map) {
            return this;
        }
        return new PersistentSet<>(newMap);
    }

    public PersistentSet<E> conjAll(Collection<? extends E> elements) {
        PersistentMap.Builder<E, Object> builder = PersistentMap.builder(map, elements.size());
        for (E element : elements) {
            builder.put(element, PRESENT);
        }
        return doReturn(builder.build());
    }
    
    public PersistentSet<E> disjoin(Object element) {
        return doReturn(map.dissoc(element));
    }
    
    public int size() {
        return map.size();
    }
    
    public Iterator<E> iterator() {
        return map.atomicMap().keySet().iterator();
    }

    public boolean contains(Object o) {
        return map.containsKey(o);
    }
    
    public AtomicSet<E> asSet() {
        return new AtomicSet<E>(this);
    }
}
