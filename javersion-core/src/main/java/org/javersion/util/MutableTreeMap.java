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

import java.util.*;

import org.javersion.util.AbstractTreeMap.Node;

public class MutableTreeMap<K, V> extends AbstractMap<K, V> implements MutableSortedMap<K, V> {
    
    private MMap<K, V> map;
    
    private V previousValue;
    
    private final Merger<Entry<K, V>> defaultMerger = new Merger<Map.Entry<K,V>>() {

        @Override
        public void insert(java.util.Map.Entry<K, V> newEntry) {
            previousValue = null;
        }

        @Override
        public boolean merge(java.util.Map.Entry<K, V> oldEntry, java.util.Map.Entry<K, V> newEntry) {
            previousValue = oldEntry.getValue();
            return true;
        }

        @Override
        public void delete(java.util.Map.Entry<K, V> oldEntry) {
            previousValue = oldEntry.getValue();
        }
    };
    
    public MutableTreeMap() {
        this.map = new MMap<K, V>();
    }

    public MutableTreeMap(Comparator<? super K> comparator) {
        this.map = new MMap<K, V>(comparator);
    }
    
    MutableTreeMap(Comparator<? super K> comparator, Node<K, V> root, int size) {
        this.map = new MMap<K, V>(comparator, root, size);
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

    Node<K, V> root() {
        // For tests
        return map.root;
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
        map.merge(key, value, defaultMerger);
        return previousValue;
    }

    @Override
    public V remove(final Object key) {
        map.dissoc(key, defaultMerger);
        return previousValue;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        map.assocAll(m);
    }

    @Override
    public void clear() {
        map = new MMap<K, V>(map.comparator);
    }


    @Override
    public Iterator<java.util.Map.Entry<K, V>> iterator() {
        return map.iterator();
    }


    @Override
    public void merge(K key, V value, Merger<java.util.Map.Entry<K, V>> merger) {
        map.merge(key, value, merger);
    }

    @Override
    public void mergeAll(Map<? extends K, ? extends V> m, Merger<java.util.Map.Entry<K, V>> merger) {
        map.mergeAll(m, merger);
    }

    @Override
    public void mergeAll(Iterable<java.util.Map.Entry<K, V>> entries, Merger<java.util.Map.Entry<K, V>> merger) {
        map.mergeAll(entries, merger);
    }

    @Override
    public PersistentTreeMap<K, V> toPersistentMap() {
        return map.toPersistentMap();
    }

    
    private static class MMap<K, V> extends AbstractTreeMap<K, V, MMap<K, V>> {
        
        private final Thread owner = Thread.currentThread();
        
        private UpdateContext<Map.Entry<K, V>>  updateContext;
        
        private Node<K, V> root;
        
        private int size;
        
        private MMap() {
            super();
            this.root = null;
            this.size = 0;
            updateContext = new UpdateContext<Map.Entry<K, V>>();
        }
        
        private MMap(Comparator<? super K> comparator) {
            this(comparator, null, 0);
        }
    
        private MMap(Comparator<? super K> comparator, Node<K, V> root, int size) {
            super(comparator);
            this.updateContext = new UpdateContext<Map.Entry<K, V>>();
            this.root = root;
            this.size = size;
        }
    
        @Override
        protected Node<K, V> root() {
            verifyThread();
            return root;
        }
    
        @Override
        protected MMap<K, V> self() {
            return this;
        }
        
        public PersistentTreeMap<K, V> toPersistentMap() {
            verifyThread();
            updateContext.commit();
            return new PersistentTreeMap<K, V>(comparator, root, size);
        }
        
        private void verifyThread() {
            if (owner != Thread.currentThread()) {
                throw new IllegalStateException("MutableMap should only be accessed form the thread it was created in.");
            }
        }
    
        @Override
        public int size() {
            verifyThread();
            return size;
        }
    
        @Override
        protected MMap<K, V> doReturn(Comparator<? super K> comparator, Node<K, V> newRoot, int newSize) {
            this.root = newRoot;
            this.size = newSize;
            return this;
        }
        
        @Override
        protected UpdateContext<Map.Entry<K, V>> updateContext(Merger<Map.Entry<K, V>> merger) {
            verifyThread();
            if (updateContext.isCommitted()) {
                updateContext = new UpdateContext<Map.Entry<K, V>>(32, merger);
            } else {
                updateContext.validate();
                updateContext.merger(merger);
            }
            return updateContext;
        }
        
        @Override
        protected void commit(UpdateContext<?> updateContext) {
            // Nothing to do here
        }
    }
}