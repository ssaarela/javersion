package org.javersion.util;

import static org.javersion.util.PersistentSortedMap.Color.BLACK;
import static org.javersion.util.PersistentSortedMap.Color.RED;
import static org.javersion.util.PersistentSortedMap.NodeTranslator.LEFT;
import static org.javersion.util.PersistentSortedMap.NodeTranslator.RIGHT;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Objects;

public class PersistentSortedMap<K, V> {

    private static class UpdateContext<K, V>{

        private MutableNode<K, V> root;

        private final Map<Node<K, V>, MutableNode<K, V>> nodes;

        private final MutableNode<K, V> nil = new MutableNode<K, V>(this, new Node<K, V>(null, null, BLACK)) {
            @Override
            public boolean isNil() {
                return true;
            }
            @Override
            public Color color() {
                return BLACK;
            }

            public Node<K, V> getNode() {
                return null;
            }

        };

        UpdateContext() {
            this.nodes = new LinkedHashMap<Node<K, V>, MutableNode<K, V>>();
        }
        MutableNode<K, V> put(Node<K, V> node) {
            MutableNode<K, V> mutable = new MutableNode<>(this, node);
            nodes.put(node, mutable);
            return mutable;
        }
        MutableNode<K, V> root(Node<K, V> node) {
            MutableNode<K, V> mutable = nodes.get(node);
            if (mutable == null) {
                node = node.clone();
                mutable = new MutableNode<>(this, node);
                nodes.put(node, mutable);
            }
            root = mutable;
            return mutable;
        }
        //        MutableNode<K, V> nil() {
            //            return nil;
        //        }
        MutableNode<K, V> get(final MutableNode<K, V> parent, final Node<K, V> node) {
            Check.notNull(parent, "parent");
            if (node == null) {
                nil.parent(parent);
                return nil;
            }
            MutableNode<K, V> mutable = nodes.get(node);
            if (mutable == null) {
                Node<K, V> clone = node.clone();
                mutable = new MutableNode<>(this, clone);
                nodes.put(clone, mutable);

                if (node == parent.node.left) {
                    parent.left(mutable);
                } else {
                    parent.right(mutable);
                }
                mutable.parent(parent);
            }
            return mutable;
        }
        void root(MutableNode<K, V> root) {
            this.root = root;
        }
        public String toString() {
            if (root == null) {
                return "null";
            } else {
                return root.toString();
            }
        }
    }

    private static class MutableNode<K, V> {

        private final UpdateContext<K, V> context;

        private final Node<K, V> node;

        private MutableNode<K, V> parent;

        public MutableNode(UpdateContext<K, V> context, Node<K, V> node) {
            this.context = context;
            this.node = node;
            this.parent = context.nil;
        }

        public boolean isNil() {
            return false;
        }

        public Node<K, V> getNode() {
            return node;
        }

        public K key() {
            return node.key;
        }
        public void key(K key) {
            this.node.key = key;
        }

        public V value() {
            return node.value;
        }
        public void value(V value) {
            this.node.value = value;
        }

        public void parent(MutableNode<K, V> mutable) {
            this.parent = mutable;
        }
        public boolean hasLeft() {
            return node.left != null;
        }
        public boolean hasRight() {
            return node.right != null;
        }
        public boolean leftIs(Color color) {
            return color == BLACK 
                    ? node.left == null || node.left.color == BLACK
                    : node.left != null && node.left.color == RED;
        }
        public boolean rightIs(Color color) {
            return color == BLACK 
                    ? node.right == null || node.right.color == BLACK
                    : node.right != null && node.right.color == RED;
        }
        public MutableNode<K, V> left() {
            return context.get(this, node.left);
        }
        public boolean isLeft() {
            return !parent.isNil() && (isNil() ? parent.node.left == null : node == parent.node.left);
        }
        public boolean isRight() {
            return !parent.isNil() && isNil() ? parent.node.right == null : node == parent.node.right;
        }
        public void left(MutableNode<K, V> mutable) {
            if (mutable.isNil()) {
                this.node.left = null;
            } else {
                this.node.left = mutable.node;
            }
        }

        public MutableNode<K, V> right() {
            return context.get(this, node.right);
        }

        public void right(MutableNode<K, V> mutable) {
            if (mutable.isNil()) {
                this.node.right = null;
            } else {
                this.node.right = mutable.node;
            }
        }

        public Color color() {
            return node.color;
        }
        public void color(Color color) {
            node.color = color;
        }

