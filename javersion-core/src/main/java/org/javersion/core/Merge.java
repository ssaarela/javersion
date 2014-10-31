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

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Maps.filterValues;
import static com.google.common.collect.Maps.transformValues;

import java.util.Map;
import java.util.Set;

import org.javersion.util.PersistentHashMap;
import org.javersion.util.PersistentHashSet;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

public abstract class Merge<K, V> {

    protected static <K, V, T extends Version<K, V>> Iterable<Merge<K, V>> toMergeNodes(Iterable<VersionNode<K, V, T>> nodes) {
        return Iterables.transform(nodes, new Function<VersionNode<K, V, T>, Merge<K, V>>() {
            @Override
            public Merge<K, V> apply(VersionNode<K, V, T> input) {
                return input;
            }
        });
    }

    public final Function<VersionProperty<V>, V> getVersionPropertyValue = new Function<VersionProperty<V>, V>() {

        @Override
        public V apply(VersionProperty<V> input) {
            return input != null ? input.value : null;
        }

    };

    public final PersistentHashMap<K, VersionProperty<V>> mergedProperties;

    public final PersistentHashSet<Revision> mergedRevisions;

    public final Multimap<K, VersionProperty<V>> conflicts;

    protected Merge(MergeBuilder<K, V> mergeBuilder) {
        this.mergedProperties = mergeBuilder.getMergedProperties();
        this.mergedRevisions = mergeBuilder.getMergedRevisions();
        this.conflicts = mergeBuilder.getConflicts();
        setMergeHeads(mergeBuilder.getHeads());
    }

    public abstract Set<Revision> getMergeHeads();

    protected abstract void setMergeHeads(Set<Revision> heads);

    public Map<K, V> diff(Map<K, V> newProperties) {
        return Diff.diff(getPropertiesAsPlainMap(), newProperties);
    }

    public Map<K, V> getProperties() {
        return filterValues(getPropertiesAsPlainMap(), notNull());
    }

    private Map<K, V> getPropertiesAsPlainMap() {
        return transformValues(mergedProperties.asMap(), getVersionPropertyValue);
    }

    public Multimap<K, VersionProperty<V>> getConflicts() {
        return conflicts;
    }
}
