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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.javersion.util.PersistentMap.UpdateContext;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

public abstract class AbstractTrieMap<K, V, M extends AbstractTrieMap<K, V, M>> implements Iterable<Map.Entry<K, V>>{
    
    public abstract int size();

    
    protected abstract UpdateContext updateContext(int expectedUpdates);
    
    protected abstract Node<K, V> getRoot();
    
    protected abstract M self();
    
    protected abstract M doReturn(Node<? extends K, ? extends V> newRoot, int newSize);

    
    public M assoc(K key, V value) {
        return assoc(new Entry<K, V>(key, value));
    }
    
    public final M assoc(java.util.Map.Entry<? extends K, ? extends V> entry) {
        return assoc(updateContext(1), entry);
    }
    
    final M assoc(UpdateContext updateContext, Map.Entry<? extends K, ? extends V> entry) {
        Node<K, V> newRoot = getRoot().assoc(updateContext, toEntry(entry));
        return doReturn(newRoot, size() + updateContext.getChangeAndReset());
    }

    
    public final M assocAll(Map<? extends K, ? extends V> map) {
        return assocAll(updateContext(map.size()), map);
    }

    public final M assocAll(Collection<Map.Entry<? extends K, ? extends V>> entries) {
        return doAssocAll(updateContext(entries.size()), entries);
    }

    public final M assocAll(Iterable<Map.Entry<? extends K, ? extends V>> entries, int expectedUpdates) {
        return doAssocAll(updateContext(expectedUpdates), entries);
    }
    
    final M assocAll(UpdateContext updateContext, Map<? extends K, ? extends V> map) {
        Node<K, V> newRoot = getRoot();
        int size = size();
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            newRoot = newRoot.assoc(updateContext, toEntry(entry));
            size += updateContext.getChangeAndReset();
        }
        