        public String toString() {
            return node.toString(new StringBuilder(), 0).toString();
        }
    }

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
        if (root == null) {
            return null;
        } else {
            int cmpr;
            Node<K, V> x = root;
            do {
                cmpr = comparator.compare(key, x.key);
                if (cmpr < 0) {
                    x = x.left;
                } else if (cmpr > 0) {
                    x = x.right;
                } else {
                    return x.value;
                }
            } while (x != null);
            return null;
        }
    }

    public PersistentSortedMap<K, V> assoc(K key, V value) {
        if (root == null) {
            // Null and type check
            comparator.compare(key, key);
            return new PersistentSortedMap<K, V>(
                    comparator,
                    new Node<K, V>(key, value, BLACK),
                    1);
        }
        UpdateContext<K, V> context = new UpdateContext<>();
        MutableNode<K, V> root = context.root(this.root);
        MutableNode<K, V> y = null;
        MutableNode<K, V> x = root;
        int cmpr = 0;
        while (!x.isNil()) {
            y = x;
            cmpr = comparator.compare(key, x.key());
            if (cmpr < 0) {
                x = x.left();
            } else if (cmpr > 0) {
                x = x.right();
            } else {
                if (Objects.equal(value, x.value())) {
                    return this;
                } else {
                    x.value(value);
                    return new PersistentSortedMap<K, V>(comparator, root.getNode(), size);
                }
            }
        }

        MutableNode<K, V> z = context.put(new Node<K, V>(key, value, RED));
        z.parent(y);
        if (y.isNil()) {
            return new PersistentSortedMap<K, V>(comparator, z.getNode(), size + 1);
        }
        if (cmpr < 0) {
            y.left(z);
        } else {
            y.right(z);
        }
        return postInsert(z);
    }

    private PersistentSortedMap<K, V> postInsert(MutableNode<K, V> x) {
        MutableNode<K, V> y;

        while (!x.parent.isNil() && isRed(x.parent)) {
            NodeTranslator translator;
            if (x.parent.isLeft()) {
                translator = LEFT;
            } else {
                translator = RIGHT;
            }
            y = translator.right(x.parent.parent);
            if (isRed(y)) {
                x.parent.color(BLACK);
                y.color(BLACK);
                x.parent.parent.color(RED);
                x = x.parent.parent;
            } else {
                if (translator.isRight(x)) {
                    x = x.parent;
                    rotate(translator.toLeft(), x);
                }
                x.parent.color(BLACK);
                x.parent.parent.color(RED);
                rotate(translator.toRight(), x.parent.parent);
            }
        }
        MutableNode<K, V> root = x.context.root;
        root.color(BLACK);
        return new PersistentSortedMap<K, V>(comparator, root.node, size + 1);
    }

    private void rotate(NodeTranslator translator, MutableNode<K, V> x) {
        MutableNode<K, V> y = translator.right(x);
        translator.right(x, translator.left(y));
        if (translator.hasLeft(y)) {
            translator.left(y).parent(x);
        }
        y.parent(x.parent);

        MutableNode<K, V> px = x.parent;
        if (px.isNil()) {
            y.context.root(y);
        } else if (translator.isLeft(x)) {
            translator.left(px, y);
        } else {
            translator.right(px, y);
        }
        translator.left(y, x);
        x.parent(y);
    }

    private boolean isRed(MutableNode<K, V> node) {
        return node.color() == RED;
    }

    Node<K, V> root() {
        return root;
    }

    public PersistentSortedMap<K, V> dissoc(Object keyObj) {
        if (root == null) {
            return this;
        }
        @SuppressWarnings("unchecked")
        K key = (K) keyObj;

        UpdateContext<K, V> context = new UpdateContext<>();
        MutableNode<K, V> root = context.root(this.root);
        MutableNode<K, V> z = null;
        MutableNode<K, V> x = root;
        int cmpr = -1;
        while (!x.isNil() && cmpr != 0) {
            z = x;
            cmpr = comparator.compare(key, x.key());
            if (cmpr < 0) {
                x = x.left();
            } else if (cmpr > 0) {
                x = x.right();
            }
        }
        if (cmpr != 0) {
            return this;
        }
        MutableNode<K, V> y;
        if (!z.hasLeft() || !z.hasRight()) {
            y = z;
        } else {
            y = successor(z);
        }
        if (y.hasLeft()) {
            x = y.left();
        } else {
            x = y.right();
        }
        x.parent(y.parent);
        if (y.parent.isNil()) {
            context.root(x);
        } else {
            if (y.isLeft()) {
                y.parent.left(x);
            } else {
                y.parent.right(x);
            }
        }
        if (y != z) {
            z.key(y.key());
            z.value(y.value());
        }
        if (y.color() == BLACK) {
            deleteFixup(x);
        }
        root = y.context.root;
        return new PersistentSortedMap<K, V>(comparator, root.getNode(), size - 1);
    }

    private void deleteFixup(MutableNode<K, V> x) {
        MutableNode<K, V> w;
        while (!x.parent.isNil() && x.color() == BLACK) {
            NodeTranslator translator;
            if (x.isLeft()) {
                translator = LEFT;
            } else {
                translator = RIGHT;
            }
            w = translator.right(x.parent);
            if (w.color() == RED) {
                w.color(BLACK);
                x.parent.color(RED);
                rotate(translator.toLeft(), x.parent);
                w = translator.right(x.parent);
            }
            if (translator.leftIs(w, BLACK) && translator.rightIs(w, BLACK)) {
                w.color(RED);
                x = x.parent;
            } else {
                MutableNode<K, V> px = x.parent;
                if (translator.rightIs(w, BLACK)) {
                    translator.left(w).color(BLACK);
                    w.color(RED);
                    rotate(translator.toRight(), w);
                    w = translator.right(px);
                }
                w.color(px.color());
                px.color(BLACK);
                translator.right(w).color(BLACK);
                rotate(translator.toLeft(), px);
                x = x.context.root;
            }
        }
        x.color(BLACK);
    }

    private MutableNode<K, V> successor(MutableNode<K, V> x) {
        if (x.hasRight()) {
            return minimum(x.right());
        }
        MutableNode<K, V> y = x.parent;
        while (!y.isNil() && x.isRight()) {
            x = y;
            y = x.parent;
        }
        return y;
    }

    private MutableNode<K, V> minimum(MutableNode<K, V> x) {
        while (x.hasLeft()) {
            x = x.left();
        }
        return x;
    }

    //  private Node<K, V> maximum(Node<K, V> x) {
    //      while (x.right != null) {
    //          x = x.right;
    //      }
    //      return x;
    //  }

    public String toString() {
        return root == null ? "null" : root.toString();
    }

    static enum NodeTranslator {
        LEFT,
        /**
         * Inverse of LEFT
         */
        RIGHT {
            @Override
            public <K, V> MutableNode<K, V> right(MutableNode<K, V> node) {
                return node.left();
            }
            @Override
            public <K, V> MutableNode<K, V> left(MutableNode<K, V> node) {
                return node.right();
            }
            @Override
            public <K, V> void right(MutableNode<K, V> node, MutableNode<K, V> newRight) {
                node.left(newRight);
            }
            @Override
            public <K, V> void left(MutableNode<K, V> node, MutableNode<K, V> newLeft) {
                node.right(newLeft);
            }
            @Override
            public NodeTranslator toLeft() {
                return RIGHT;
            }
            @Override
            public NodeTranslator toRight() {
                return LEFT;
            }
            @Override
            public boolean isLeft(MutableNode<?, ?> node) {
                return node.isRight();
            }
            @Override
            public boolean isRight(MutableNode<?, ?> node) {
                return node.isLeft();
            }
            @Override
            public boolean leftIs(MutableNode<?, ?> node, Color color) {
                return node.rightIs(color);
            }
            @Override
            public boolean rightIs(MutableNode<?, ?> node, Color color) {
                return node.leftIs(color);
            }
            @Override
            public <K, V> boolean hasLeft(MutableNode<K, V> node) {
                return node.hasRight();
            }
            @Override
            public <K, V> boolean hasRight(MutableNode<K, V> node) {
                return node.hasLeft();
            }
        };
        public boolean leftIs(MutableNode<?, ?> node, Color color) {
            return node.leftIs(color);
        }
        public boolean rightIs(MutableNode<?, ?> node, Color color) {
            return node.rightIs(color);
        }
        public boolean isLeft(MutableNode<?, ?> node) {
            return node.isLeft();
        }
        public boolean isRight(MutableNode<?, ?> node) {
            return node.isRight();
        }
        public <K, V> MutableNode<K, V> right(MutableNode<K, V> node) {
            return node.right();
        }
        public <K, V> MutableNode<K, V> left(MutableNode<K, V> node) {
            return node.left();
        }
        public <K, V> boolean hasLeft(MutableNode<K, V> node) {
            return node.hasLeft();
        }
        public <K, V> boolean hasRight(MutableNode<K, V> node) {
            return node.hasRight();
        }
        public <K, V> void right(MutableNode<K, V> node, MutableNode<K, V> newRight) {
            node.right(newRight);
        }
        public <K, V> void left(MutableNode<K, V> node, MutableNode<K, V> newLeft) {
            node.left(newLeft);
        }
        public NodeTranslator toLeft() {
            return LEFT;
        }
        public NodeTranslator toRight() {
            return RIGHT;
        }
    }

    static enum Color {
        RED,
        BLACK
    }

    static class Node<K, V> implements Map.Entry<K, V>, Cloneable {
        K key;
        V value;
        Color color;
        Node<K, V> left;
        Node<K, V> right;
        public Node(K key, V value, Color color) {
            this.key = key;
            this.value = value;
            this.color = color;
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
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
        @SuppressWarnings("unchecked")
        public Node<K, V> clone() {
            try {
                return (Node<K, V>) super.clone();
            } catch (CloneNotSupportedException e) {
                // Should never happen: die horribly!
                throw new Error(e);
            }
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
