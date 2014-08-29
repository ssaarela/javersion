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
            if (!equal(newValue, oldValue)) {
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
            if (!equal(oldValue, newValue)) {
                diff.put(key, newValue);
            }
        }
        for (Entry<K, V> entry : toClone.entrySet()) {
            diff.put(entry.getKey(), entry.getValue());
        }
        return diff;
    }
}
