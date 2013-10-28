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
import static java.lang.System.arraycopy;

import java.util.*;


import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

public class PersistentMap<K, V> implements Iterable<Map.Entry<K, V>>{
    
    private static final class Version{
        final int expectedSize;
        private int change = 0;
        public Version(int expectedSize) {
            this.expectedSize = expectedSize;
        }
        int getAndResetChange() {
            try {
                return change;
            } finally {
                change = 0;
            }
        }
        void recordAddition() {
            change = 1;
        }
        void recordRemoval() {
            change = -1;
        }
    }
    
    public static class Builder<K, V> {
        
        private Version version;
        
        private Node<K, V> root;
        
        private int size;
        
        public Builder(int size, int expectedSize) {
            this(null, size, expectedSize);
        }
        
        public Builder(Node<K, V> root, int size, int expectedSize) {
            this.version = new Version(expectedSize);
            this.root = root != null ? root : new HashNode<K, V>(version);
            this.size = size;
        }
        
        public Builder<K, V> put(Map.Entry<? extends K, ? extends V> entry) {
            root = root.assoc(version, entry);
            size += version.getAndResetChange();
            return this;
        }
        
        public Builder<K, V> put(K key, V value) {
            return put(new Entry<K, V>(key, value));
        }
        
        public Builder<K, V> putAll(Map<? extends K, ? extends V> map) {
            Check.notNull(map, "map");
            
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                put(toEntry(entry));
            }
            return this;
        }
        
        public Builder<K, V> remove(Object key) {
            root = root.dissoc(version, key);
            size += version.getAndResetChange();
            return this;
        }
        
        public Builder<K, V> removeAll(Iterable<? extends Object> keys) {
            Check.notNull(keys, "keys");
            
            for (Object key : keys) {
                remove(key);
            }
            return this;
        }
        
        public boolean containsKey(Object key) {
            return root.find(key) != null;
        }
        
        public V get(Object key) {
            Entry<K, V> entry = root.find(key);
            return entry != null ? entry.getValue() : null;
        }
        
        public int size() {
            return size;
        }
        
