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
        this(new PersistentMap<K, V>());
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
    
    public MutableMap<K, V> toMutableMap() {
        return atomicMap.get().toMutableMap();
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
        return apply(new Function<MutableMap<K,V>, V>() {
            @Override
            public V apply(MutableMap<K, V> map) {
                return map.put(key, value);
            }
        });
    }

    @Override
    public V remove(final Object key) {
        return apply(new Function<MutableMap<K,V>, V>() {
            @Override
            public V apply(MutableMap<K, V> map) {
                return map.remove(key);
            }
        });
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        apply(new Function<MutableMap<K,V>, Void>() {
            @Override
            public Void apply(MutableMap<K, V> map) {
                map.putAll(m);
                return null;
            }
        });
    }

    @Override
    public void clear() {
        apply(new Function<MutableMap<K,V>, Void>() {
            @Override
            public Void apply(MutableMap<K, V> map) {
                map.clear();
                return null;
            }
        });
    }
    
    public <T> T apply(final Function<MutableMap<K, V>, T> f) {
        PersistentMap<K, V> currentValue;
        MutableMap<K, V> mutableMap;
        T result;
        do {
            currentValue = atomicMap.get();
            mutableMap = currentValue.toMutableMap();
            result = f.apply(mutableMap);
        } while (!atomicMap.compareAndSet(currentValue, mutableMap.persistentValue()));
        return result;
    }
}