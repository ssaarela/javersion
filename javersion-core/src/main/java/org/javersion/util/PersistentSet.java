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

public class PersistentSet<E> extends AbstractTrieSet<E, PersistentMap<E, Object>, PersistentSet<E>> {
    
    private final PersistentMap<E, Object> map;

    public PersistentSet() {
        this(PersistentMap.<E, Object>empty());
    }
    
    public PersistentSet(PersistentMap<E, Object> map) {
        this.map = Check.notNull(map, "map");
    }

    public MutableSet<E> toMutableSet() {
        return new MutableSet<E>(map.toMutableMap());
    }
    
    public ImmutableSet<E> asImmutableSet() {
        return new ImmutableSet<E>(this);
    }

    @Override
    PersistentSet<E> doReturn(PersistentMap<E, Object> newMap) {
        if (map == newMap) {
            return this;
        } else {
            return new PersistentSet<E>(newMap);
        }            
    }

    @Override
    PersistentMap<E, Object> getMap() {
        return map;
    }

    @Override
    public PersistentSet<E> update(int expectedUpdates, SetUpdate<E> updateFunction) {
        MutableSet<E> mutableSet = toMutableSet();
        updateFunction.apply(mutableSet);
        MutableMap<E, Object> newMap = mutableSet.getMap();
        if (newMap.getRoot() == map.getRoot()) {
            return this;
        } else {
            return new PersistentSet<>(newMap.toPersistentMap());
        }
    }
}
