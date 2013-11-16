package org.javersion.util;

import static org.javersion.util.PersistentSortedMap.Color.BLACK;
import static org.javersion.util.PersistentSortedMap.Color.RED;
import static org.javersion.util.PersistentSortedMap.Mirror.LEFT;
import static org.javersion.util.PersistentSortedMap.Mirror.RIGHT;

import java.util.Comparator;
import java.util.Map;

public class PersistentSortedMap<K, V> {

    @SuppressWarnings("rawtypes")
    private final static Comparator<Comparable> NATURAL = new Comparator<Comparable>() {
        @SuppressWarnings("unchecked")
        @Override
        public int compare(Comparable left, Comparable right) {
            Check.notNull(left, "left");
            Check.notNull(right, "right");
            return left.compareTo(right);
        }
    };

    public static <K, V> PersistentSortedMap<K, V> empty() {
        return new PersistentSortedMap<K, V>();
    }

    private final Comparator<? super K> comparator;

    private Node<K, V> root;

    private int size;

    @SuppressWarnings("unchecked")
    public PersistentSortedMap() {
        this((Comparator<K>) NATURAL);
    }

    public PersistentSortedMap(Comparator<? super K> comparator) {
        this.comparator = Check.notNull(comparator, "comparator");
    }

    private PersistentSortedMap(Comparator<? super K> comparator, Node<K, V> root, int size) {
        this(comparator);
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
    
    Node<K, V> root() {
        return root;
    }
    
    private Node<K, V> find(Object keyObj) {
        @SuppressWarnings("unchecked")
        K key = (K) keyObj;
        Node<K, V> node = root;
        while (node != null) {
            int cmpr;
            cmpr = comparator.compare(key, node.key);
            if (cmpr < 0) {
                node = node.left;
            } else if (cmpr > 0) {
                node = node.right;
            } else {
                return node;
            }
        }
        return null;
    }

    public PersistentSortedMap<K, V> assoc(K key, V value) {
        UpdateContext<Node<K, V>> context = new UpdateContext<Node<K, V>>(1);
        if (root == null) {
            return new PersistentSortedMap<K, V>(comparator, new Node<K, V>(context, key, value, BLACK), 1);
        } else {
            Node<K, V> newRoot = root.add(context, new Node<K, V>(context, key, value, RED), comparator);
            if (newRoot == root) {
                return this;
            } else {
                return new PersistentSortedMap<K, V>(comparator, newRoot.blacken(context), size + context.getChangeAndReset());
            }
        }
    }

    public PersistentSortedMap<K, V> assocAll(Map<K, V> map) {
        UpdateContext<Node<K, V>> context = new UpdateContext<Node<K, V>>(map.size());
        Node<K, V> newRoot = root;
        int newSize = size;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (newRoot == null) {
                newSize++;
                newRoot = new Node<K, V>(context, entry.getKey(), entry.getValue(), BLACK);
            } else {
                newRoot = newRoot.add(context, new Node<K, V>(context, entry.getKey(), entry.getValue(), RED), comparator);
            }
            newRoot = newRoot.blacken(context);
            newSize += context.getChangeAndReset();
        }
        if (newRoot == root) {
            size = newSize;
            return this;
        } else {
            return new PersistentSortedMap<K, V>(comparator, newRoot.blacken(context), newSize);
        }
    }

    public PersistentSortedMap<K, V> dissoc(Object keyObj) {
        return this;
    }

    static enum Color {
        RED {
            <K, V> Node<K, V> balanceInsert(UpdateContext<Node<K, V>> currentContext, Node<K, V> parent, Node<K, V> child, Mirror mirror) {
                Node<K, V> result;
                Node<K, V> left = mirror.leftOf(child);
                Node<K, V> right = mirror.rightOf(child);
                if (left != null && left.color == RED) {
                    Node<K, V> newRight = parent.toEditable(currentContext);
                    newRight.color = BLACK;
                    mirror.children(newRight, right, mirror.rightOf(parent));
                    
                    result = child.toEditable(currentContext);
                    result.color = RED;
                    mirror.children(result, left.blacken(currentContext), newRight);
                } else if (right != null && right.color == RED) {
                    Node<K, V> newLeft = child.toEditable(currentContext);
                    newLeft.color = BLACK;
                    mirror.children(newLeft, left, mirror.leftOf(right));

                    Node<K, V> newRight = parent.toEditable(currentContext);
                    newRight.color = BLACK;
                    mirror.children(newRight, mirror.rightOf(right), mirror.rightOf(parent));
                    
                    result = right.toEditable(currentContext);
                    result.color = RED;
                    mirror.children(result, newLeft, newRight);
                } else {
                    result = BLACK.balanceInsert(currentContext, parent, child, mirror);
                }
                return result;
            }
        },
        BLACK {
            <K, V> Node<K, V> balanceInsert(UpdateContext<Node<K, V>> currentContext, Node<K, V> parent, Node<K, V> child, Mirror mirror) {
                Node<K, V> result = parent.toEditable(currentContext);
                result.color = BLACK;
                mirror.children(result, child, mirror.rightOf(parent));
                return result;
            }
        };
        abstract <K, V> Node<K, V> balanceInsert(UpdateContext<Node<K, V>> currentContext, Node<K, V> parent, Node<K, V> child, Mirror mirror);
    }

