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

import org.javersion.util.AbstractTrieMap.Entry;
import org.javersion.util.Check;
import org.javersion.util.Merger;
import org.javersion.util.PersistentMap;

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
                PersistentMap<K, VersionProperty<V>> mergedProperties = versionNode.allProperties;

                Set<Long> heads = Sets.newHashSet(versionNode.getRevision());
                
                final ImmutableMultimap.Builder<K, VersionProperty<V>> conflicts = ImmutableMultimap.builder();
                
                final Set<Long> mergedRevisions = Sets.newHashSet(versionNode.allRevisions);

                Merger<Entry<K, VersionProperty<V>>> merger = new Merger<Entry<K, VersionProperty<V>>>() {

                    @Override
                    public void insert(Entry<K, VersionProperty<V>> newEntry) {
                    }

                    @Override
                    public Entry<K, VersionProperty<V>> merge(
                            Entry<K, VersionProperty<V>> oldEntry,
                            Entry<K, VersionProperty<V>> newEntry) {
                        // newEntry derives from a common ancestor?
                        if (mergedRevisions.contains(newEntry.getValue().revision)) {
                            return oldEntry;
                        } 
                        // Conflicting value?
                        else if (!equal(oldEntry.getValue().value, newEntry.getValue().value)) {
                            conflicts.put(newEntry);
                            return oldEntry;
                        } 
                        // New property
                        else {
                            return newEntry;
                        }
                    }

                    @Override
                    public void delete(Entry<K, VersionProperty<V>> oldEntry) {
                        throw new UnsupportedOperationException();
                    }
                };
                do {
                    versionNode = next(iter);

                    // Version already merged?
                    if (!mergedRevisions.contains(versionNode.getRevision())) {
                        mergedProperties = mergedProperties.mergeAll(versionNode.allProperties, merger);
                        mergedRevisions.addAll(versionNode.allRevisions.asSet());
                        heads.removeAll(versionNode.allRevisions.asSet());
                        heads.add(versionNode.getRevision());
                    }
                } while (iter.hasNext());

                this.mergedProperties = mergedProperties.asMap();
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
