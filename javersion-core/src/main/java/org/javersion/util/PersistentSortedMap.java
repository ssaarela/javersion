package org.javersion.util;

import static org.javersion.util.PersistentSortedMap.Color.BLACK;
import static org.javersion.util.PersistentSortedMap.Color.RED;
import static org.javersion.util.PersistentSortedMap.Mirror.LEFT;
import static org.javersion.util.PersistentSortedMap.Mirror.RIGHT;

import java.util.Comparator;
import java.util.Map;

import com.google.common.base.Objects;

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
            if (newRoot == null || newRoot == root) {
                return this;
            } else {
                return new PersistentSortedMap<K, V>(comparator, newRoot.blacken(context), size + context.getChangeAndReset());
            }
        }
    }

    public PersistentSortedMap<K, V> assocAll(Map<K, V> map) {
        UpdateContext<Node<K, V>> context = new UpdateContext<Node<K, V>>(map.size());
        Node<K, V> newRoot = root;
        Node<K, V> addedRoot = null;
        int newSize = size;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (newRoot == null) {
                newSize++;
                addedRoot = new Node<K, V>(context, entry.getKey(), entry.getValue(), BLACK);
            } else {
                addedRoot = newRoot.add(context, new Node<K, V>(context, entry.getKey(), entry.getValue(), RED), comparator);
            }
            if (addedRoot != null) {
                newRoot = addedRoot.blacken(context);
                newSize += context.getChangeAndReset();
            }
        }
        if (newRoot == root) {
            size = newSize;
            return this;
        } else {
            return new PersistentSortedMap<K, V>(comparator, newRoot.blacken(context), newSize);
        }
    }

    public PersistentSortedMap<K, V> dissoc(Object keyObj) {
        @SuppressWarnings("unchecked")
        K key = (K) keyObj;
        if (root == null) {
            return this;
        } else {
            UpdateContext<Node<K, V>> context = new UpdateContext<Node<K, V>>(1);
            Node<K, V> newRoot = root.remove(context, key, comparator);
            if (newRoot == null || newRoot == root) {
                return this;
            } else {
                return new PersistentSortedMap<K, V>(comparator, newRoot.blacken(context), size + context.getChangeAndReset());
            }
        }
    }

    
    public String toString() {
        return root == null ? "NIL" : root.toString();
    }
    
    static enum Color {
        RED {
            <K, V> Node<K, V> balanceInsert(UpdateContext<Node<K, V>> currentContext, Node<K, V> parent, Node<K, V> child, Mirror mirror) {
                Node<K, V> result;
                Node<K, V> left = mirror.leftOf(child);
                Node<K, V> right = mirror.rightOf(child);
                if (isRed(left)) {
                    Node<K, V> newRight = parent.toEditable(currentContext);
                    newRight.color = BLACK;
                    mirror.children(newRight, right, mirror.rightOf(parent));
                    
                    result = child.toEditable(currentContext);
                    result.color = RED;
                    mirror.children(result, left.blacken(currentContext), newRight);
                } 
                else if (isRed(right)) {
                    Node<K, V> newLeft = child.toEditable(currentContext);
                    newLeft.color = BLACK;
                    mirror.children(newLeft, left, mirror.leftOf(right));

                    Node<K, V> newRight = parent.toEditable(currentContext);
                    newRight.color = BLACK;
                    mirror.children(newRight, mirror.rightOf(right), mirror.rightOf(parent));
                    
                    result = right.toEditable(currentContext);
                    result.color = RED;
                    mirror.children(result, newLeft, newRight);
                } 
                else {
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
            return changeColor(currentContext, BLACK);
        }
        public Node<K, V> redden(UpdateContext<Node<K, V>> currentContext) {
            return changeColor(currentContext, RED);
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
            if (newChild == null) {
                return null;
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
        public Node<K, V> remove(UpdateContext<Node<K, V>> currentContext, final K key, Comparator<? super K> comparator) {
            int cmpr = comparator.compare(key, this.key);
            Mirror mirror;
            if (cmpr == 0) {
                currentContext.delete(this);
                return append(currentContext, left, right);
            } else if (cmpr < 0) {
                mirror = LEFT;
            } else {
                mirror = RIGHT;
            }
            Node<K, V> child = mirror.leftOf(this);
            if (child == null) {
                // key not found
                return this;
            }
            Node<K, V> newChild = child.remove(currentContext, key, comparator);
            if (newChild == child) {
                // key not found
                return this;
            }
            if (mirror == LEFT) {
                if (isBlack(left)) {
                    return LEFT.balanceDel(currentContext, this, newChild, right);
                } else {
                    return edit(currentContext, RED, newChild, right);
                }
            } else {
                if (isBlack(right)) {
                    return RIGHT.balanceDel(currentContext, this, left, newChild);
                } else {
                    return edit(currentContext, RED, left, newChild);
                }
            }
        }

        private Node<K, V> append(UpdateContext<Node<K, V>> currentContext, Node<K, V> left, Node<K, V> right) {
            if (left == null) {
                return right;
            } else if (right == null) {
                return left;
            } else if (left.color == RED) {
                if (right.color == RED) {
                    Node<K, V> app = append(currentContext, left.right, right.left);
                    if (app != null && app.color == RED) {
                        Node<K, V> newLeft = left.edit(currentContext, RED, left.left, app.left);
                        Node<K, V> newRight = right.edit(currentContext, RED, app.right, right.right);
                        return app.edit(currentContext, RED, newLeft, newRight);
                    } else {
                        Node<K, V> newRight = right.edit(currentContext, RED, app, right.right);
                        return left.edit(currentContext, RED, left.left, newRight);
                    }
                } else {
                    Node<K, V> app = append(currentContext, left.right, right);
                    return left.edit(currentContext, RED, left.left, app);
                }
            } else if (right.color == RED) {
                Node<K, V> app = append(currentContext, left, right.left);
                return right.edit(currentContext, RED, app, right.right);
            } else { // black/black
                Node<K, V> app = append(currentContext, left.right, right.left);
                if (app != null && app.color == RED) {
                    Node<K, V> newLeft = left.edit(currentContext, BLACK, left.left, app.left);
                    Node<K, V> newRight = right.edit(currentContext, BLACK, app.right, right.right);
                    return app.edit(currentContext, RED, newLeft, newRight);
                } else {
                    Node<K, V> newRight = right.edit(currentContext, BLACK, app, right.right);
                    return LEFT.balanceDel(currentContext, left, left.left, newRight);
                }
            }
        }
        
        private Node<K, V> changeColor(UpdateContext<Node<K, V>> currentContext, Color newColor) {
            Node<K, V> node = toEditable(currentContext);
            node.color = newColor;
            return node;
        }
        
        private Node<K, V> edit(UpdateContext<Node<K, V>> currentContext, Color newColor, Node<K, V> newLeft, Node<K, V> newRight) {
            Node<K, V> node = toEditable(currentContext);
            node.color = newColor;
            node.left = newLeft;
            node.right = newRight;
            return node;
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

    static boolean isBlack(Node<?, ?> node) {
        return node != null && node.color == BLACK;
    }

    static boolean isRed(Node<?, ?> node) {
        return node != null && node.color == RED;
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
            @Override
            <K, V> Node<K, V> balanceDel(UpdateContext<Node<K, V>> currentContext, Node<K, V> node, Node<K, V> left, Node<K, V> right) {
                if (isRed(right)) {
                    return node.edit(currentContext, RED, left, right.blacken(currentContext));
                } 
                else if (isBlack(left)) {
                    return LEFT.balance(currentContext, node, left.redden(currentContext), right);
                } 
                else if (isRed(left) && isBlack(left.right)) {
                    Node<K, V> newLeft = LEFT.balance(currentContext, left, left.left.redden(currentContext), left.right.left);
                    Node<K, V> newRight = node.edit(currentContext, BLACK, left.right.right, right);
                    return left.right.edit(currentContext, RED, newLeft, newRight);
                }
                else {
                    throw new IllegalStateException("Illegal invariant");
                }
            }
            @Override
            <K, V> Node<K, V> balance(UpdateContext<Node<K, V>> currentContext, Node<K, V> node, Node<K, V> left, Node<K, V> right) {
                if (isRed(right) && isRed(right.right)) {
                    Node<K, V> newLeft= node.edit(currentContext, BLACK, left, right.left);
                    return left.edit(currentContext, RED, newLeft, right.right.blacken(currentContext));
                } 
                else if (isRed(right) && isRed(right.left)) {
                    Node<K, V> newLeft = node.edit(currentContext, BLACK, left, right.left.left);
                    Node<K, V> newRight = right.edit(currentContext, BLACK, right.left.right, right.right);
                    return right.left.edit(currentContext, RED, newLeft, newRight);
                } 
                else {
                    return node.edit(currentContext, BLACK, left, right);
                }
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
        <K, V> Node<K, V> balanceDel(UpdateContext<Node<K, V>> currentContext, Node<K, V> node, Node<K, V> left, Node<K, V> right) {
            if (isRed(left)) {
                return node.edit(currentContext, RED, left.blacken(currentContext), right);
            } 
            else if (isBlack(right)) {
                return RIGHT.balance(currentContext, node, left, right.redden(currentContext));
            } 
            else if (isRed(right) && isBlack(right.left)) {
                Node<K, V> newLeft = node.edit(currentContext, BLACK, left, right.left.left);
                Node<K, V> newRight = RIGHT.balance(currentContext, right, right.left.right, right.right.redden(currentContext));
                return right.left.edit(currentContext, RED, newLeft, newRight);
            }
            else {
                throw new IllegalStateException("Illegal invariant");
            }
        }
        <K, V> Node<K, V> balance(UpdateContext<Node<K, V>> currentContext, Node<K, V> node, Node<K, V> left, Node<K, V> right) {
            if (isRed(left) && isRed(left.left)) {
                Node<K, V> newRight = node.edit(currentContext, BLACK, left.right, right);
                return left.edit(currentContext, RED, left.left.blacken(currentContext), newRight);
            } 
            else if (isRed(left) && isRed(left.right)) {
                Node<K, V> newLeft = left.edit(currentContext, BLACK, left.left, left.right.left);
                Node<K, V> newRight = node.edit(currentContext, BLACK, left.right.right, right);
                return left.right.edit(currentContext, RED, newLeft, newRight);
            }
            else {
                return node.edit(currentContext, BLACK, left, right);
            }
        }
    }

}
