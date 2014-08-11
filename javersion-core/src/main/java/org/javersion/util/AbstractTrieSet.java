/*
 * Copyright 2013 Samppa Saarela
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

import static com.google.common.base.Objects.equal;
import static com.google.common.collect.Iterators.transform;

import java.util.Collection;
import java.util.Iterator;

import org.javersion.util.AbstractTrieSet.Entry;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;


public abstract class AbstractTrieSet<E, S extends AbstractTrieSet<E, S>> extends AbstractHashTrie<E, Entry<E>, S> implements Iterable<E> {
    
    @SuppressWarnings("rawtypes")
    private static final Function ELEMENT_TO_ENTRY = new Function() {
        @SuppressWarnings("unchecked")
        @Override
        public Entry apply(Object input) {
            return new Entry(input);
        }
    };

    @SuppressWarnings("rawtypes")
    private static final Function ENTRY_TO_ELEMENT = new Function() {
        @Override
        public Object apply(Object input) {
            return ((Entry) input).element();
        }
    };

    public S conj(E element) {
        final UpdateContext<Entry<E>> updateContext = updateContext(1, null);
        try {
            return doAdd(updateContext, new Entry<E>(element));
        } finally {
            commit(updateContext);
        }
    }
    
    public S conjAll(final Collection<? extends E> elements) {
        return conjAll(elements, elements.size());
    }

    public S conjAll(AbstractTrieSet<? extends E, ?> elements) {
        return conjAll(elements, elements.size());
    }
    
    public S conjAll(final Iterable<? extends E> elements) {
        return conjAll(elements, 32);
    }

    @SuppressWarnings("unchecked")
    private S conjAll(final Iterable<? extends E> elements, int size) {
        final UpdateContext<Entry<E>> updateContext = updateContext(size, null);
        try {
            return (S) doAddAll(updateContext, transform(elements.iterator(), ELEMENT_TO_ENTRY));
        } finally {
            commit(updateContext);
        }
    }
    
    public S disjoin(Object element) {
        final UpdateContext<Entry<E>> updateContext = updateContext(1, null);
        try {
            return doRemove(updateContext, element);
        } finally {
            commit(updateContext);
        }
    }
    
    @SuppressWarnings("unchecked")
    public Iterator<E> iterator() {
        return Iterators.transform(doIterator(), ENTRY_TO_ELEMENT);
    }
    
    protected UpdateContext<Entry<E>> updateContext(int expectedSize, Merger<Entry<E>> merger) {
        return new UpdateContext<>(expectedSize, merger);
    }

    public boolean contains(Object o) {
        return containsKey(o);
    }

    public static final class Entry<E> extends AbstractHashTrie.Entry<E, Entry<E>> {
        
        public Entry(E element) {
            super(element);
        }
        
        public E element() {
            return key;
        }
        
        @Override
        public Node<E, Entry<E>> assocInternal(final UpdateContext<? super Entry<E>>  currentContext, final int shift, final int hash, final Entry<E> newEntry) {
            if (equal(this.key, newEntry.key)) {
                currentContext.merge(this, newEntry); 
                return this;
            } else {
                return split(currentContext, shift, hash, newEntry);
            }
        }
    }

}
