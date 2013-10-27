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
import java.util.Set;

public class PersistentSet<E> implements Iterable<E> {
    
    public static class Builder<E> {
        
        private final PersistentMap.Builder<E, Object> builder;
        
        private final int originalSize;
        
        private boolean modified;
        
        public Builder() {
            this(new PersistentSet<E>());
        }
        
        public Builder(PersistentSet<E> set) {
            this.builder = PersistentMap.builder(set.map);
            this.originalSize = set.size();
        }
        
        public Builder<E> add(E e) {
            builder.put(e, PRESENT);
            modified = modified || builder.size() != originalSize;
            return this;
        }
        
        public Builder<E> addAll(Collection<? extends E> elements) {
            for (E e : elements) {
                add(e);
            }
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
        return new PersistentSet<>(map.assoc(element, PRESENT));
    }

    public PersistentSet<E> conjAll(Collection<E> elements) {
        PersistentMap.Builder<E, Object> builder = PersistentMap.builder(map, elements.size());
        for (E element : elements) {
            builder.put(element, PRESENT);
        }
        return new PersistentSet<>(builder.build());
    }
    
    public int size() {
        return map.size();
    }
    
    public Iterator<E> iterator() {
        return map.asMap().keySet().iterator();
    }

    public boolean contains(Object o) {
        return map.containsKey(o);
    }
    
    public Set<E> asSet() {
        return new AsSet<E>(this);
    }

    
    
    private static class AsSet<E> extends AbstractSet<E> {

        private PersistentSet<E> set;
        
        public AsSet(PersistentSet<E> set) {
            this.set = set;
        }

        @Override
        public Iterator<E> iterator() {
            return set.iterator();
        }

        @Override
        public int size() {
            return set.size();
        }

        @Override
        public boolean contains(Object o) {
            return set.contains(o);
        }

        @Override
        public boolean add(E e) {
            if (set.contains(e)) {
                return false;
            }
            set = set.conj(e);
            return true;
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            Builder<E> builder = new Builder<>(set);
            set = builder.addAll(c).build();
            return builder.modified;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
        
    }
}