        public PersistentMap<K, V> build() {
            try {
                return new PersistentMap<K, V>(root, size);
            } finally {
                this.root = null;
                this.version = null;
            }
        }
    }
    
    private final Node<K, V> root;
    
    final int size;
    
    public static <K, V> Builder<K, V> builder() {
        return builder(32);
    }
    
    public static <K, V> Builder<K, V> builder(int expectedSize) {
        return new Builder<K, V>(0, expectedSize);
    }
    
    public static <K, V> Builder<K, V> builder(PersistentMap<K, V> parent) {
        return builder(parent, 32);
    }
    
    public static <K, V> Builder<K, V> builder(PersistentMap<K, V> parent, int expectedUpdateSize) {
        return new Builder<K, V>(parent.root, parent.size, expectedUpdateSize);
    }
    
    public PersistentMap() {
        this(null, 0);
    }
    
    private PersistentMap(Node<K, V> root, int size) {
        this.root = root;
        this.size = size;
    }
    
    public PersistentMap<K, V> assoc(K key, V value) {
        Entry<K, V> entry = new Entry<K, V>(key, value);
        if (root == null) {
            return new PersistentMap<K, V>(entry, 1);
        }
        return doReturn(root.assoc(new Version(1), entry), 1);
    }
    
    public PersistentMap<K, V> assocAll(Map<? extends K, ? extends V> map) {
        return builder(this, map.size()).putAll(map).build();
    }
    
    public PersistentMap<K, V> dissoc(Object key) {
        if (root == null) {
            return this;
        }
        return doReturn(root.dissoc(new Version(1), key), -1);
    }
    
    public int size() {
        return size;
    }
    
    public V get(Object key) {
        if (root == null) {
            return null;
        }
        Entry<K, V> entry = root.find(key);
        return entry != null ? entry.getValue() : null;
    }
    
    public boolean containsKey(Object key) {
        if (root == null) {
            return false;
        }
        return root.find(key) != null;
    }

    public Iterator<Map.Entry<K, V>> iterator() {
        if (root == null) {
            return Iterators.emptyIterator();
        }
        return root.iterator();
    }
    
    public AtomicMap<K, V> atomicMap() {
        return new AtomicMap<>(this);
    }
    
    
    private PersistentMap<K, V> doReturn(Node<K, V> newRoot, int change) {
        if (newRoot == root) {
            return this;
        } else {
            return new PersistentMap<K, V>(newRoot, size + change);
        }
    }
    
    private static <K, V> Entry<K, V> toEntry(Map.Entry<K, V> entry) {
        if (entry instanceof Entry) {
            return (Entry<K, V>) entry;
        } else {
            return new Entry<K, V>(entry.getKey(), entry.getValue());
        }
    }
    
    private static int hash(Object key) {
        return key != null ? key.hashCode() : 0;
    }

    static final int index(int bitmap, int bit){
        return Integer.bitCount(bitmap & (bit - 1));
    }

    static int bit(int hash, int level) {
        return bit(bitIndex(hash, level * 5));
    }

    static int bit(int bitIndex) {
        // (bitpos + 1)'th bit
        return 1 << bitIndex;
    }
    
    private static int bitIndex(int hash, int shift) {
        // xx xxxxx xxxxx xxxxx xxxxx NNNNN xxxxx   >>> 5
        // 00 00000 00000 00000 00000 00000 NNNNN   & 0x01f
        // return number (NNNNN) between 0..31
        return (hash >>> shift) & 0x01f;
    }
    
    
    
    static interface Node<K, V> extends Iterable<Map.Entry<K, V>>{

        public abstract Entry<K, V> find(Object key);

        public abstract Node<K, V> assoc(Version currentVersion, Map.Entry<? extends K, ? extends V> newEntry);

        public abstract Node<K, V> dissoc(Version currentVersion, Object key);

    }
    
    
    static abstract class AbstractNode<K, V> implements Node<K, V> {

        @Override
        public Entry<K, V> find(Object key) {
            return findInternal(0, hash(key), key);
        }

        @Override
        public Node<K, V> assoc(Version currentVersion, Map.Entry<? extends K, ? extends V> newEntry) {
            return assocInternal(currentVersion, 0, toEntry(newEntry));
        }

        @Override
        public Node<K, V> dissoc(Version currentVersion, Object key) {
            return dissocInternal(currentVersion, 0, hash(key), key);
        }
        
        abstract Entry<K, V> findInternal(int level, int hash, Object key);

        abstract AbstractNode<K, V> assocInternal(Version currentVersion, int level, Entry<? extends K, ? extends V> newEntry);

        abstract AbstractNode<K, V> dissocInternal(Version currentVersion, int level, int hash, Object key);
        
    }
    
    
    static final class Entry<K, V> extends AbstractNode<K, V> implements Map.Entry<K, V> {
        
        final int hash;
        
        final K key; 
        
        final V value;
        
        public Entry(K key, V value) {
            this(hash(key), key, value);
        }
        
        Entry(int hash, K key, V value) {
            this.hash = hash;
            this.key = key;
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

        @SuppressWarnings("unchecked")
        public AbstractNode<K, V> assocInternal(final Version currentVersion, final int level, final Entry<? extends K, ? extends V> newEntry) {
            if (equal(newEntry.key, key)) {
                if (equal(newEntry.value, value)) {
                    return this;
                } else {
                    currentVersion.recordAddition();
                    return (AbstractNode<K, V>) newEntry;
                }
            }
            else if (newEntry.hash == hash) {
                currentVersion.recordAddition();
                return new CollisionNode<K, V>(this, newEntry);
            } 
            else {
                return new HashNode<K, V>(currentVersion, 4)
                        .assocInternal(currentVersion, level, this)
                        .assocInternal(currentVersion, level, newEntry);
            }
        }

        @Override
        AbstractNode<K, V> dissocInternal(Version currentVersion, int level, int hash, Object key) {
            if (equal(key, this.key)) {
                currentVersion.recordRemoval();
                return null;
            }
            return this;
        }
        
        @Override
        public Entry<K, V> findInternal(int level, int hash, Object key) {
            if (equal(this.key, key)) {
                return this;
            }
            return null;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return Iterators.<Map.Entry<K, V>>singletonIterator(this);
        }
    }
    
    
    static final class HashNode<K, V> extends AbstractNode<K, V> {
        
        private final Version version;
        
        private int bitmap; 
        
        private AbstractNode<K, V>[] children;

        HashNode(Version version) {
            this(version, version.expectedSize);
        }

        @SuppressWarnings("unchecked")
        HashNode(Version version, int expectedSize) {
            this(version, 0, new AbstractNode[expectedSize < 32 ? expectedSize : 32]);
        }
        
        HashNode(Version version, int bitmap, AbstractNode<K, V>[] children) {
            this.version = version;
            this.bitmap = bitmap;
            this.children = children;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        AbstractNode<K, V> assocInternal(final Version currentVersion, final int level, final Entry<? extends K, ? extends V> newEntry) {
            int bit = bit(newEntry.hash, level);
            int index = index(bitmap, bit);
            if ((bitmap & bit) != 0) {
                AbstractNode<K, V> oldNode = children[index];
                AbstractNode<K, V> newNode = oldNode.assocInternal(currentVersion, level + 1, newEntry);
                if (newNode == oldNode) {
                    return this;
                } else {
                    HashNode<K, V> editable = cloneForReplace(currentVersion);
                    editable.children[index] = newNode;

                    return editable;
                }
            } else {
                currentVersion.recordAddition();
                HashNode<K, V> editable = cloneForInsert(currentVersion, index);
                editable.children[index] = (AbstractNode<K, V>) newEntry;
                editable.bitmap |= bit;
                
                return editable;
            }
        }

        @Override
        AbstractNode<K, V> dissocInternal(Version currentVersion, int level, int hash, Object key) {
            int bit = bit(hash, level);
            if ((bitmap & bit) == 0) {
                return this;
            }
            int index = index(bitmap, bit);
            AbstractNode<K, V> oldNode = children[index];
            AbstractNode<K, V> newNode = oldNode.dissocInternal(currentVersion, level + 1, hash, key);

            if (newNode == oldNode) {
                return this;
            } else if (newNode == null) {
                HashNode<K, V> editable = cloneForDelete(currentVersion, index);
                editable.bitmap = bitmap ^ bit;
                return editable;
            } else {
                HashNode<K, V> editable = cloneForReplace(currentVersion);
                editable.children[index] = newNode;

                return editable;
            }
        }
        
        @Override
        public Entry<K, V> findInternal(int level, int hash, Object key) {
            int bit = bit(hash, level);
            if ((bitmap & bit) == 0) {
                return null;
            }
            int index = index(bitmap, bit);
            AbstractNode<K, V> nodeOrEntry = children[index];
            return nodeOrEntry.findInternal(level + 1, hash, key);
        }
        

        @SuppressWarnings("unchecked")
        private HashNode<K, V> cloneForInsert(Version currentVersion, int index) {
            int childCount = Integer.bitCount(bitmap);
            boolean editInPlace = isEditInPlace(currentVersion);

            AbstractNode<K, V>[] newChildren;
            if (editInPlace && childCount < children.length) {
                newChildren = this.children;
            } else {
                newChildren = new AbstractNode[newSize(currentVersion, childCount)];
            }

            // make root for insertion
            arraycopy(children, 0, newChildren, 0, index);
            arraycopy(children, index, newChildren, index + 1, childCount - index);
            
            return withNewChildren(currentVersion, editInPlace, newChildren);
        }

        @SuppressWarnings("unchecked")
        private HashNode<K, V> cloneForDelete(Version currentVersion, int index) {
            int childCount = Integer.bitCount(bitmap);
            boolean editInPlace = isEditInPlace(currentVersion);

            AbstractNode<K, V>[] newChildren;
            if (editInPlace) {
                newChildren = this.children;
            } else {
                newChildren = new AbstractNode[childCount - 1];
            }

            // Delete given node
            arraycopy(children, 0, newChildren, 0, index);
            
            if (index + 1 < children.length) {
                arraycopy(children, index + 1, newChildren, index, childCount - index - 1);
                if (newChildren.length >= childCount) {
                    newChildren[childCount - 1] = null;
                }
            }
            
            return withNewChildren(currentVersion, editInPlace, newChildren);
        }

        private HashNode<K, V> withNewChildren(Version currentVersion,
                boolean editInPlace, AbstractNode<K, V>[] newChildren) {
            if (editInPlace) {
                children = newChildren;
                return this;
            } else {
                return new HashNode<K, V>(currentVersion, bitmap, newChildren);
            }
        }

        private boolean isEditInPlace(Version currentVersion) {
            boolean editInPlace = currentVersion != null && currentVersion == this.version;
            return editInPlace;
        }
        
        private int newSize(Version currentVersion, int childCount) {
            if (currentVersion == null) {
                return childCount < 32 ? childCount + 1 : 32;
            } else {
                return childCount < 16 ? 2*(childCount + 1) : 32;
            }
        }
        
        private HashNode<K, V> cloneForReplace(Version currentVersion) {
            if (currentVersion != null && currentVersion == this.version) {
                return this;
            } else {
                return new HashNode<>(currentVersion, bitmap, children.clone());
            }
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return Iterators.concat(new ArrayIterator<>(children, Integer.bitCount(bitmap)));
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("#{");
            boolean first = true;
            for (AbstractNode<K, V> child : children) {
                if (child != null) {
                    if (!first) {
                        sb.append(", ");
                    }
                    first = false;
                    sb.append(child);
                }
            }
            sb.append("}");
            return sb.toString();
        }
    }

    
    static final class CollisionNode<K, V> extends AbstractNode<K, V> {
        
        final int hash;
        
        private Entry<K, V>[] entries;

        @SuppressWarnings("unchecked")
        public CollisionNode(Entry<? extends K, ? extends V> first, Entry<? extends K, ? extends V> second) {
            this.hash = first.hash;
            this.entries = new Entry[] { first, second };
        }
        @SuppressWarnings("unchecked")
        private CollisionNode(Entry<? extends K, ? extends V>[] entries) {
            this.hash = entries[0].hash;
            this.entries = (Entry<K, V>[]) entries;
        }

        @Override
        public Entry<K, V> findInternal(int level, int hash, Object key) {
            for (Entry<K, V> entry : entries) {
                if (equal(entry.key, key)) {
                    return entry;
                }
            }
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public AbstractNode<K, V> assocInternal(final Version currentVersion, final int level, final Entry<? extends K, ? extends V> newEntry) {
            if (newEntry.hash == this.hash) {
                for (int i=0; i < entries.length; i++) {
                    if (equal(entries[i].key, newEntry.key)) {
                        if (equal(entries[i].value, newEntry.value)) {
                            return this;
                        }
                        Entry<K, V>[] newEntries = entries.clone();
                        newEntries[i] = (Entry<K, V>) newEntry;
                        return new CollisionNode<K, V>(newEntries);
                    }
                }
                
                currentVersion.recordAddition();

                Entry<K, V>[] newEntries = new Entry[entries.length + 1];
                arraycopy(entries, 0, newEntries, 0, entries.length);
                newEntries[entries.length] = (Entry<K, V>) newEntry;
                return new CollisionNode<K, V>(newEntries);
            }
            
            
            AbstractNode<K, V>[] newChildren = (currentVersion == null 
                    ? new AbstractNode[] { this, null } : new AbstractNode[] { this, null, null, null });

            AbstractNode<K, V> newNode = new HashNode<K, V>(currentVersion, bit(this.hash, level), newChildren);
            return newNode.assocInternal(currentVersion, level, newEntry);
        }

        @Override
        AbstractNode<K, V> dissocInternal(Version currentVersion, int level, int hash, Object key) {
            if (hash != this.hash) {
                return this;
            }
            for (int i=0; i < entries.length; i++) {
                if (equal(entries[i].key, key)) {
                    currentVersion.recordRemoval();

                    if (entries.length == 2) {
                        if (i == 1) {
                            return entries[0];
                        } else {
                            return entries[1];
                        }
                    }
                    @SuppressWarnings("unchecked")
                    Entry<K, V>[] newEntries = new Entry[entries.length - 1];
                    arraycopy(entries, 0, newEntries, 0, i);
                    if (i + 1 < entries.length) {
                        arraycopy(entries, i + 1, newEntries, i, entries.length - i - 1);
                    }
                    return new CollisionNode<K, V>(newEntries);
                }
            }
            return this;
        }
        
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return Iterators.concat(new ArrayIterator<>(entries));
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            for (Entry<K, V> entry : entries) {
                if (entry != null) {
                    if (!first) {
                        sb.append(", ");
                    }
                    first = false;
                    sb.append(entry);
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }
    
    static class ArrayIterator<K, V, T extends Node<K, V>> extends UnmodifiableIterator<Iterator<Map.Entry<K, V>>> {
        
        private final T[] array;
        
        private final int limit;
        
        private int pos = 0;
        
        public ArrayIterator(T[] array) {
            this(array, array.length);
        }
        
        public ArrayIterator(T[] array, int limit) {
            this.array = array;
            this.limit = limit;
        }
        
        @Override
        public boolean hasNext() {
            return pos < limit;
        }

        @Override
        public Iterator<Map.Entry<K, V>> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return array[pos++].iterator();
        }
    }
}
