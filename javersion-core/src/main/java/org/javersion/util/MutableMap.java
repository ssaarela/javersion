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

import org.javersion.util.PersistentMap.UpdateContext;

public class MutableMap<K, V> extends AbstractTrieMap<K, V, MutableMap<K, V>> {
    
    private final UpdateContext updateContext;
    
    private Node<K, V> root;
    
    private int size;
    
    MutableMap(UpdateContext context, Node<K, V> root, int size) {
        this.updateContext = context;
        this.root = root;
        this.size = size;
    }

    @Override
    protected Node<K, V> getRoot() {
        return root;
    }

    @Override
    protected MutableMap<K, V> self() {
        return this;
    }

    @Override
    protected UpdateContext updateContext(int expectedUpdates) {
        return updateContext;
    }

    @Override
    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected MutableMap<K, V> doReturn(Node<? extends K, ? extends V> newRoot, int newSize) {
        this.root = (Node<K, V>) newRoot != null ? newRoot : HashNode.EMPTY;
        this.size = newSize;
        return this;
    }

}