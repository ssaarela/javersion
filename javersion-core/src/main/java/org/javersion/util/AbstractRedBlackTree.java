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

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.javersion.util.AbstractRedBlackTree.Node;

import com.google.common.collect.UnmodifiableIterator;

public abstract class AbstractRedBlackTree<K, N extends Node<K, N>, This extends AbstractRedBlackTree<K, N, This>> {

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

    protected final Comparator<? super K> comparator;

    @SuppressWarnings("unchecked")
    public AbstractRedBlackTree() {
        this((Comparator<K>) NATURAL);
    }

    public AbstractRedBlackTree(Comparator<? super K> comparator) {
        this.comparator = Check.notNull(comparator, "comparator");
    }

    public abstract int size();
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    protected abstract This doReturn(Comparator<? super K> comparator, N newRoot, int newSize);

    private final This commitAndReturn(UpdateContext<? super N> context, Comparator<? super K> comparator, N newRoot, int newSize) {
        commit(context);
        return doReturn(comparator, newRoot, newSize);
    }
    
    protected void commit(UpdateContext<?> context) {
        context.commit();
    }
    
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

    protected final This doAdd(UpdateContext<? super N> context, N root, N node) {
        if (root == null) {
            context.insert(node);
            return commitAndReturn(context, comparator, node.edit(context, BLACK, null, null), 1);
        } else {
            N newRoot = root.add(context, node.edit(context, RED, null, null), comparator);
            if (newRoot == null) {
                return self();
            } else {
                return commitAndReturn(context, comparator, newRoot.blacken(context), size() + context.getChangeAndReset());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    protected final This doAddAll(UpdateContext<? super N> context, final N root, Iterable nodes) {
        N newRoot = root;
        N rootCandidate = null;
        int newSize = size();
        for (Object n : nodes) {
            @SuppressWarnings("unchecked")
            N node = (N) n;
            if (newRoot == null) {
                newSize++;
                rootCandidate = node.edit(context, BLACK, null, null);
            } else {
                rootCandidate = newRoot.add(context, node.edit(context, RED, null, null), comparator);
            }
            if (rootCandidate != null) {
                newRoot = rootCandidate.blacken(context);
                newSize += context.getChangeAndReset();
            }
        }
        return commitAndReturn(context, comparator, newRoot, newSize);
    }
    
    protected Iterator<N> doIterator(N root, boolean asc) {
        return new RBIterator<K, N>(root, asc);
    }
    
    protected Iterator<N> doRangeIterator(N root, boolean asc, K from, boolean fromInclusive, K to, boolean toInclusive) {
        return new RangeIterator<K, N>(root, asc, comparator, from, fromInclusive, to, toInclusive);
    }

    protected final This doRemove(UpdateContext<? super N> context, N root, Object keyObj) {
        @SuppressWarnings("unchecked")
        K key = (K) keyObj;
        if (root == null) {
            return self();
        } else {
            N newRoot = root.remove(context, key, comparator);
            if (!context.hasChanged()) {
                return self();
            } else if (newRoot != null) {
                return commitAndReturn(context, comparator, newRoot.blacken(context), size() + context.getChangeAndReset());
            } else {
                return commitAndReturn(context, comparator, null, 0);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    protected This self() {
        return (This) this;
    }
    
    static abstract class Node<K, This extends Node<K, This>> implements Cloneable {
        final UpdateContext<? super This> context;
        final K key;
        Color color;
        This left;
        This right;
        
        protected Node(UpdateContext<? super This> context, K key, Color color, This left, This right) {
            this.context = context;
            this.key = key;
            this.color = color;
            this.left = left;
            this.right = right;
        }
        
        @SuppressWarnings("unchecked")
        protected This self() {
            return (This) this;
        }

        This blacken(UpdateContext<? super This> currentContext) {
            return changeColor(currentContext, BLACK);
        }
        
        This redden(UpdateContext<? super This> currentContext) {
            return changeColor(currentContext, RED);
        }
        
        protected This add(UpdateContext<? super This> currentContext, final This node, Comparator<? super K> comparator) {
            This self = self();
            int cmpr = comparator.compare(node.key, key);
            if (cmpr == 0) {
                if (currentContext.merge(self, node)) {
                    return replaceWith(currentContext, node);
                } else {
                    return null;
                }
            } else if (cmpr < 0) {
                return LEFT.add(currentContext, self, node, comparator);
            } else {
                return RIGHT.add(currentContext, self, node, comparator);
            }
        }
        
        protected This remove(UpdateContext<? super This> currentContext, final K key, Comparator<? super K> comparator) {
            This self = self();
            int cmpr = comparator.compare(key, self.key);
            if (cmpr == 0) {
                currentContext.delete(self());
                return append(currentContext, left, right);
            } else if (cmpr < 0) {
                return LEFT.remove(currentContext, self, key, comparator);
            } else {
                return RIGHT.remove(currentContext, self, key, comparator);
            }
        }

        private This append(UpdateContext<? super This> currentContext, This left, This right) {
            if (left == null) {
                return right;
            } 
            else if (right == null) {
                return left;
            } 
            else if (isRed(left)) {
                if (isRed(right)) {
                    This app = append(currentContext, left.right, right.left);
                    if (isRed(app)) {
                        This newLeft = left.edit(currentContext, RED, left.left, app.left);
                        This newRight = right.edit(currentContext, RED, app.right, right.right);
                        return app.edit(currentContext, RED, newLeft, newRight);
                    } 
                    else {
                        This newRight = right.edit(currentContext, RED, app, right.right);
                        return left.edit(currentContext, RED, left.left, newRight);
                    }
                }
                else {
                    This newRight = append(currentContext, left.right, right);
                    return left.edit(currentContext, RED, left.left, newRight);
                }
            } 
            else if (isRed(right)) {
                This newLeft = append(currentContext, left, right.left);
                return right.edit(currentContext, RED, newLeft, right.right);
            } 
            else { // black/black
                This app = append(currentContext, left.right, right.left);
                if (isRed(app)) {
                    This newLeft = left.edit(currentContext, BLACK, left.left, app.left);
                    This newRight = right.edit(currentContext, BLACK, app.right, right.right);
                    return app.edit(currentContext, RED, newLeft, newRight);
                } 
                else {
                    This newRight = right.edit(currentContext, BLACK, app, right.right);
                    return balanceLeftDel(currentContext, left, left.left, newRight);
                }
            }
        }
        
        This changeColor(UpdateContext<? super This> currentContext, Color newColor) {
            This node = toEditable(currentContext);
            node.color = newColor;
            return node;
        }
        
        This edit(UpdateContext<? super This> currentContext, Color newColor, This newLeft, This newRight) {
            This node = toEditable(currentContext);
            node.color = newColor;
            node.left = newLeft;
            node.right = newRight;
            return node;
        }
        
        This toEditable(UpdateContext<? super This> currentContext) {
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

        abstract This cloneWith(UpdateContext<? super This> currentContext);
        
        abstract This replaceWith(UpdateContext<? super This> currentContext, This node);

    }
    
    static enum Color {
        RED {
            @Override
            <K, N extends Node<K, N>> N balanceInsert(UpdateContext<? super N> currentContext, N parent, N child, Mirror mirror) {
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
            <K, N extends Node<K, N>> N add(UpdateContext<? super N> currentContext, N node, N newChild, Mirror mirror) {
                N editable = node.toEditable(currentContext);
                mirror.setLeftOf(editable, newChild);
                editable.color = RED;
                return editable;
            }
        },
        BLACK;
        <K, N extends Node<K, N>> N balanceInsert(UpdateContext<? super N> currentContext, N parent, N child, Mirror mirror) {
            N result = parent.toEditable(currentContext);
            result.color = BLACK;
            mirror.children(result, child, mirror.rightOf(parent));
            return result;
        }
        <K, N extends Node<K, N>> N add(UpdateContext<? super N> currentContext, N node, N newChild, Mirror mirror) {
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
            <K, N extends Node<K, N>> N balanceDelete(UpdateContext<? super N> currentContext, N node, N newChild) {
                return balanceRightDel(currentContext, node, node.left, newChild);
            }
            @Override
            <K, N extends Node<K, N>> N delete(UpdateContext<? super N> currentContext, N node, N newChild) {
                return node.edit(currentContext, RED, node.left, newChild);
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
        <K, N extends Node<K, N>> N add(UpdateContext<? super N> currentContext, N self, N node, Comparator<? super K> comparator) {
            N left = leftOf(self);
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
            return self.color.add(currentContext, self, newChild, this);
        }
        <K, N extends Node<K, N>> N balanceDelete(UpdateContext<? super N> currentContext, N node, N newChild) {
            return balanceLeftDel(currentContext, node, newChild, node.right);
        }
        
        <K, N extends Node<K, N>> N delete(UpdateContext<? super N> currentContext, N node, N newChild) {
            return node.edit(currentContext, RED, newChild, node.right);
        }
        <K, N extends Node<K, N>> N remove(UpdateContext<? super N> currentContext, N self, final K key, Comparator<? super K> comparator) {
            N child = leftOf(self);
            if (child == null) {
                // key not found
                return self;
            }
            boolean balance = isBlack(leftOf(self));
            N newChild = child.remove(currentContext, key, comparator);
            if (!currentContext.hasChanged()) {
                return self;
            } else if (balance) {
                return balanceDelete(currentContext, self, newChild);
            } else {
                return delete(currentContext, self, newChild);
            }
        }
    }

    private static boolean isBlack(Node<?, ?> node) {
        return node != null && node.color == BLACK;
    }

    private static boolean isRed(Node<?, ?> node) {
        return node != null && node.color == RED;
    }
    
    private static <K, N extends Node<K, N>> N balanceLeftDel(UpdateContext<? super N> currentContext, N node, N left, N right) {
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
    
    private static <K, N extends Node<K, N>> N balanceRightDel(UpdateContext<? super N> currentContext, N node, N left, N right) {
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

    private static <K, N extends Node<K, N>> N balanceLeft(UpdateContext<? super N> currentContext, N node, N left, N right) {
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

    private static <K, N extends Node<K, N>> N balanceRight(UpdateContext<? super N> currentContext, N node, N left, N right) {
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

    static abstract class AbstractRBIterator<K, N extends Node<K, N>> extends UnmodifiableIterator<N> {

        final Deque<N> stack = new ArrayDeque<N>();

        final boolean asc;
        
        public AbstractRBIterator(boolean asc) {
            this.asc = asc;
        }
        
        protected abstract void push(N node);
        
        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public N next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            N result = stack.pop();
            push(asc ? result.right : result.left);
            return result;
        }
        
    }

    static class RBIterator<K, N extends Node<K, N>> extends AbstractRBIterator<K, N> {
        
        public RBIterator(N root, boolean asc) {
            super(asc);
            push(root);
        }
        
        protected void push(N node) {
            while (node != null) {
                stack.push(node);
                node = (asc ? node.left : node.right);
            }
        }
    }
    
    static class RangeIterator<K, N extends Node<K, N>> extends AbstractRBIterator<K, N> {

        final Comparator<? super K> comparator; 
        final K from;
        final boolean fromInclusive;
        final K to;
        final boolean toInclusive;

        public RangeIterator(N root, boolean asc, Comparator<? super K> comparator, K from, boolean fromInclusive, K to, boolean toInclusive) {
            super(asc);
            this.comparator = comparator;
            this.from = from;
            this.fromInclusive = fromInclusive;
            this.to = to;
            this.toInclusive = toInclusive;
            push(root);
        }

        @Override
        protected void push(N node) {
            while (node != null) {
                boolean fromIncluded = fromIncluded(node.key);
                boolean toIncluded = toIncluded(node.key);
                if (fromIncluded && toIncluded) {
                    stack.push(node);
                }
                if (asc) {
                    node = fromIncluded ? node.left : node.right;
                } else {
                    node = toIncluded ? node.right : node.left;
                }
            }
        }
        
        private boolean fromIncluded(K key) {
            return from == null || isIncluded(from, key, fromInclusive);
        }
        
        private boolean toIncluded(K key) {
            return to == null || isIncluded(key, to, toInclusive);
        }
        
        private boolean isIncluded(K key1, K key2, boolean inclusive) {
            int cmpr = comparator.compare(key1, key2);
            return cmpr < 0 || inclusive && cmpr == 0;
        }
    }
}