        return doReturn(newRoot, size);
    }
    
    final M doAssocAll(UpdateContext updateContext, Iterable<Map.Entry<? extends K, ? extends V>> entries) {
        Node<K, V> newRoot = getRoot();
        int size = size();
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            newRoot = newRoot.assoc(updateContext, toEntry(entry));
            size += updateContext.getChangeAndReset();
        }
        
        return doReturn(newRoot, size);
    }

    public final M dissoc(Object key) {
        return dissoc(updateContext(1), key);
    }
        
    private M dissoc(UpdateContext updateContext, Object key) {
        Node<K, V> newRoot = getRoot().dissoc(updateContext, key);
        return doReturn(newRoot, size() + updateContext.getChangeAndReset());
    }
    
    public V get(Object key) {
        Entry<K, V> entry = getRoot().find(key);
        return entry != null ? entry.getValue() : null;
    }
    
    public boolean containsKey(Object key) {
        return getRoot().find(key) != null;
    }

    public Iterator<Map.Entry<K, V>> iterator() {
        return getRoot().iterator();
    }
    
    protected static <K, V> Entry<? extends K, ? extends V> toEntry(Map.Entry<? extends K, ? extends V> entry) {
        if (entry instanceof Entry) {
            return (Entry<? extends K, ? extends V>) entry;
        } else {
            return new Entry<K, V>(entry.getKey(), entry.getValue());
        }
    }
    
    
    static abstract class Node<K, V> implements Iterable<Map.Entry<K, V>> {

        public Entry<K, V> find(Object key) {
            return findInternal(0, hash(key), key);
        }

        public Node<K, V> assoc(UpdateContext currentContext, Entry<? extends K, ? extends V> newEntry) {
            Check.notNull(currentContext, "currentContext");
            return assocInternal(currentContext, 0, newEntry);
        }

        public Node<K, V> dissoc(UpdateContext currentContext, Object key) {
            Check.notNull(currentContext, "currentContext");
            return dissocInternal(currentContext, 0, hash(key), key);
        }
        
        static int hash(Object key) {
            return key == null ? 0 : key.hashCode();
        }

        final int index(int bitmap, int bit){
            return Integer.bitCount(bitmap & (bit - 1));
        }

        int bit(int hash, int level) {
            return bit(bitIndex(hash, level * 5));
        }

        int bit(int bitIndex) {
            // (bitpos + 1)'th bit
            return 1 << bitIndex;
        }
        
        int bitIndex(int hash, int shift) {
            // xx xxxxx xxxxx xxxxx xxxxx NNNNN xxxxx   >>> 5
            // 00 00000 00000 00000 00000 00000 NNNNN   & 0x01f
            // return number (NNNNN) between 0..31
            return (hash >>> shift) & 0x01f;
        }
        
        
        abstract Entry<K, V> findInternal(int level, int hash, Object key);

        abstract Node<K, V> assocInternal(UpdateContext currentContext, int level, Entry<? extends K, ? extends V> newEntry);

        abstract Node<K, V> dissocInternal(UpdateContext currentContext, int level, int hash, Object key);
        
    }
    
    @SuppressWarnings("rawtypes")
    private static final Iterator<Map.Entry> EMTPY_ITER = Iterators.emptyIterator();
    
    @SuppressWarnings("rawtypes")
    static final Node EMPTY_NODE = new Node() {
        
        @Override
        public Iterator<Map.Entry> iterator() {
            return EMTPY_ITER;
        }
        
        @Override
        Entry findInternal(int level, int hash, Object key) {
            return null;
        }
        
        @Override
        Node dissocInternal(UpdateContext currentContext, int level, int hash, Object key) {
            return this;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        Node assocInternal(UpdateContext currentContext, int level, Entry newEntry) {
            Node node = new HashNode<>(currentContext);
            return node.assocInternal(currentContext, level, newEntry);
        }
    };
    
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

        @SuppressWarnings("unchecked")
        public Node<K, V> assocInternal(final UpdateContext currentContext, final int level, final Entry<? extends K, ? extends V> newEntry) {
            if (equal(newEntry.key, key)) {
                if (equal(newEntry.value, value)) {
                    return this;
                } else {
                    return (Node<K, V>) newEntry;
                }
            }
            else if (newEntry.hash == hash) {
                currentContext.recordAddition();
                return new CollisionNode<K, V>(this, newEntry);
            } 
            else {
                return new HashNode<K, V>(currentContext, 4)
                        .assocInternal(currentContext, level, this)
                        .assocInternal(currentContext, level, newEntry);
            }
        }

        @Override
        Node<K, V> dissocInternal(UpdateContext currentContext, int level, int hash, Object key) {
            if (equal(key, this.key)) {
                currentContext.recordRemoval();
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
    
    
    static final class HashNode<K, V> extends Node<K, V> {
        
        private final UpdateContext updateContext;
        
        private int bitmap; 
        
        private Node<K, V>[] children;

        HashNode(UpdateContext updateContext) {
            this(updateContext, updateContext.expectedUpdates);
        }

        @SuppressWarnings("unchecked")
        HashNode(UpdateContext updateContext, int expectedSize) {
            this(updateContext, 0, new Node[expectedSize < 32 ? expectedSize : 32]);
        }
        
        HashNode(UpdateContext updateContext, int bitmap, Node<K, V>[] children) {
            this.updateContext = updateContext;
            this.bitmap = bitmap;
            this.children = children;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        Node<K, V> assocInternal(final UpdateContext currentContext, final int level, final Entry<? extends K, ? extends V> newEntry) {
            int bit = bit(newEntry.hash, level);
            int index = index(bitmap, bit);
            if ((bitmap & bit) != 0) {
                Node<K, V> oldNode = children[index];
                Node<K, V> newNode = oldNode.assocInternal(currentContext, level + 1, newEntry);
                if (newNode == oldNode) {
                    return this;
                } else {
                    HashNode<K, V> editable = cloneForReplace(currentContext);
                    editable.children[index] = newNode;

                    return editable;
                }
            } else {
                currentContext.recordAddition();
                HashNode<K, V> editable = cloneForInsert(currentContext, index);
                editable.children[index] = (Node<K, V>) newEntry;
                editable.bitmap |= bit;
                
                return editable;
            }
        }

        @Override
        Node<K, V> dissocInternal(UpdateContext currentContext, int level, int hash, Object key) {
            int bit = bit(hash, level);
            if ((bitmap & bit) == 0) {
                return this;
            }
            int index = index(bitmap, bit);
            Node<K, V> oldNode = children[index];
            Node<K, V> newNode = oldNode.dissocInternal(currentContext, level + 1, hash, key);

            if (newNode == oldNode) {
                return this;
            } else if (newNode == null) {
                int childCount = childCount();
                if (childCount == 1) {
                    return null;
                } else {
                    HashNode<K, V> editable = cloneForDelete(currentContext, index);
                    editable.bitmap = bitmap ^ bit;
                    return editable;
                }
            } else {
                HashNode<K, V> editable = cloneForReplace(currentContext);
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
            Node<K, V> nodeOrEntry = children[index];
            return nodeOrEntry.findInternal(level + 1, hash, key);
        }
        

        @SuppressWarnings("unchecked")
        private HashNode<K, V> cloneForInsert(UpdateContext currentContext, int index) {
            int childCount = childCount();
            boolean editInPlace = isEditInPlace(currentContext);

            Node<K, V>[] newChildren;
            if (editInPlace && childCount < children.length) {
                newChildren = this.children;
            } else {
                newChildren = new Node[newSize(currentContext, childCount)];
                if (index > 0) {
                    arraycopy(children, 0, newChildren, 0, index);
                }
            }

            // make room for insertion
            if (index < childCount) {
                arraycopy(children, index, newChildren, index + 1, childCount - index);
            }
            
            return withNewChildren(currentContext, editInPlace, newChildren);
        }

        private int childCount() {
            return Integer.bitCount(bitmap);
        }

        @SuppressWarnings("unchecked")
        private HashNode<K, V> cloneForDelete(UpdateContext currentContext, int index) {
            int childCount = childCount();
            boolean editInPlace = isEditInPlace(currentContext);

            Node<K, V>[] newChildren;
            if (editInPlace) {
                newChildren = this.children;
            } else {
                newChildren = new Node[childCount - 1];
                if (index > 0) {
                    arraycopy(children, 0, newChildren, 0, index);
                }
            }

            // Delete given node
            if (index + 1 < children.length) {
                arraycopy(children, index + 1, newChildren, index, childCount - index - 1);
                if (newChildren.length >= childCount) {
                    newChildren[childCount - 1] = null;
                }
            }
            
            return withNewChildren(currentContext, editInPlace, newChildren);
        }

        private HashNode<K, V> withNewChildren(UpdateContext currentContext,
                boolean editInPlace, Node<K, V>[] newChildren) {
            if (editInPlace) {
                children = newChildren;
                return this;
            } else {
                return new HashNode<K, V>(currentContext, bitmap, newChildren);
            }
        }

        private boolean isEditInPlace(UpdateContext currentContext) {
            boolean editInPlace = currentContext == this.updateContext;
            return editInPlace;
        }
        
        private int newSize(UpdateContext currentContext, int childCount) {
            if (currentContext.expectedUpdates == 1) {
                return childCount + 1;
            } else {
                return childCount < 16 ? 2*(childCount + 1) : 32;
            }
        }
        
        private HashNode<K, V> cloneForReplace(UpdateContext currentContext) {
            if (currentContext == this.updateContext) {
                return this;
            } else {
                return new HashNode<>(currentContext, bitmap, children.clone());
            }
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return Iterators.concat(new ArrayIterator<>(children, childCount()));
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

    
    static final class CollisionNode<K, V> extends Node<K, V> {
        
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
        public Node<K, V> assocInternal(final UpdateContext currentContext, final int level, final Entry<? extends K, ? extends V> newEntry) {
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
                
                currentContext.recordAddition();

                Entry<K, V>[] newEntries = new Entry[entries.length + 1];
                arraycopy(entries, 0, newEntries, 0, entries.length);
                newEntries[entries.length] = (Entry<K, V>) newEntry;
                return new CollisionNode<K, V>(newEntries);
            }
            
            
            Node<K, V>[] newChildren = (currentContext.expectedUpdates == 1 
                    ? new Node[] { this, null } : new Node[] { this, null, null, null });

            Node<K, V> newNode = new HashNode<K, V>(currentContext, bit(this.hash, level), newChildren);
            return newNode.assocInternal(currentContext, level, newEntry);
        }

        @Override
        Node<K, V> dissocInternal(UpdateContext currentContext, int level, int hash, Object key) {
            if (hash == this.hash) {
                for (int i=0; i < entries.length; i++) {
                    if (equal(entries[i].key, key)) {
                        currentContext.recordRemoval();
    
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
