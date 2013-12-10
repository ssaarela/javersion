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

public class PersistentTreeMap<K, V> extends AbstractTreeMap<K, V, PersistentTreeMap<K, V>> {
    
    @SuppressWarnings("rawtypes")
    private static final PersistentTreeMap EMPTY = new PersistentTreeMap();

    @SuppressWarnings("unchecked")
    public static <K, V> PersistentTreeMap<K, V> empty() {
        return EMPTY;
    }
    
    public static <K, V> PersistentTreeMap<K, V> empty(Comparator<? super K> comparator) {
        return new PersistentTreeMap<K, V>(comparator);
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

    private PersistentTreeMap(Comparator<? super K> comparator, Node<K, V> root, int size) {
        super(comparator);
        this.root = root;
        this.size = size;
    }

    public int size() {
        return size;
    }
    
    protected Node<K, V> root() {
        return root;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected PersistentTreeMap<K, V> doReturn(UpdateContext<Node<K, V>> context, Comparator<? super K> comparator, Node<K, V> newRoot, int newSize) {
        context.commit();
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

}
