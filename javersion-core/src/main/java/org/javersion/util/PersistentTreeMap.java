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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.javersion.util.PersistentTreeMap.Node;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Iterators;

public class PersistentTreeMap<K, V> extends AbstractRedBlackTree<K, Node<K, V>, PersistentTreeMap<K, V>> implements Iterable<Map.Entry<K, V>> {
    
    @SuppressWarnings("rawtypes")
    private static final PersistentTreeMap EMPTY = new PersistentTreeMap();
    
    @SuppressWarnings("rawtypes")
    private static final Function TO_MAP_ENTRY = new Function() {
        @Override
        public Object apply(Object input) {
            return (Map.Entry) input;
        }
    };

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
        super();
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

    public V get(K key) {
        Node<K, V> node = find(root, key);
        return node != null ? node.value : null;
    }
    
    Node<K, V> root() {
        return root;
    }

    public PersistentTreeMap<K, V> assoc(K key, V value) {
        UpdateContext<Node<K, V>> context = new UpdateContext<Node<K, V>>(1);
        return doAdd(context, root, new Node<K, V>(context, key, value, RED));
    }

    public PersistentTreeMap<K, V> assocAll(Map<K, V> map) {
        final UpdateContext<Node<K, V>> context = new UpdateContext<Node<K, V>>(map.size());
        return doAddAll(context, root, transform(map.entrySet(), new EntryToNode<K, V>(context)));
    }

    public PersistentTreeMap<K, V> dissoc(Object keyObj) {
        return doRemove(new UpdateContext<Node<K, V>>(1), root, keyObj);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return Iterators.transform(doIterator(root, true), TO_MAP_ENTRY);
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

    private static final class EntryToNode<K, V> implements Function<Entry<K, V>, Node<K, V>> {
        private final UpdateContext<Node<K, V>> context;

        private EntryToNode(UpdateContext<Node<K, V>> context) {
            this.context = context;
        }

        @Override
        public Node<K, V> apply(Entry<K, V> input) {
            if (input instanceof Node) {
                return (Node<K, V>) input;
            } else {
                return new Node<K, V>(context, input.getKey(), input.getValue(), RED);
            }
        }
    }

    static class Node<K, V> extends AbstractRedBlackTree.Node<K, Node<K,V>> implements Map.Entry<K, V>{
        final V value;
        
        public Node(UpdateContext<Node<K, V>> context, K key, V value, Color color) {
            this(context, key, value, color, null, null);
        }
        
        public Node(UpdateContext<Node<K, V>> context, K key, V value, Color color, Node<K, V> left, Node<K, V> right) {
            super(context, key, color, left, right);
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
        public Node<K, V> self() {
            return this;
        }
        
        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected Node<K, V> cloneWith(UpdateContext<Node<K, V>> currentContext) {
            return new Node<K, V>(currentContext, key, value, color, left, right);
        }

        @Override
        protected Node<K, V> replaceWith(UpdateContext<Node<K, V>> currentContext, Node<K, V> node) {
            if (Objects.equal(this.value, node.value)) {
                return null;
            }
            else if (node.context.isSameAs(currentContext)) {
                node.color = this.color;
                node.left = this.left;
                node.right = this.right;
                return node;
            } 
            else {
                return new Node<K, V>(currentContext, key, node.value, this.color, left, right);
            }
        }
        
        @Override
        protected StringBuilder label(StringBuilder sb) {
            sb.append(color).append('(').append(key).append(':').append(value).append(')');
            return sb;
        }
    }

}
