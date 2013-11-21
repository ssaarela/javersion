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

import static com.google.common.collect.Iterables.transform;
import static org.javersion.util.AbstractRedBlackTree.Color.RED;

import java.util.Comparator;

import org.javersion.util.PersistentSortedSet.Node;

import com.google.common.base.Function;

public class PersistentSortedSet<K> extends AbstractRedBlackTree<K, Node<K>, PersistentSortedSet<K>> {
    
    @SuppressWarnings("rawtypes")
    private static final PersistentSortedSet EMPTY = new PersistentSortedSet();

    @SuppressWarnings("unchecked")
    public static <K> PersistentSortedSet<K> empty() {
        return EMPTY;
    }
    
    public static <K> PersistentSortedSet<K> empty(Comparator<? super K> comparator) {
        return new PersistentSortedSet<K>(comparator);
    }
    
    
    private final Node<K> root;

    private final int size;
    
    private PersistentSortedSet() {
        super();
        root = null;
        size = 0;
    }

    private PersistentSortedSet(Comparator<? super K> comparator) {
        super(comparator);
        root = null;
        size = 0;
    }

    private PersistentSortedSet(Comparator<? super K> comparator, Node<K> root, int size) {
        super(comparator);
        this.root = root;
        this.size = size;
    }

    public int size() {
        return size;
    }

    public boolean contains(K key) {
        return find(root, key) != null;
    }
    
    Node<K> root() {
        return root;
    }

    public PersistentSortedSet<K> conj(K value) {
        UpdateContext<Node<K>> context = new UpdateContext<Node<K>>(1);
        return doAdd(context, root, new Node<K>(context, value, RED));
    }

    public PersistentSortedSet<K> conjAll(Iterable<K> coll) {
        final UpdateContext<Node<K>> context = new UpdateContext<Node<K>>(32);
        return doAddAll(context, root, transform(coll, new EntryToNode<K>(context)));
    }

    public PersistentSortedSet<K> disj(Object keyObj) {
        return doRemove(new UpdateContext<Node<K>>(1), root, keyObj);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected PersistentSortedSet<K> doReturn(UpdateContext<Node<K>> context, Comparator<? super K> comparator, Node<K> newRoot, int newSize) {
        context.commit();
        if (newRoot == root) {
            return this;
        } else if (newRoot == null) {
            return EMPTY;
        }
        return new PersistentSortedSet<K>(comparator, newRoot, newSize);
    }

    
    public String toString() {
        return root == null ? "NIL" : root.toString();
    }

    private static final class EntryToNode<K> implements Function<K, Node<K>> {
        private final UpdateContext<Node<K>> context;

        private EntryToNode(UpdateContext<Node<K>> context) {
            this.context = context;
        }

        @Override
        public Node<K> apply(K input) {
            return new Node<K>(context, input, RED);
        }
    }

    static class Node<K> extends AbstractRedBlackTree.Node<K, Node<K>> {

        public Node(UpdateContext<Node<K>> context, K key, Color color) {
            this(context, key, color, null, null);
        }
        
        public Node(UpdateContext<Node<K>> context, K key, Color color, Node<K> left, Node<K> right) {
            super(context, key, color, left, right);
        }
        
        public K getKey() {
            return key;
        }
        
        @Override
        public Node<K> self() {
            return this;
        }

        @Override
        protected Node<K> cloneWith(UpdateContext<Node<K>> currentContext) {
            return new Node<K>(currentContext, key, color, left, right);
        }

        @Override
        protected Node<K> replaceWith(UpdateContext<Node<K>> currentContext, Node<K> node) {
            return this;
        }
    }
}
