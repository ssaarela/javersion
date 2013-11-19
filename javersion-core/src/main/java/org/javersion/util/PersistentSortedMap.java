package org.javersion.util;

import static org.javersion.util.AbstractSortedTree.Color.RED;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import org.javersion.util.PersistentSortedMap.Node;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

public class PersistentSortedMap<K, V> extends AbstractSortedTree<K, Node<K, V>, PersistentSortedMap<K, V>> {
    
    @SuppressWarnings("rawtypes")
    private static final PersistentSortedMap EMPTY = new PersistentSortedMap();

    @SuppressWarnings("unchecked")
    public static <K, V> PersistentSortedMap<K, V> empty() {
        return EMPTY;
    }
    
    public static <K, V> PersistentSortedMap<K, V> empty(Comparator<? super K> comparator) {
        return new PersistentSortedMap<K, V>(comparator);
    }
    
    
    private final Node<K, V> root;

    private final int size;
    
    private PersistentSortedMap() {
        super();
        root = null;
        size = 0;
    }

    private PersistentSortedMap(Comparator<? super K> comparator) {
        super(comparator);
        root = null;
        size = 0;
    }

    private PersistentSortedMap(Comparator<? super K> comparator, Node<K, V> root, int size) {
        super(comparator);
        this.root = root;
        this.size = size;
    }

    public int size() {
        return size;
    }

    public V get(K key) {
        Node<K, V> node = find(key);
        return node != null ? node.value : null;
    }
    
    @Override
    protected Node<K, V> root() {
        return root;
    }

    public PersistentSortedMap<K, V> assoc(K key, V value) {
        UpdateContext<Node<K, V>> context = new UpdateContext<Node<K, V>>(1);
        return doAdd(context, new Node<K, V>(context, key, value, RED));
    }

    public PersistentSortedMap<K, V> assocAll(Map<K, V> map) {
        final UpdateContext<Node<K, V>> context = new UpdateContext<Node<K, V>>(map.size());
        return doAddAll(context, Iterables.transform(map.entrySet(), 
                new Function<Entry<K, V>, Node<K, V>>() {
                    @Override
                    public Node<K, V> apply(Entry<K, V> input) {
                        if (input instanceof Node) {
                            return (Node<K, V>) input;
                        } else {
                            return new Node<K, V>(context, input.getKey(), input.getValue(), RED);
                        }
                    }
                }));
    }

    public PersistentSortedMap<K, V> dissoc(Object keyObj) {
        return doRemove(new UpdateContext<Node<K, V>>(1), keyObj);
    }

    @Override
    protected PersistentSortedMap<K, V> self() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected PersistentSortedMap<K, V> doReturn(UpdateContext<Node<K, V>> context, Comparator<? super K> comparator, Node<K, V> newRoot, int newSize) {
        context.commit();
        if (newRoot == root) {
            return this;
        } else if (newRoot == null) {
            return EMPTY;
        }
        return new PersistentSortedMap<K, V>(comparator, newRoot, newSize);
    }

    
    public String toString() {
        return root == null ? "NIL" : root.toString();
    }

    static class Node<K, V> extends AbstractSortedTree.Node<K, Node<K,V>> implements Map.Entry<K, V>{
        V value;
        
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
            if (this.context.isSameAs(currentContext)) {
                this.value = node.value;
                return this;
            } else if (node.context.isSameAs(currentContext)) {
                node.color = this.color;
                node.left = this.left;
                node.right = this.right;
                return node;
            } else {
                return new Node<K, V>(currentContext, key, node.value, this.color, left, right);
            }
        }
        
        private StringBuilder label(StringBuilder sb) {
            sb.append(color).append('(').append(key).append(':').append(value).append(')');
            return sb;
        }
        
        public String toString() {
            return toString(new StringBuilder(), 0).toString();
        }
        
        private StringBuilder toString(StringBuilder sb, int level) {
            label(sb);

            indent(sb, level+1).append("left:");
            if (left != null) {
                left.toString(sb, level+1);
            } else {
                sb.append("NIL");
            }

            indent(sb, level+1).append("right:");
            if (right != null) {
                right.toString(sb, level+1);
            } else {
                sb.append("NIL");
            }
            return sb;
        }
        private StringBuilder indent(StringBuilder sb, int level) {
            sb.append('\n');
            for (int i=0; i < level; i++) {
                sb.append("   ");
            }
            return sb;
        }
    }
}
