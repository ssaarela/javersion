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

import org.javersion.util.AbstractTreeMap.Node;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public abstract class AbstractTreeMap<K, V, This extends AbstractTreeMap<K, V, This>> 
        extends AbstractRedBlackTree<K, Node<K, V>, This> implements Iterable<Map.Entry<K, V>> {
    
    protected AbstractTreeMap() {
        super();
    }

    protected AbstractTreeMap(Comparator<? super K> comparator) {
        super(comparator);
    }

    public abstract int size();
    
    protected abstract Node<K, V> root();

    protected UpdateContext<Entry<K, V>> updateContext() {
        return updateContext(null);
    }
    protected UpdateContext<Entry<K, V>> updateContext(Merger<Entry<K, V>> merger) {
        return new UpdateContext<Entry<K, V>>(1, merger);
    }
    
    public V get(Object key) {
        Node<K, V> node = find(root(), key);
        return node != null ? node.value : null;
    }
    
    public V max() {
        Node<K, V> max = findMax(root());
        return max != null ? max.value : null;
    }
    
    public V min() {
        Node<K, V> min = findMin(root());
        return min != null ? min.value : null;
    }

    public This assoc(K key, V value) {
        UpdateContext<Entry<K, V>> context = updateContext();
        return doAdd(context, root(), new Node<K, V>(context, key, value, RED));
    }

    @SuppressWarnings("unchecked")
    public This assocAll(Map<? extends K, ? extends V> map) {
        final UpdateContext<Entry<K, V>> context = updateContext();
        return doAddAll(context, root(), transform(map.entrySet(), new EntryToNode(context)));
    }

    public This dissoc(Object keyObj) {
        return doRemove(updateContext(), root(), keyObj);
    }

    public This assocAll(Iterable<Entry<K, V>> entries) {
        final UpdateContext<Entry<K, V>> context = updateContext();
        return doAddAll(context, root(), entries);
    }

    public This merge(K key, V value, Merger<Entry<K, V>> merger) {
        final UpdateContext<Entry<K, V>> context = updateContext(merger);
        return doAdd(context, root(), new Node<K, V>(context, key, value, RED));
    }

    @SuppressWarnings("unchecked")
    public This mergeAll(Map<? extends K, ? extends V> map, Merger<Entry<K, V>> merger) {
        final UpdateContext<Entry<K, V>> context = updateContext(merger);
        return doAddAll(context, root(), transform(map.entrySet(), new EntryToNode(context)));
    }

    public This mergeAll(Iterable<Entry<K, V>> entries, Merger<Entry<K, V>> merger) {
        final UpdateContext<Entry<K, V>> context = updateContext(merger);
        return doAddAll(context, root(), entries);
    }

    public This dissoc(Object key, Merger<Entry<K, V>> merger) {
        final UpdateContext<Entry<K, V>> context = updateContext(merger);
        return doRemove(context, root(), key);
    }

    public boolean containsKey(Object key) {
        return find(root(), key) != null;
    }


    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return iterator(true);
    }

    public Iterator<Map.Entry<K, V>> iterator(boolean asc) {
        return Iterators.transform(doIterator(root(), true), MapUtils.<K, V>mapEntryFunction());
    }
    
    public Iterable<Map.Entry<K, V>> range(K from, K to) {
        return range(from, true, to, false, true);
    }
    
    public Iterable<Map.Entry<K, V>> range(K from, K to, boolean asc) {
        return range(from, true, to, false, asc);
    }

    public Iterable<Map.Entry<K, V>> range(final K from, final boolean fromInclusive, final K to, final boolean toInclusive) {
        return range(from, fromInclusive, to, toInclusive, true);
    }

    public Iterable<Map.Entry<K, V>> range(final K from, final boolean fromInclusive, final K to, final boolean toInclusive, final boolean asc) {
        return new Iterable<Map.Entry<K,V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return Iterators.transform(doRangeIterator(root(), asc, from, fromInclusive, to, toInclusive), 
                        MapUtils.<K, V>mapEntryFunction());
            }
        };
    }

    public Iterable<K> keys() {
        return Iterables.transform(this, MapUtils.<K>mapKeyFunction());
    }
    
    public Iterable<V> values() {
        return Iterables.transform(this, MapUtils.<V>mapValueFunction());
    }

    public String toString() {
        Node<K, V> root = root();
        return root == null ? "NIL" : root.toString();
    }

    @SuppressWarnings("rawtypes")
    private static final class EntryToNode implements Function {
        private final UpdateContext context;

        private EntryToNode(UpdateContext context) {
            this.context = context;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object apply(Object input) {
            if (input instanceof Node) {
                return (Node) input;
            } else {
                Entry entry = (Entry) input;
                return new Node(context, entry.getKey(), entry.getValue(), RED);
            }
        }
    }

    static class Node<K, V> extends AbstractRedBlackTree.Node<K, Node<K,V>> implements Map.Entry<K, V>{
        V value;
        
        public Node(UpdateContext<? super Node<K, V>> context, K key, V value, Color color) {
            this(context, key, value, color, null, null);
        }
        
        public Node(UpdateContext<? super Node<K, V>> context, K key, V value, Color color, Node<K, V> left, Node<K, V> right) {
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
        protected Node<K, V> cloneWith(UpdateContext<? super Node<K, V>> currentContext) {
            return new Node<K, V>(currentContext, key, value, color, left, right);
        }

        @Override
        protected Node<K, V> replaceWith(UpdateContext<? super Node<K, V>> currentContext, Node<K, V> node) {
            if (node == this || Objects.equal(this.value, node.value)) {
                return null;
            }
            else if (context.isSameAs(currentContext)) {
                this.value = node.value;
                return this;
            }
            else {
                node.color = this.color;
                node.left = this.left;
                node.right = this.right;
                return node;
            }
        }
        
        @Override
        protected StringBuilder label(StringBuilder sb) {
            sb.append(color).append('(').append(key).append(':').append(value).append(')');
            return sb;
        }
    }

}
