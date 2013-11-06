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


public class MutableSet<E> extends AbstractTrieSet<E, MutableMap<E,Object>, MutableSet<E>> {
    
    private final MutableMap<E, Object> map;
    
    public MutableSet(PersistentSet<E> set) {
        this(set.getMap().toMutableMap());
    }
    
    MutableSet(MutableMap<E,Object> map) {
        this.map = map;
    }
    
    public PersistentSet<E> toPersistentSet() {
        return new PersistentSet<>(map.toPersistentMap());
    }

    @Override
    MutableSet<E> doReturn(MutableMap<E, Object> newMap) {
        if (newMap != map) {
            throw new IllegalArgumentException("Mutable map is edit in place!");
        }
        return this;
    }

    @Override
    MutableMap<E, Object> getMap() {
        return map;
    }

    @Override
    public MutableSet<E> update(int expectedUpdates, SetUpdate<E> updateFunction) {
        updateFunction.apply(this);
        return this;
    }

}