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
package org.javersion.core;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Maps.filterValues;
import static java.util.Collections.unmodifiableSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.javersion.util.Check;
import org.javersion.util.PersistentMap;
import org.javersion.util.PersistentSet;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public final class Merge<K, V> {

    public final Map<K, VersionProperty<V>> mergedProperties;

    public final Multimap<K, VersionProperty<V>> conflicts;

    public final Set<Long> revisions;
    
    public final Function<VersionProperty<V>, V> getVersionPropertyValue = new Function<VersionProperty<V>, V>() {

        @Override
        public V apply(VersionProperty<V> input) {
            return input != null ? input.value : null;
        }
        
    };

    public <T extends Version<K, V>> Merge(Iterable<VersionNode<K, V, T>> versions) {
        Check.notNull(versions, "versions");
        Iterator<VersionNode<K, V, T>> iter = versions.iterator();

        // No versions
        if (!iter.hasNext()) {
            mergedProperties = ImmutableMap.of();
            revisions = ImmutableSet.of();
            conflicts = ImmutableMultimap.of();
        } else {
            VersionNode<K, V, T> versionNode = next(iter);

            // One version
            if (!iter.hasNext()) {
                mergedProperties = versionNode.allProperties.asMap();
                revisions = ImmutableSet.of(versionNode.getRevision());
                conflicts = ImmutableMultimap.of();
            } 
            // More than one version -> merge!
            else {
                PersistentMap.Builder<K, VersionProperty<V>> mergedProperties = PersistentMap.builder(versionNode.allProperties);
                Set<Long> heads = Sets.newHashSet(versionNode.getRevision());

                ImmutableMultimap.Builder<K, VersionProperty<V>> conflicts = ImmutableMultimap.builder();

                PersistentSet.Builder<Long> mergedRevisions = PersistentSet.<Long>builder(versionNode.allRevisions);
                do {
                    versionNode = next(iter);

                    // Version already merged?
                    if (!mergedRevisions.contains(versionNode.getRevision())) {
                        for (Map.Entry<K, VersionProperty<V>> entry : versionNode.allProperties) {
                            K key = entry.getKey();
                            VersionProperty<V> nextValue = entry.getValue();

                            // nextValue derives from common ancestor?
                            if (!mergedRevisions.contains(nextValue.revision)) {
                                VersionProperty<V> previousValue = mergedProperties.get(key);

                                // New value
                                if (previousValue == null) {
                                    mergedProperties.put(entry);
                                }
                                // Conflicting value?
                                else if (!equal(previousValue.value, nextValue.value)) {
                                    conflicts.put(entry);
                                }
                            }
                        }
                        mergedRevisions.addAll(versionNode.allRevisions);

                        heads.removeAll(versionNode.allRevisions.asSet());
                        heads.add(versionNode.getRevision());
                    }
                } while (iter.hasNext());

                this.mergedProperties = mergedProperties.build().asMap();
                this.revisions = unmodifiableSet(heads);
                this.conflicts = conflicts.build();
            }
        }

    }

    public Map<K, V> getProperties() {
        return filterValues(Maps.transformValues(mergedProperties, getVersionPropertyValue), notNull());
    }
    
    private <T extends Version<K, V>> VersionNode<K, V, T> next(Iterator<VersionNode<K, V, T>> iter) {
        VersionNode<K, V, T> versionNode = iter.next();
        checkNotNull(versionNode, "versions should not contain nulls");
        return versionNode;
    }

}
