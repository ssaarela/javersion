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


public final class UpdateContext<T> implements Merger<T> {
    
    int expectedUpdates;
    
    Merger<T> merger;
    
    private int change = 0;
    
    UpdateContext(int expectedUpdates) {
        this(expectedUpdates, null);
    }
    UpdateContext(int expectedUpdates, Merger<T> merger) {
        this.expectedUpdates = expectedUpdates;
        this.merger = merger;
    }
    
    int getChangeAndReset() {
        try {
            return change;
        } finally {
            change = 0;
        }
    }

    @Override
    public void insert(T newEntry) {
        change = 1;
        if (merger != null) {
            merger.insert(newEntry);
        }
    }

    @Override
    public T merge(T oldEntry, T newEntry) {
        return merger == null ? newEntry : merger.merge(oldEntry, newEntry);
    }
    
    @Override
    public void delete(T oldEntry) {
        change = -1;
        if (merger != null) {
            merger.delete(oldEntry);
        }
    }
    
}