    static class Node<K, V> implements Map.Entry<K, V>, Cloneable {
        final UpdateContext<Node<K, V>> context;
        final K key;
        V value;
        Color color;
        Node<K, V> left;
        Node<K, V> right;
        
        public Node(UpdateContext<Node<K, V>> context, K key, V value, Color color) {
            this(context, key, value, color, null, null);
        }
        public Node(UpdateContext<Node<K, V>> context, K key, V value, Color color, Node<K, V> left, Node<K, V> right) {
            this.context = context;
            this.key = key;
            this.value = value;
            this.color = color;
            this.left = left;
            this.right = right;
        }
        @Override
        public K getKey() {
            return key;
        }
        @Override
        public V getValue() {
            return value;
        }
        public Node<K, V> blacken(UpdateContext<Node<K, V>> currentContext) {
            if (color == BLACK) {
                return this;
            } else if (context.isSameAs(currentContext)) {
                color = BLACK;
                return this;
            } else {
                return new Node<K, V>(currentContext, key, value, BLACK, left, right);
            }
        }
        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
        public String label() {
            return label(new StringBuilder()).toString();
        }
        private StringBuilder label(StringBuilder sb) {
            sb.append(color).append('(').append(key).append(':').append(value).append(')');
            return sb;
        }
        public String toString() {
            return toString(new StringBuilder(), 0).toString();
        }
        public Node<K, V> add(UpdateContext<Node<K, V>> currentContext, final Node<K, V> node, Comparator<? super K> comparator) {
            int cmpr = comparator.compare(node.key, this.key);
            Mirror mirror;
            if (cmpr == 0) {
                return replaceWith(currentContext, node);
            } else if (cmpr < 0) {
                mirror = LEFT;
            } else {
                mirror = RIGHT;
            }
            Node<K, V> left = mirror.leftOf(this);
            Node<K, V> newChild;
            if (left == null) {
                currentContext.insert(node);
                newChild = node;
            } else {
                newChild = left.add(currentContext, node, comparator);
            }
            Node<K, V> editable = toEditable(currentContext);
            mirror.setLeftOf(editable, newChild);

            if (color == BLACK) {
                return newChild.color.balanceInsert(currentContext, editable, newChild, mirror);
            } else {
                editable.color = RED;
                return editable;
            }
        }
        private Node<K, V> toEditable(UpdateContext<Node<K, V>> currentContext) {
            if (this.context.isSameAs(currentContext)) {
                return this;
            } else {
                return cloneWith(currentContext);
            }
        }
        private Node<K, V> cloneWith(UpdateContext<Node<K, V>> currentContext) {
            return new Node<K, V>(currentContext, key, value, color, left, right);
        }
        private Node<K, V> replaceWith(UpdateContext<Node<K, V>> currentContext, Node<K, V> node) {
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


    static enum Mirror {
        RIGHT {
            @Override
            <K, V> Node<K, V> leftOf(Node<K, V> node) {
                return node.right;
            }
            @Override
            <K, V> Node<K, V> rightOf(Node<K, V> node) {
                return node.left;
            }
            @Override
            <K, V> void setLeftOf(Node<K, V> node, Node<K, V> left) {
                node.right = left;
            }
            @Override
            <K, V> void setRigthOf(Node<K, V> node, Node<K, V> right) {
                node.left = right;
            }
        },
        LEFT;
        <K, V> Node<K, V> leftOf(Node<K, V> node) {
            return node.left;
        }
        <K, V> Node<K, V> rightOf(Node<K, V> node) {
            return node.right;
        }
        <K, V> void setLeftOf(Node<K, V> node, Node<K, V> left) {
            node.left = left;
        }
        <K, V> void setRigthOf(Node<K, V> node, Node<K, V> right) {
            node.right = right;
        }
        <K, V> void children(Node<K, V> node, Node<K, V> left, Node<K, V> right) {
            setLeftOf(node, left);
            setRigthOf(node, right);
        }
    }

}
