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

import org.javersion.util.PersistentMap.Version;

public class MutableMap<K, V> extends AbstractMap<K, V> {
    
    private Version version;
    
    private PersistentMap<K, V> map;
    
    public MutableMap() {
        this(new PersistentMap<K, V>(), 32);
    }
    
    public MutableMap(PersistentMap<K, V> map) {
        this(map, 32);
    }
    public MutableMap(PersistentMap<K, V> map, int expectedSize) {
        this.map = Check.notNull(map, "map");
        this.version = new Version(expectedSize);
    }
    
    public PersistentMap<K, V> persistentValue() {
        return map;
    }
    
    public AtomicMap<K, V> toAtomicMap() {
        return new AtomicMap<>(map);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
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
        V result = get(key);
        put(new PersistentMap.Entry<K, V>(key, value));
        return result;
    }

    public void put(Map.Entry<? extends K, ? extends V> entry) {
        map = map.assoc(version, entry);
    }
    
    @Override
    public V remove(final Object key) {
        V result = get(key);
        map = map.dissoc(version, key);
        return result;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry);
        }
    }

    @Override
    public void clear() {
        for (Map.Entry<K, V> entry : map) {
            remove(entry.getKey());
        }
    }

}