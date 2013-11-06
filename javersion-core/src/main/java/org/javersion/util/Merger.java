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

import org.javersion.util.AbstractTrieMap.Entry;

public interface Merger<K, V> {

    public void insert(Entry<K, V> newEntry);
    
    public Entry<K, V> merge(Entry<K, V> oldEntry, Entry<K, V> newEntry);

    public void delete(Entry<K, V> oldEntry);
    
}
