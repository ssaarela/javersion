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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Function;

public class AtomicMap<K, V> extends AbstractMap<K, V> {
    
    private final AtomicReference<PersistentMap<K, V>> atomicMap;
    
    public AtomicMap() {
        this(PersistentMap.<K, V>empty());
    }
    
    public AtomicMap(PersistentMap<K, V> map) {
        this.atomicMap = new AtomicReference<>(Check.notNull(map, "map"));
    }
    
    public PersistentMap<K, V> getPersistentMap() {
        return atomicMap.get();
    }

    @Override
    public int size() {
        return atomicMap.get().size();
    }

    @Override
    public boolean containsKey(Object key) {
        return atomicMap.get().containsKey(key);
    }

    @Override
    public V get(Object key) {
        return atomicMap.get().get(key);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        final PersistentMap<K, V> map = atomicMap.get();
        return new AbstractSet<Map.Entry<K, V>>() {

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return map.iterator();
            }

            @Override
            public int size() {
                return map.size();
            }
        };
    }

    @Override
    public V put(final K key, final V value) {
        return apply(1, 
                new MapUpdate<K, V>() {
                    @Override
                    public void apply(MutableMap<K, V> input) {
                        input.merge(key, value, merger);
                    }
                });
    }

    @Override
    public V remove(final Object key) {
        return apply(1, 
                new MapUpdate<K, V>() {
                    @Override
                    public void apply(MutableMap<K, V> input) {
                        input.dissoc(key, merger);
                    }
                });
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        apply(m.size(), 
                new MapUpdate<K, V>() {
                    @Override
                    public void apply(MutableMap<K, V> map) {
                        map.assocAll(m);
                    }
                });
    }

    @Override
    public void clear() {
        atomicMap.getAndSet(PersistentMap.<K, V>empty());
    }

    
    public <T> T apply(int expectedUpdates, final Function<MutableMap<K, V>, T> f) {
        return apply(32, 
                new MapUpdate<K, V>() {
                    @Override
                    public void apply(MutableMap<K, V> map) {
                        result.set(f.apply(map));
                    }
                });
    }
    
    @SuppressWarnings("unchecked")
    private <T> T apply(int expectedUpdates, MapUpdate<K, V> updateFunction) {
        try {
            atomicMap.getAndSet(atomicMap.get().update(expectedUpdates, updateFunction));
            return (T) result.get();
        } finally {
            result.set(null);
        }
    }
    
    private final static ThreadLocal<Object> result = new ThreadLocal<Object>();

    private final Merger<K, V> merger = new  Merger<K, V>() {
        @Override
        public org.javersion.util.AbstractTrieMap.Entry<K, V> merge(
                org.javersion.util.AbstractTrieMap.Entry<K, V> oldEntry,
                org.javersion.util.AbstractTrieMap.Entry<K, V> newEntry) {
            if (oldEntry == null) {
                result.set(null);
            } else {
                result.set(oldEntry.getValue());
            }
            return newEntry;
        }
    };

}

