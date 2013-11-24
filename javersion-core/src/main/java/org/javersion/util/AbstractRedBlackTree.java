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

import static org.javersion.util.AbstractRedBlackTree.Color.BLACK;
import static org.javersion.util.AbstractRedBlackTree.Color.RED;
import static org.javersion.util.AbstractRedBlackTree.Mirror.LEFT;
import static org.javersion.util.AbstractRedBlackTree.Mirror.RIGHT;

import java.util.*;

import org.javersion.util.AbstractRedBlackTree.Node;

import com.google.common.collect.UnmodifiableIterator;

public abstract class AbstractRedBlackTree<K, N extends Node<K, N>, T extends AbstractRedBlackTree<K, N, T>> {

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

    private final Comparator<? super K> comparator;

    @SuppressWarnings("unchecked")
    public AbstractRedBlackTree() {
        this((Comparator<K>) NATURAL);
    }

    public AbstractRedBlackTree(Comparator<? super K> comparator) {
        this.comparator = Check.notNull(comparator, "comparator");
    }

    public abstract int size();
    
    protected abstract T doReturn(UpdateContext<N> context, Comparator<? super K> comparator, N newRoot, int newSize);
    
    protected final N find(N root, Object keyObj) {
        @SuppressWarnings("unchecked")
        K key = (K) keyObj;
        N node = root;
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
    
    protected final N findMin(N node) {
        while (node != null) {
            if (node.left == null) {
                return node;
            } else {
                node = node.left;
            }
        }
        return null;
    }
    
    protected final N findMax(N node) {
        while (node != null) {
            if (node.right == null) {
                return node;
            } else {
                node = node.right;
            }
        }
        return null;
    }

    protected final T doAdd(UpdateContext<N> context, N root, N node) {
        if (root == null) {
            return doReturn(context, comparator, node.edit(context, BLACK, null, null), 1);
        } else {
            N newRoot = root.add(context, node.edit(context, RED, null, null), comparator);
            if (newRoot == null) {
                return self();
            } else {
                return doReturn(context, comparator, newRoot.blacken(context), size() + context.getChangeAndReset());
            }
        }
    }

    protected final T doAddAll(UpdateContext<N> context, N root, Iterable<N> nodes) {
        N newRoot = null;
        int newSize = size();
        for (N node : nodes) {
            if (root == null) {
                newSize++;
                newRoot = node.edit(context, BLACK, null, null);
            } else {
                newRoot = newRoot.add(context, node.edit(context, RED, null, null), comparator);
            }
            if (newRoot != null) {
                root = newRoot.blacken(context);
                newSize += context.getChangeAndReset();
            }
        }
        return doReturn(context, comparator, root, newSize);
    }
    
    protected Iterator<N> doIterator(N root, boolean asc) {
        return new RBIterator<K, N>(root, asc);
    }

    protected final T doRemove(UpdateContext<N> context, N root, Object keyObj) {
        @SuppressWarnings("unchecked")
        K key = (K) keyObj;
        if (root == null) {
            return self();
        } else {
            N newRoot = root.remove(context, key, comparator);
            if (newRoot != null) {
                return doReturn(context, comparator, newRoot.blacken(context), size() + context.getChangeAndReset());
            } else {
                return doReturn(context, comparator, null, 0);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }
    
    static abstract class Node<K, N extends Node<K, N>> implements Cloneable {
        final UpdateContext<N> context;
        final K key;
        Color color;
        N left;
        N right;
        
        public Node(UpdateContext<N> context, K key, Color color, N left, N right) {
            this.context = context;
            this.key = key;
            this.color = color;
            this.left = left;
            this.right = right;
        }
        
        @SuppressWarnings("unchecked")
        protected N self() {
            return (N) this;
        }

        N blacken(UpdateContext<N> currentContext) {
            return changeColor(currentContext, BLACK);
        }
        
        N redden(UpdateContext<N> currentContext) {
            return changeColor(currentContext, RED);
        }
        
        public N add(UpdateContext<N> currentContext, final N node, Comparator<? super K> comparator) {
            if (node == this) {
                return null;
            }
            N self = self();
            int cmpr = comparator.compare(node.key, key);
            Mirror mirror;
            if (cmpr == 0) {
                return replaceWith(currentContext, node);
            } else if (cmpr < 0) {
                mirror = LEFT;
            } else {
                mirror = RIGHT;
            }
            N left = mirror.leftOf(self());
            N newChild;
            if (left == null) {
                currentContext.insert(node);
                newChild = node;
            } else {
                newChild = left.add(currentContext, node, comparator);
            }
            if (newChild == null) {
                return null;
            }
            return color.add(currentContext, self, newChild, mirror);
        }
        
        public N remove(UpdateContext<N> currentContext, final K key, Comparator<? super K> comparator) {
            N self = self();
            int cmpr = comparator.compare(key, self.key);
            Mirror mirror;
            if (cmpr == 0) {
                currentContext.delete(self());
                return append(currentContext, left, right);
            } else if (cmpr < 0) {
                mirror = LEFT;
            } else {
                mirror = RIGHT;
            }
            N child = mirror.leftOf(self);
            if (child == null) {
                // key not found
                return self;
            }
            N newChild = child.remove(currentContext, key, comparator);
            if (newChild == child) {
                // key not found
                return self;
            }
            return mirror.remove(currentContext, self, newChild);
        }

        private N append(UpdateContext<N> currentContext, N left, N right) {
            if (left == null) {
                return right;
            } 
            else if (right == null) {
                return left;
            } 
            else if (isRed(left)) {
                if (isRed(right)) {
                    N app = append(currentContext, left.right, right.left);
                    if (isRed(app)) {
                        N newLeft = left.edit(currentContext, RED, left.left, app.left);
                        N newRight = right.edit(currentContext, RED, app.right, right.right);
                        return app.edit(currentContext, RED, newLeft, newRight);
                    } 
                    else {
                        N newRight = right.edit(currentContext, RED, app, right.right);
                        return left.edit(currentContext, RED, left.left, newRight);
                    }
                }
                else {
                    N newRight = append(currentContext, left.right, right);
                    return left.edit(currentContext, RED, left.left, newRight);
                }
            } 
            else if (isRed(right)) {
                N newLeft = append(currentContext, left, right.left);
                return right.edit(currentContext, RED, newLeft, right.right);
            } 
            else { // black/black
                N app = append(currentContext, left.right, right.left);
                if (isRed(app)) {
                    N newLeft = left.edit(currentContext, BLACK, left.left, app.left);
                    N newRight = right.edit(currentContext, BLACK, app.right, right.right);
                    return app.edit(currentContext, RED, newLeft, newRight);
                } 
                else {
                    N newRight = right.edit(currentContext, BLACK, app, right.right);
                    return balanceLeftDel(currentContext, left, left.left, newRight);
                }
            }
        }
        
        N changeColor(UpdateContext<N> currentContext, Color newColor) {
            N node = toEditable(currentContext);
            node.color = newColor;
            return node;
        }
        
        N edit(UpdateContext<N> currentContext, Color newColor, N newLeft, N newRight) {
            N node = toEditable(currentContext);
            node.color = newColor;
            node.left = newLeft;
            node.right = newRight;
            return node;
        }
        
        N toEditable(UpdateContext<N> currentContext) {
            if (this.context.isSameAs(currentContext)) {
                return self();
            } else {
                return cloneWith(currentContext);
            }
        }
        
        public String toString() {
            return toString(new StringBuilder(), 0).toString();
        }
        
        protected StringBuilder toString(StringBuilder sb, int level) {
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
        
        protected StringBuilder label(StringBuilder sb) {
            sb.append(color).append('(').append(key).append(')');
            return sb;
        }

        private StringBuilder indent(StringBuilder sb, int level) {
            sb.append('\n');
            for (int i=0; i < level; i++) {
                sb.append("   ");
            }
            return sb;
        }

        abstract N cloneWith(UpdateContext<N> currentContext);
        
        abstract N replaceWith(UpdateContext<N> currentContext, N node);

    }
    
    static enum Color {
        RED {
            @Override
            <K, N extends Node<K, N>> N balanceInsert(UpdateContext<N> currentContext, N parent, N child, Mirror mirror) {
                N result;
                N left = mirror.leftOf(child);
                N right = mirror.rightOf(child);
                if (isRed(left)) {
                    N newRight = parent.toEditable(currentContext);
                    newRight.color = BLACK;
                    mirror.children(newRight, right, mirror.rightOf(parent));
                    
                    result = child.toEditable(currentContext);
                    result.color = RED;
                    mirror.children(result, left.blacken(currentContext), newRight);
                } 
                else if (isRed(right)) {
                    N newLeft = child.toEditable(currentContext);
                    newLeft.color = BLACK;
                    mirror.children(newLeft, left, mirror.leftOf(right));

                    N newRight = parent.toEditable(currentContext);
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
            @Override
            <K, N extends Node<K, N>> N add(UpdateContext<N> currentContext, N node, N newChild, Mirror mirror) {
                N editable = node.toEditable(currentContext);
                mirror.setLeftOf(editable, newChild);
                editable.color = RED;
                return editable;
            }
        },
        BLACK;
        <K, N extends Node<K, N>> N balanceInsert(UpdateContext<N> currentContext, N parent, N child, Mirror mirror) {
            N result = parent.toEditable(currentContext);
            result.color = BLACK;
            mirror.children(result, child, mirror.rightOf(parent));
            return result;
        }
        <K, N extends Node<K, N>> N add(UpdateContext<N> currentContext, N node, N newChild, Mirror mirror) {
            N editable = node.toEditable(currentContext);
            mirror.setLeftOf(editable, newChild);
            return newChild.color.balanceInsert(currentContext, editable, newChild, mirror);
        }
    }

    static enum Mirror {
        RIGHT {
            @Override
            <K, N extends Node<K, N>> N leftOf(N node) {
                return node.right;
            }
            @Override
            <K, N extends Node<K, N>> N rightOf(N node) {
                return node.left;
            }
            @Override
            <K, N extends Node<K, N>> void setLeftOf(N node, N left) {
                node.right = left;
            }
            @Override
            <K, N extends Node<K, N>> void setRigthOf(N node, N right) {
                node.left = right;
            }
            @Override
            <K, N extends Node<K, N>> N remove(UpdateContext<N> currentContext, N node, N newChild) {
                if (isBlack(node.right)) {
                    return balanceRightDel(currentContext, node, node.left, newChild);
                } else {
                    return node.edit(currentContext, RED, node.left, newChild);
                }
            }
        },
        LEFT;
        <K, N extends Node<K, N>> N leftOf(N node) {
            return node.left;
        }
        <K, N extends Node<K, N>> N rightOf(N node) {
            return node.right;
        }
        <K, N extends Node<K, N>> void setLeftOf(N node, N left) {
            node.left = left;
        }
        <K, N extends Node<K, N>> void setRigthOf(N node, N right) {
            node.right = right;
        }
        <K, N extends Node<K, N>> void children(N node, N left, N right) {
            setLeftOf(node, left);
            setRigthOf(node, right);
        }
        <K, N extends Node<K, N>> N remove(UpdateContext<N> currentContext, N node, N newChild) {
            if (isBlack(node.left)) {
                return balanceLeftDel(currentContext, node, newChild, node.right);
            } else {
                return node.edit(currentContext, RED, newChild, node.right);
            }
        }
    }

    private static boolean isBlack(Node<?, ?> node) {
        return node != null && node.color == BLACK;
    }

    private static boolean isRed(Node<?, ?> node) {
        return node != null && node.color == RED;
    }
    
    private static <K, N extends Node<K, N>> N balanceLeftDel(UpdateContext<N> currentContext, N node, N left, N right) {
        if (isRed(left)) {
            return node.edit(currentContext, RED, left.blacken(currentContext), right);
        } 
        else if (isBlack(right)) {
            return balanceRight(currentContext, node, left, right.redden(currentContext));
        } 
        else if (isRed(right) && isBlack(right.left)) {
            N rightLeft = right.left;
            N newLeft = node.edit(currentContext, BLACK, left, rightLeft.left);
            N newRight = balanceRight(currentContext, right, rightLeft.right, right.right.redden(currentContext));
            return rightLeft.edit(currentContext, RED, newLeft, newRight);
        }
        else {
            throw new IllegalStateException("Illegal invariant");
        }
    }
    
    private static <K, N extends Node<K, N>> N balanceRightDel(UpdateContext<N> currentContext, N node, N left, N right) {
        if (isRed(right)) {
            return node.edit(currentContext, RED, left, right.blacken(currentContext));
        } 
        else if (isBlack(left)) {
            return balanceLeft(currentContext, node, left.redden(currentContext), right);
        } 
        else if (isRed(left) && isBlack(left.right)) {
            N leftRight = left.right;
            N newLeft = balanceLeft(currentContext, left, left.left.redden(currentContext), leftRight.left);
            N newRight = node.edit(currentContext, BLACK, leftRight.right, right);
            return leftRight.edit(currentContext, RED, newLeft, newRight);
        }
        else {
            throw new IllegalStateException("Illegal invariant");
        }
    }

    private static <K, N extends Node<K, N>> N balanceLeft(UpdateContext<N> currentContext, N node, N left, N right) {
        if (isRed(left) && isRed(left.left)) {
            N newRight = node.edit(currentContext, BLACK, left.right, right);
            return left.edit(currentContext, RED, left.left.blacken(currentContext), newRight);
        } 
        else if (isRed(left) && isRed(left.right)) {
            N leftRight = left.right;
            N newLeft = left.edit(currentContext, BLACK, left.left, leftRight.left);
            N newRight = node.edit(currentContext, BLACK, leftRight.right, right);
            return leftRight.edit(currentContext, RED, newLeft, newRight);
        }
        else {
            return node.edit(currentContext, BLACK, left, right);
        }
    }

    private static <K, N extends Node<K, N>> N balanceRight(UpdateContext<N> currentContext, N node, N left, N right) {
        if (isRed(right) && isRed(right.right)) {
            N newLeft= node.edit(currentContext, BLACK, left, right.left);
            return right.edit(currentContext, RED, newLeft, right.right.blacken(currentContext));
        } 
        else if (isRed(right) && isRed(right.left)) {
            N rightLeft = right.left;
            N newLeft = node.edit(currentContext, BLACK, left, rightLeft.left);
            N newRight = right.edit(currentContext, BLACK, rightLeft.right, right.right);
            return rightLeft.edit(currentContext, RED, newLeft, newRight);
        } 
        else {
            return node.edit(currentContext, BLACK, left, right);
        }
    }

    static final class RBIterator<K, N extends Node<K, N>> extends UnmodifiableIterator<N> {

        private final Deque<N> stack = new ArrayDeque<N>();

        private final boolean asc;
        
        public RBIterator(N root, boolean asc) {
            this.asc = asc;
            push(root);
        }
        
        private void push(N node) {
            while (node != null) {
                stack.addLast(node);
                node = (asc ? node.left : node.right);
            }
        }
        
        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public N next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            N result = stack.removeLast();
            push(asc ? result.right : result.left);
            return result;
        }
    }
}
