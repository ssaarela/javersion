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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;


public abstract class AbstractTrieSet<E, M extends AbstractTrieMap<E, Object, M>, S extends AbstractTrieSet<E, M, S>> implements Iterable<E> {
    
    static final Object PRESENT = new Object(); 
    
    public S conj(E element) {
        return doReturn(getMap().assoc(element, PRESENT));
    }
    
    abstract M getMap();
    
    abstract S doReturn(M newMap);

    public S conjAll(final Collection<? extends E> elements) {
        return conjAll(elements, elements.size());
    }

    public S conjAll(AbstractTrieSet<? extends E, ?, ?> elements) {
        return conjAll(elements, elements.size());
    }

    private S conjAll(final Iterable<? extends E> elements, int size) {
        M newMap = getMap().update(
                size, 
                new MapUpdate<E, Object>() {
                    @Override
                    public void apply(MutableMap<E, Object> map) {
                        for (E e : elements) {
                            map.assoc(e, PRESENT);
                        }
                    }
                });
        return doReturn(newMap);
    }
    
    public S disjoin(Object element) {
        return doReturn(getMap().dissoc(element));
    }
    
    public int size() {
        return getMap().size();
    }
    
    public Iterator<E> iterator() {
        return Iterators.transform(getMap().iterator(), new Function<Map.Entry<E, Object>, E>() {

            @Override
            public E apply(java.util.Map.Entry<E, Object> input) {
                return input.getKey();
            }
            
        });
    }

    public boolean contains(Object o) {
        return getMap().containsKey(o);
    }

    public S update(SetUpdate<E> updateFunction) {
        return update(32, updateFunction);
    }
    
    public abstract S update(int expectedUpdates, SetUpdate<E> updateFunction);
    
}
