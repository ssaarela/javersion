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
import java.util.Iterator;

import org.javersion.util.AbstractHashTrie.Node;

public class MutableHashSet<E> extends AbstractSet<E> implements MutableSet<E> {
    
    private MSet<E> set;
    
    public MutableHashSet() {
        this.set = new MSet<E>(32);
    }
    
    public MutableHashSet(int expectedSize) {
        this.set = new MSet<E>(expectedSize);
    }
    
    MutableHashSet(Node<E, AbstractTrieSet.Entry<E>> root, int size) {
        this.set = new MSet<E>(root, size);
    }

    @Override
    public boolean add(E e) {
        int size = set.size;
        set.conj(e);
        return size != set.size; 
    }
    
    @Override
    public boolean addAllFrom(Iterable<E> iterable) {
        int size = set.size;
        set.conjAll(iterable);
        return size != set.size; 
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
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
    public boolean remove(Object o) {
        int size = set.size;
        set.disjoin(o);
        return size != set.size; // No need to verify thread again here
    }

    @Override
    public void clear() {
        if (set.size() > 0) {
            set = new MSet<>(32);
        }
    }

    @Override
    public PersistentHashSet<E> toPersistentSet() {
        return set.toPersistentSet();
    }

    private static class MSet<E> extends AbstractTrieSet<E, MSet<E>> {
        
        private final Thread owner = Thread.currentThread();
        
        private UpdateContext<Entry<E>> updateContext;
        
        private Node<E, Entry<E>> root;
        
        private int size;
    
        private MSet(int expectedSize) {
            this(expectedSize, null, 0);
        }
        
        private MSet(Node<E, Entry<E>> root, int size) {
            this(32, root, size);
        }
        
        @SuppressWarnings("unchecked")
        private MSet(int expectedSize, Node<E, Entry<E>> root, int size) {
            this.updateContext = new UpdateContext<>(expectedSize);
            this.root = root != null ? root : EMPTY_NODE;
            this.size = size;
        }
        
        public PersistentHashSet<E> toPersistentSet() {
            verifyThread();
            updateContext.commit();
            return new PersistentHashSet<>(root, size);
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
        @SuppressWarnings("unchecked")
        protected MSet<E> doReturn(Node<E, Entry<E>> newRoot, int newSize) {
            verifyThread();
            root = newRoot != null ? newRoot : EMPTY_NODE;
            size = newSize;
            return this;
        }
    
        @Override
        protected Node<E, Entry<E>> root() {
            return root;
        }
        
        @Override
        protected UpdateContext<Entry<E>> updateContext(int expectedUpdates, Merger<Entry<E>> merger) {
            verifyThread();
            if (updateContext.isCommitted()) {
                updateContext = new UpdateContext<Entry<E>>(expectedUpdates, merger);
            } else {
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