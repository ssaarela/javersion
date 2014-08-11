/*
 *  Copyright 2013 Samppa Saarela
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

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

public class PersistentTreeMap<K, V> extends AbstractTreeMap<K, V, PersistentTreeMap<K, V>> implements PersistentSortedMap<K, V> {
    
    @SuppressWarnings("rawtypes")
    public static final PersistentTreeMap EMPTY = new PersistentTreeMap();

    @SuppressWarnings("unchecked")
    public static <K, V> PersistentTreeMap<K, V> empty() {
        return EMPTY;
    }
    
    public static <K, V> PersistentTreeMap<K, V> empty(Comparator<? super K> comparator) {
        return new PersistentTreeMap<K, V>(comparator);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentTreeMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        return ((PersistentTreeMap<K, V>) EMPTY).assocAll(map);
    }
    
    public static <K, V> PersistentTreeMap<K, V> of() {
        return empty();
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentTreeMap<K, V> of(K k1, V v1) {
        return (PersistentTreeMap<K, V>) EMPTY.assoc(k1, v1);
    }
    
    public static <K, V> PersistentTreeMap<K, V> of(K k1, V v1, K k2, V v2) {
        MutableTreeMap<K, V> map = new MutableTreeMap<K, V>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map.toPersistentMap();
    }
    
    public static <K, V> PersistentTreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        MutableTreeMap<K, V> map = new MutableTreeMap<K, V>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map.toPersistentMap();
    }
    
    
    private final Node<K, V> root;

    private final int size;
    
    private PersistentTreeMap() {
        root = null;
        size = 0;
    }

    private PersistentTreeMap(Comparator<? super K> comparator) {
        super(comparator);
        root = null;
        size = 0;
    }

    PersistentTreeMap(Comparator<? super K> comparator, Node<K, V> root, int size) {
        super(comparator);
        this.root = root;
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }
    
    @Override
    protected Node<K, V> root() {
        return root;
    }

    @Override
    public MutableTreeMap<K, V> toMutableMap() {
        return new MutableTreeMap<>(comparator, root, size);
    }

    @Override
    public Map<K, V> asMap() {
        return new ImmutableMap<>(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected PersistentTreeMap<K, V> doReturn(Comparator<? super K> comparator, Node<K, V> newRoot, int newSize) {
        if (newRoot == root) {
            return this;
        } else if (newRoot == null) {
            return EMPTY;
        }
        return new PersistentTreeMap<K, V>(comparator, newRoot, newSize);
    }

    public String toString() {
        return root == null ? "NIL" : root.toString();
    }

    @Override
    public Entry<K, V> getFirstEntry() {
        return findMin(root);
    }

    @Override
    public Entry<K, V> getLastEntry() {
        return findMax(root);
    }

}
