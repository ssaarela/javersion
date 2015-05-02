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

import static com.google.common.base.Objects.equal;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static org.javersion.util.Check.notNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Predicate;

public final class Diff {

    private Diff() {}

    public static <K, V> Map<K, V> diff(Map<K, V> from, Map<K, V> to) {
        return diff(from, to, p -> true);
    }

    public static <K, V> Map<K, V> diff(Map<K, V> from, Map<K, V> to, Predicate<K> filter) {
        notNull(from, "from");
        notNull(to, "to");
        notNull(filter, "filter");

        if (from.size() < to.size()) {
            return diffBySmallerFrom(from, to, filter);
        } else {
            return diffBySmallerTo(from, to, filter);
        }
    }

    private static <K, V> Map<K, V> diffBySmallerFrom(Map<K, V> from, Map<K, V> to, Predicate<K> filter) {
        Map<K, V> diff = newHashMapWithExpectedSize(from.size() + to.size());
        Map<K, V> fromClone = new LinkedHashMap<>(from);
        for (Entry<K, V> entry : to.entrySet()) {
            K key = entry.getKey();
            if (filter.apply(key)) {
                V newValue = entry.getValue();
                V oldValue = fromClone.remove(key);
                if (!equal(newValue, oldValue)) {
                    diff.put(key, newValue);
                }
            }
        }
        for (K key : fromClone.keySet()) {
            if (filter.apply(key)) {
                diff.put(key, null);
            }
        }
        return diff;
    }

    private static <K, V> Map<K, V> diffBySmallerTo(Map<K, V> from, Map<K, V> to, Predicate<K> filter) {
        Map<K, V> diff = newHashMapWithExpectedSize(from.size() + to.size());
        Map<K, V> toClone = new LinkedHashMap<>(to);
        for (Entry<K, V> entry : from.entrySet()) {
            K key = entry.getKey();
            if (filter.apply(key)) {
                V oldValue = entry.getValue();
                V newValue = toClone.remove(key);
                if (!equal(oldValue, newValue)) {
                    diff.put(key, newValue);
                }
            }
        }
        for (Entry<K, V> entry : toClone.entrySet()) {
            if (filter.apply(entry.getKey())) {
                diff.put(entry.getKey(), entry.getValue());
            }
        }
        return diff;
    }
}
