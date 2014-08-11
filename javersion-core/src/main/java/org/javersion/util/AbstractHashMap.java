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

import static com.google.common.base.Objects.equal;
import static com.google.common.collect.Iterators.transform;

import java.util.Iterator;
import java.util.Map;

import org.javersion.util.AbstractHashMap.Entry;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public abstract class AbstractHashMap<K, V, This extends AbstractHashMap<K, V, This>> 
        extends AbstractHashTrie<K, Entry<K,V>, AbstractHashMap<K, V, This>>
        implements Iterable<Map.Entry<K, V>> {
    
    @SuppressWarnings("rawtypes")
    private static final Function TO_ENTRY = new Function() {
        @SuppressWarnings("unchecked")
        @Override
        public Object apply(Object input) {
            return toEntry((Map.Entry) input);
        }
    };
    
    public This assoc(K key, V value) {
        return assoc(new Entry<K, V>(key, value));
    }
    
    private This assoc(java.util.Map.Entry<? extends K, ? extends V> entry) {
        return merge(entry, null);
    }

    public This assocAll(Map<? extends K, ? extends V> map) {
        return mergeAll(map, null);
    }
    
    public This assocAll(Iterable<Map.Entry<K, V>> entries) {
        return mergeAll(entries, null);
    }

    
    public This merge(K key, V value, Merger<Map.Entry<K, V>> merger) {
        return doMerge(new Entry<K, V>(key, value), merger);
    }
    
    public This merge(Map.Entry<? extends K, ? extends V> entry, Merger<Map.Entry<K, V>> merger) {
        return doMerge(toEntry(entry), merger);
    }

    @SuppressWarnings("unchecked")
    protected This doMerge(Entry<K, V> entry, Merger<Map.Entry<K, V>> merger) {
        final UpdateContext<Map.Entry<K, V>> updateContext = updateContext(1, merger);
        return (This) doAdd(updateContext, toEntry(entry));
    }

    
    @SuppressWarnings("unchecked")
    public This mergeAll(Map<? extends K, ? extends V> map, Merger<Map.Entry<K, V>> merger) {
        final UpdateContext<Map.Entry<K, V>> updateContext = updateContext(map.size(), merger);
        return (This) doAddAll(updateContext, transform(map.entrySet().iterator(), TO_ENTRY));
    }

    @SuppressWarnings("unchecked")
    public This mergeAll(Iterable<Map.Entry<K, V>> entries, Merger<Map.Entry<K, V>> merger) {
        final UpdateContext<Map.Entry<K, V>> updateContext = updateContext(32, merger);
        return (This) doAddAll(updateContext, transform(entries.iterator(), TO_ENTRY));
    }
    
    protected UpdateContext<Map.Entry<K, V>> updateContext(int expectedSize, Merger<Map.Entry<K, V>> merger) {
        return new UpdateContext<>(expectedSize, merger);
    }

    
    public This dissoc(Object key) {
        return dissoc(key, null);
    }

    @SuppressWarnings("unchecked")
    public This dissoc(Object key, Merger<Map.Entry<K, V>> merger) {
        final UpdateContext<Map.Entry<K, V>> updateContext = updateContext(1, merger);
        return (This) doRemove(updateContext, key);
    }
    
    public V get(Object key) {
        Entry<K, V> entry = root().find(key);
        return entry != null ? entry.getValue() : null;
    }
    
    public boolean containsKey(Object key) {
        return root().find(key) != null;
    }

    public Iterator<Map.Entry<K, V>> iterator() {
        return transform(doIterator(), MapUtils.<K, V>mapEntryFunction());
    }

    public Iterable<K> keys() {
        return Iterables.transform(this, MapUtils.<K>mapKeyFunction());
    }

    public Iterable<V> values() {
        return Iterables.transform(this, MapUtils.<V>mapValueFunction());
    }
    
    
    @SuppressWarnings("unchecked")
    protected static <K, V> Entry<K, V> toEntry(Map.Entry<? extends K, ? extends V> entry) {
        if (entry instanceof Entry) {
            return (Entry<K, V>) entry;
        } else {
            return new Entry<K, V>(entry.getKey(), entry.getValue());
        }
    }
    
    public static final class Entry<K, V> extends AbstractHashTrie.Entry<K, Entry<K, V>> implements Map.Entry<K, V> {
        
        final V value;
        
        public Entry(K key, V value) {
            super(key);
            this.value = value;
        }
        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
     
        public String toString() {
            return "" + key + ": " + value;
        }
        
        @Override
        public Node<K, Entry<K, V>> assocInternal(final UpdateContext<? super Entry<K, V>>  currentContext, final int shift, final int hash, final Entry<K, V> newEntry) {
            if (equal(key, newEntry.key)) {
                return currentContext.merge(this, newEntry) ? newEntry : this;
            } else {
                return split(currentContext, shift, hash, newEntry);
            }
        }
    }
}