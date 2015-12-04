/*
 * Copyright 2015 Samppa Saarela
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
package org.javersion.core;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class VersionNodeIterable<K, V, M> implements Iterable<VersionNode<K, V, M>> {

    private final VersionNode<K, V, M> tip;

    public VersionNodeIterable(VersionNode<K, V, M> tip) {
        this.tip = tip;
    }

    @Override
    public Iterator<VersionNode<K, V, M>> iterator() {
        return new Iterator<VersionNode<K, V, M>>() {

            private VersionNode<K, V, M> current = tip;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public VersionNode<K, V, M> next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                VersionNode<K, V, M> next = current;
                current = (current.previousVersionNode != null ? current.previousVersionNode : null);
                return next;
            }
        };
    }

}
