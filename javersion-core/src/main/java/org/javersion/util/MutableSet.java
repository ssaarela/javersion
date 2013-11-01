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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

public class MutableSet<E> extends AbstractSet<E> {

    private PersistentSet<E> set;
    
    public MutableSet() {
        this(new PersistentSet<E>());
    }
    
    public MutableSet(PersistentSet<E> set) {
        this.set = set;
    }
    
    public PersistentSet<E> persistentValue() {
        return set;
    }

    @Override
    public Iterator<E> iterator() {
        return set.iterator();
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public boolean add(final E e) {
        PersistentSet<E> newSet = set.conj(e);
        try {
            return set != newSet;
        } finally {
            this.set = newSet;
        }
    }

    @Override
    public boolean remove(final Object o) {
        PersistentSet<E> newSet = set.disjoin(o);
        try {
            return set != newSet;
        } finally {
            this.set = newSet;
        }
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        PersistentSet<E> newSet = set.conjAll(c);
        try {
            return set != newSet;
        } finally {
            this.set = newSet;
        }
    }

    @Override
    public void clear() {
        for (E e : set) {
            this.set = set.disjoin(e);
        }
    }
    
}