/*
 * Copyright 2014 Samppa Saarela
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

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static org.javersion.util.Check.notNull;

import java.util.*;
import java.util.Map.Entry;

import com.google.common.base.MoreObjects;

public final class Diff {

    private Diff() {}

    /**
     * SortedMap-optimized version of diff. With TreeMaps this is faster for small maps (e.g. &lt; 100)
     * but gets slower after that pretty fast.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> SortedMap<K, V> diff(SortedMap<K, V> from, SortedMap<K, V> to) {
        Comparator comparator = MoreObjects.firstNonNull(from.comparator(), Comparator.naturalOrder());

        TreeMap<K, V> diff = new TreeMap<>(comparator);
        Iterator<Entry<K, V>> fromIter = from.entrySet().iterator();
        Iterator<Entry<K, V>> toIter = to.entrySet().iterator();

        Entry<K, V> oldEntry = null, newEntry = null;

        while (fromIter.hasNext() && toIter.hasNext()) {
            if (oldEntry == null) {
                oldEntry = fromIter.next();
            }
            if (newEntry == null) {
                newEntry = toIter.next();
            }
            int cmp = comparator.compare(oldEntry.getKey(), newEntry.getKey());
            if (cmp == 0) {
                V newValue = newEntry.getValue();
                if (!Objects.equals(oldEntry.getValue(), newValue)) {
                    diff.put(oldEntry.getKey(), newValue);
                }
                oldEntry = newEntry = null;
            }
            else if (cmp < 0) {
                diff.put(oldEntry.getKey(), null);
                oldEntry = null;
            } else {
                diff.put(newEntry.getKey(), newEntry.getValue());
                newEntry = null;
            }
        }

        if (oldEntry != null) {
            diff.put(oldEntry.getKey(), null);
        } else if (newEntry != null) {
            diff.put(newEntry.getKey(), newEntry.getValue());
        }

        while (fromIter.hasNext()) {
            diff.put(fromIter.next().getKey(), null);
        }
        while (toIter.hasNext()) {
            newEntry = toIter.next();
            diff.put(newEntry.getKey(), newEntry.getValue());
        }

        return diff;
    }

    public static <K, V> Map<K, V> diff(Map<K, V> from, Map<K, V> to) {
        notNull(from, "from");
        notNull(to, "to");

        if (from.size() < to.size()) {
            return diffBySmallerFrom(from, to);
        } else {
            return diffBySmallerTo(from, to);
        }
    }

    private static <K, V> Map<K, V> diffBySmallerFrom(Map<K, V> from, Map<K, V> to) {
        Map<K, V> diff = newHashMapWithExpectedSize(from.size() + to.size());
        Map<K, V> fromClone = new HashMap<>(from);
        for (Entry<K, V> entry : to.entrySet()) {
            K key = entry.getKey();
            V newValue = entry.getValue();
            V oldValue = fromClone.remove(key);
            if (!Objects.equals(newValue, oldValue)) {
                diff.put(key, newValue);
            }
        }
        for (K key : fromClone.keySet()) {
            diff.put(key, null);
        }
        return diff;
    }

    private static <K, V> Map<K, V> diffBySmallerTo(Map<K, V> from, Map<K, V> to) {
        Map<K, V> diff = newHashMapWithExpectedSize(from.size() + to.size());
        Map<K, V> toClone = new HashMap<>(to);
        for (Entry<K, V> entry : from.entrySet()) {
            K key = entry.getKey();
            V oldValue = entry.getValue();
            V newValue = toClone.remove(key);
            if (!Objects.equals(oldValue, newValue)) {
                diff.put(key, newValue);
            }
        }
        diff.putAll(toClone);
        return diff;
    }

}
