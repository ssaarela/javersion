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

import java.util.Map;

public class PersistentMap<K, V> {
    
    private static final class Version{
        final int expectedSize;
        private boolean added = false;
        public Version(int expectedSize) {
            this.expectedSize = expectedSize;
        }
        boolean shouldIncrementSize() {
            try {
                return added;
            } finally {
                added = false;
            }
        }
        void markAddition() {
            added = true;
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
        
        public void put(K key, V value) {
            root = root.put(version, new Entry<K, V>(key, value));
            if (version.shouldIncrementSize()) {
                size++;
            }
        }
        
        public void putAll(Map<K, V> map) {
            Check.notNull(map, "map");
            
            for (Map.Entry<K, V> entry : map.entrySet()) {
                root = root.put(version, toEntry(entry));
                if (version.shouldIncrementSize()) {
                    size++;
                }
            }
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
    
    private final int size;
    
    public static <K, V> Builder<K, V> builder() {
        return new Builder<K, V>(0, 32);
    }
    
    public static <K, V> Builder<K, V> builder(int expectedSize) {
        return new Builder<K, V>(0, expectedSize);
    }
    
    public PersistentMap() {
        this(null, 0);
    }
    
    private PersistentMap(Node<K, V> root, int size) {
        this.root = root;
        this.size = size;
    }
    
    public Builder<K, V> updateBuilder() {
        return new Builder<>(root, size, 32);
    }
    
    public Builder<K, V> updateBuilder(int expectedSize) {
        return new Builder<>(root, size, expectedSize);
    }
    
    public PersistentMap<K, V> put(K key, V value) {
        Entry<K, V> entry = new Entry<K, V>(key, value);
        if (root == null) {
            return new PersistentMap<K, V>(entry, 1);
        }
        return doReturn(root.put(new Version(1), entry), 1);
    }
    
    private PersistentMap<K, V> doReturn(Node<K, V> newRoot, int additions) {
        if (newRoot == root) {
            return this;
        } else {
            return new PersistentMap<K, V>(newRoot, size + additions);
        }
    }
    
    public PersistentMap<K, V> putAll(Map<K, V> map) {
        Check.notNull(map, "map");
        
        Version version = new Version(map.size());
        Node<K, V> newRoot = root != null ? root : new HashNode<K, V>(version);
        int additions = 0;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            newRoot = newRoot.put(version, toEntry(entry));
            if (version.shouldIncrementSize()) {
                additions++;
            }
        }
        return doReturn(newRoot, additions);
    }
    
    public int size() {
        return size;
    }
    
    private static <K, V> Entry<K, V> toEntry(Map.Entry<K, V> entry) {
        if (entry instanceof Entry) {
            return (Entry<K, V>) entry;
        } else {
            return new Entry<K, V>(entry.getKey(), entry.getValue());
        }
    }
    
    public V get(K key) {
        if (root == null) {
            return null;
        }
        Entry<K, V> entry = root.find(key);
        return entry != null ? entry.getValue() : null;
    }
    
    public boolean containsKey(K key) {
        if (root == null) {
            return false;
        }
        return root.find(key) != null;
    }
    
    private static int hash(Object key) {
        return key != null ? key.hashCode() : 0;
    }

    static abstract class Node<K, V> {

        public Entry<K, V> find(K key) {
            return findInternal(0, hash(key), key);
        }

        public Node<K, V> put(Version currentVersion, Entry<K, V> newEntry) {
            return putInternal(currentVersion, 0, newEntry);
        }
        
        abstract Entry<K, V> findInternal(int level, int hash, K key);

        abstract Node<K, V> putInternal(final Version currentVersion, final int level, final Entry<K, V> newEntry);
        
    }
    
    static final class Entry<K, V> extends Node<K, V> implements Map.Entry<K, V> {
        
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

        public Node<K, V> putInternal(final Version currentVersion, final int level, final Entry<K, V> newEntry) {
            if (equal(newEntry.key, key)) {
                if (equal(newEntry.value, value)) {
                    return this;
                } else {
                    currentVersion.markAddition();
                    return newEntry;
                }
            }
            else if (newEntry.hash == hash) {
                currentVersion.markAddition();
                return new CollisionNode<K, V>(this, newEntry);
            } 
            else {
                return new HashNode<K, V>(currentVersion, 4)
                        .putInternal(currentVersion, level, this)
                        .putInternal(currentVersion, level, newEntry);
            }
        }
        
        @Override
        public Entry<K, V> findInternal(int level, int hash, K key) {
            if (equal(this.key, key)) {
                return this;
            }
            return null;
        }
    }
    
    static final class HashNode<K, V> extends Node<K, V> {
        
        private final Version version;
        
        private int bitmap; 
        
        private Node<K, V>[] children;

        HashNode(Version version) {
            this(version, version.expectedSize);
        }

        @SuppressWarnings("unchecked")
        HashNode(Version version, int expectedSize) {
            this(version, 0, new Node[expectedSize < 32 ? expectedSize : 32]);
        }
        
        HashNode(Version version, int bitmap, Node<K, V>[] children) {
            this.version = version;
            this.bitmap = bitmap;
            this.children = children;
        }
        
        @Override
        public Node<K, V> putInternal(final Version currentVersion, final int level, final Entry<K, V> newEntry) {
            int bit = bit(newEntry.hash, level);
            int index = index(bitmap, bit);
            if ((bitmap & bit) != 0) {
                Node<K, V> oldNode = children[index];
                Node<K, V> newNode = oldNode.putInternal(currentVersion, level + 1, newEntry);
                if (newNode == oldNode) {
                    return this;
                } else {
                    HashNode<K, V> editable = toEditable(currentVersion);
                    editable.children[index] = newNode;
                    return editable;
                }
            } else {
                currentVersion.markAddition();
                int childCount = Integer.bitCount(bitmap);
                if (childCount < children.length) {
                    HashNode<K, V> editable = toEditable(currentVersion);
                    arraycopy(children, index, editable.children, index + 1, childCount-index);
                    editable.children[index] = newEntry;
                    editable.bitmap |= bit;
                    return editable;
                } else {
                    @SuppressWarnings("unchecked")
                    Node<K, V>[] newChildren = new Node[newSize(childCount)];
                    arraycopy(children, 0, newChildren, 0, index);
                    newChildren[index] = newEntry;
                    arraycopy(children, index, newChildren, index + 1, childCount - index);
                    
                    return new HashNode<>(currentVersion, bitmap | bit, newChildren);
                }
            }
        }
        
        private int newSize(int childCount) {
             return childCount < 16 ? 2*(childCount + 1) : 32;
        }
        
        @Override
        public Entry<K, V> findInternal(int level, int hash, K key) {
            int bit = bit(hash, level);
            if ((bitmap & bit) == 0) {
                return null;
            }
            int index = index(bitmap, bit);
            Node<K, V> nodeOrEntry = children[index];
            return nodeOrEntry.findInternal(level + 1, hash, key);
        }

        private HashNode<K, V> toEditable(Version version) {
            if (this.version == version) {
                return this;
            } else {
                int childCount = Integer.bitCount(bitmap);
                @SuppressWarnings("unchecked")
                Node<K, V>[] newChildren = new Node[newSize(childCount)];
                arraycopy(children, 0, newChildren, 0, childCount);
                return new HashNode<>(version, bitmap, newChildren);
            }
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("#{");
            boolean first = true;
            for (Node<K, V> child : children) {
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

    private static final int index(int bitmap, int bit){
        return Integer.bitCount(bitmap & (bit - 1));
    }

    private static int bit(int hash, int level) {
        return bit(bitIndex(hash, level * 5));
    }

    private static int bit(int bitIndex) {
        // (bitpos + 1)'th bit
        return 1 << bitIndex;
    }
    
    private static int bitIndex(int hash, int shift) {
        // xx xxxxx xxxxx xxxxx xxxxx NNNNN xxxxx   >>> 5
        // 00 00000 00000 00000 00000 00000 NNNNN   & 0x01f
        // return number (NNNNN) between 0..31
        return (hash >>> shift) & 0x01f;
    }
    
    static final class CollisionNode<K, V> extends Node<K, V> {
        
        final int hash;
        
        private Entry<K, V>[] entries;

        @SafeVarargs
        public CollisionNode(Entry<K, V>... entries) {
            if (entries.length < 2) {
                throw new AssertionError("Collision requires at least two entries");
            }
            this.hash = entries[0].hash;
            this.entries = entries;
        }

        @Override
        public Entry<K, V> findInternal(int level, int hash, K key) {
            for (Entry<K, V> entry : entries) {
                if (equal(entry.key, key)) {
                    return entry;
                }
            }
            return null;
        }

        @Override
        public Node<K, V> putInternal(final Version currentVersion, final int level, final Entry<K, V> newEntry) {
            if (newEntry.hash == this.hash) {
                for (int i=0; i < entries.length; i++) {
                    if (equal(entries[i].key, newEntry.key)) {
                        if (equal(entries[i].value, newEntry.value)) {
                            return this;
                        }
                        Entry<K, V>[] newEntries = entries.clone();
                        newEntries[i] = newEntry;
                        return new CollisionNode<K, V>(newEntries);
                    }
                }
                
                currentVersion.markAddition();

                @SuppressWarnings("unchecked")
                Entry<K, V>[] newEntries = new Entry[entries.length + 1];
                arraycopy(entries, 0, newEntries, 0, entries.length);
                newEntries[entries.length] = newEntry;
                return new CollisionNode<K, V>(newEntries);
            }
            
            Node<K, V> newNode = new HashNode<>(currentVersion, entries.length + 4);
            for (Entry<K, V> entry : entries) {
                newNode = newNode.putInternal(currentVersion, level, entry);
            }
            return newNode.putInternal(currentVersion, level, newEntry);
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
}
