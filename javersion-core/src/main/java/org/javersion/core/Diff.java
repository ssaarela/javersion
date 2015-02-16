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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Diff {

    public static <K, V> Map<K, V> mergeDiff(Merge<K, V, ?> from, Map<K, V> to) {
        return diff(from, to, false);
    }

    public static <K, V> Map<K, V> mergeDiff(Map<K, V> from, Map<K, V> to) {
        return diff(from, to, false);
    }

    public static <K, V> Map<K, V> strictDiff(Merge<K, V, ?> from, Map<K, V> to) {
        return diff(from, to, true);
    }

    public static <K, V> Map<K, V> strictDiff(Map<K, V> from, Map<K, V> to) {
        return diff(from, to, true);
    }

    private static <K, V> Map<K, V> diff(Merge<K, V, ?> from, Map<K, V> to, boolean strict) {
        Map<K, V> diff = diff(from.getProperties(), to, strict);
        from.conflicts.keySet().stream().forEach(k -> {
            if (!diff.containsKey(k) && to.containsKey(k)) {
                diff.put(k, to.get(k));
            }
        });
        return diff;
    }

    private static <K, V> Map<K, V> diff(Map<K, V> from, Map<K, V> to, boolean strict) {
        notNull(from, "from");
        notNull(to, "to");

        if (from.size() < to.size()) {
            return diffBySmallerFrom(from, to, strict);
        } else {
            return diffBySmallerTo(from, to, strict);
        }
    }

    private static <K, V> Map<K, V> diffBySmallerFrom(Map<K, V> from, Map<K, V> to, boolean strict) {
        Map<K, V> diff = newHashMapWithExpectedSize(from.size() + to.size());
        Map<K, V> fromClone = strict ? new HashMap<>(from) : from;
        for (Entry<K, V> entry : to.entrySet()) {
            K key = entry.getKey();
            V newValue = entry.getValue();
            V oldValue = strict ? fromClone.remove(key) : fromClone.get(key);
            if (!equal(newValue, oldValue)) {
                diff.put(key, newValue);
            }
        }
        if (strict) {
            for (K key : fromClone.keySet()) {
                diff.put(key, null);
            }
        }
        return diff;
    }

    private static <K, V> Map<K, V> diffBySmallerTo(Map<K, V> from, Map<K, V> to, boolean strict) {
        Map<K, V> diff = newHashMapWithExpectedSize(from.size() + to.size());
        Map<K, V> toClone = strict ? new HashMap<>(to) : to;
        for (Entry<K, V> entry : from.entrySet()) {
            K key = entry.getKey();
            V oldValue = entry.getValue();
            V newValue = strict ? toClone.remove(key) : toClone.get(key);
            if (!equal(oldValue, newValue) && strict || to.containsKey(key)) {
                diff.put(key, newValue);
            }
        }
        if (strict) {
            for (Entry<K, V> entry : toClone.entrySet()) {
                diff.put(entry.getKey(), entry.getValue());
            }
        }
        return diff;
    }
}
