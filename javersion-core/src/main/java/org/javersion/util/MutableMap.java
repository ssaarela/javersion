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

public class MutableMap<K, V> extends AbstractTrieMap<K, V, MutableMap<K, V>> {
    
    private final Thread owner = Thread.currentThread();
    
    private ContextReference<Entry<K, V>>  contextReference;
    
    private Node<K, V> root;
    
    private int size;
    
    @SuppressWarnings("unchecked")
    public MutableMap() {
        this(EMPTY_NODE, 0);
    }

    MutableMap(Node<K, V> root, int size) {
        this(new ContextReference<Entry<K, V>>(new UpdateContext<Entry<K, V>>(32, null)), root, size);
    }

    MutableMap(ContextReference<Entry<K, V>>  context, Node<K, V> root, int size) {
        this.contextReference = context;
        this.root = root;
        this.size = size;
    }

    @Override
    Node<K, V> getRoot() {
        verifyThread();
        return root;
    }

    @Override
    protected MutableMap<K, V> self() {
        return this;
    }
    
    public PersistentMap<K, V> toPersistentMap() {
        verifyThread();
        contextReference.commit();
        return PersistentMap.create(root, size);
    }
    
    private void verifyThread() {
        if (owner != Thread.currentThread()) {
            throw new IllegalStateException("MutableMap should only be accessed form the thread it was created in.");
        }
    }

    @Override
    protected ContextReference<Entry<K, V>>  contextReference(int expectedUpdates, Merger<Entry<K, V>> merger) {
        verifyThread();
        if (contextReference.isCommitted()) {
            contextReference = new ContextReference<Entry<K, V>>(new UpdateContext<Entry<K, V>>(expectedUpdates, merger));
        } else {
            contextReference.validate();
            UpdateContext<Entry<K, V>> context = contextReference.get();
            context.merger = merger;
        }
        return contextReference;
    }

    @Override
    public int size() {
        verifyThread();
        return size;
    }

    @Override
    public MutableMap<K, V> update(int expectedUpdates, MapUpdate<K, V> updateFunction, Merger<Entry<K, V>> merger) {
        verifyThread();
        updateFunction.apply(this);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected MutableMap<K, V> doReturn(ContextReference<Entry<K, V>> context, Node<K, V> newRoot, int newSize) {
        this.root = (Node<K, V>) (newRoot == null ? EMPTY_NODE : newRoot);
        this.size = newSize;
        return this;
    }

}