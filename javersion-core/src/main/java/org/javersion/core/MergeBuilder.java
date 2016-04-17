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

import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.javersion.util.*;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

@NotThreadSafe
public class MergeBuilder<K, V, M> {

    private boolean first = true;

    private boolean locked = false;

    private MutableHashMap<K, VersionProperty<V>> mergedProperties = new MutableHashMap<>();

    private MutableHashSet<Revision> mergedRevisions = new MutableHashSet<>();

    private final ArrayListMultimap<K, VersionProperty<V>> conflicts = ArrayListMultimap.create();

    private final Set<Revision> heads = Sets.newHashSet();

    public MergeBuilder() {
    }

    public MergeBuilder(Iterable<? extends Merge<K, V, M>> nodes) {
        mergeAll(nodes);
    }

    public PersistentHashMap<K, VersionProperty<V>> getMergedProperties() {
        ensureInitialized();
        locked = true;
        return mergedProperties.toPersistentMap();
    }

    public PersistentHashSet<Revision> getMergedRevisions() {
        ensureInitialized();
        locked = true;
        return mergedRevisions.toPersistentSet();
    }

    public Multimap<K, VersionProperty<V>> getConflicts() {
        ensureInitialized();
        locked = true;
        return ImmutableMultimap.copyOf(conflicts);
    }

    public Set<Revision> getHeads() {
        ensureInitialized();
        locked = true;
        return ImmutableSet.copyOf(heads);
    }

    public final MergeBuilder<K, V, M> overwrite(Version<K, V, M> version) {
        Check.notNull(version, "version");
        ensureNotLocked();
        ensureInitialized();

        final Merger<Entry<K, VersionProperty<V>>> overwriteMerger = new Merger<Entry<K, VersionProperty<V>>>() {
            @Override
            public boolean merge(Entry<K, VersionProperty<V>> prevEntry, Entry<K, VersionProperty<V>> nextEntry) {
                // Keep prevValue if there's no change
                return !Objects.equals(prevEntry.getValue().value, nextEntry.getValue().value);
            }

            @Override
            public boolean insert(Entry<K, VersionProperty<V>> newEntry) {
                return newEntry.getValue().value != null;
            }
        };

        version.changeset.forEach((path, value) -> {
            VersionProperty<V> versionProperty = new VersionProperty<V>(version.revision, value);
            mergedProperties.merge(path, versionProperty, overwriteMerger);
            conflicts.removeAll(path);
        });
        heads.removeAll(version.parentRevisions);
        heads.add(version.revision);
        mergedRevisions.add(version.revision);
        return this;
    }

    private void ensureNotLocked() {
        Check.that(!locked, "MergeHelper is locked");
    }

    public final MergeBuilder<K, V, M> mergeAll(final Iterable<? extends Merge<K, V, M>> nodes) {
        for (Merge<K, V, M> node : nodes) {
            merge(node);
        }
        return this;
    }

    private final MergeBuilder<K, V, M> merge(final Merge<K, V, M> node) {
        Check.notNull(node, "node");
        ensureNotLocked();

        if (first) {
            firstVersion(node);
        } else {
            nextVersion(node);
        }
        conflicts.putAll(node.conflicts);
        return this;
    }

    private void nextVersion(final Merge<K, V, M> node) {
        heads.removeAll(node.mergedRevisions.asSet());
        boolean newHeads = false;
        for (Revision mergeHead : node.getMergeHeads()) {
            if (!mergedRevisions.contains(mergeHead)) {
                newHeads = true;
                heads.add(mergeHead);
            }
        }
        if (newHeads) {
            handleMerge(node);
        }
    }

    private void handleMerge(final Merge<K, V, M> node) {
        final Merger<Entry<K, VersionProperty<V>>> merger = new Merger<Entry<K, VersionProperty<V>>>() {
            @Override
            public boolean merge(Entry<K, VersionProperty<V>> oldEntry, Entry<K, VersionProperty<V>> newEntry) {
                VersionProperty<V> prevValue = oldEntry.getValue();
                VersionProperty<V> nextValue = newEntry.getValue();

                // Keep prevValue if nextValue is from common ancestor
                if (mergedRevisions.contains(nextValue.revision)) {
                    return false;
                }
                // Keep nextValue if prevValue is from common ancestor
                else if (node.mergedRevisions.contains(prevValue.revision)) {
                    return true;
                }
                // Keep older value if there's no change
                else if (Objects.equals(prevValue.value, nextValue.value)) {
                    return nextValue.isBefore(prevValue);
                }
                // For conflicting value, keep newer and report the other as a conflict
                else {
                    return handleMergeConflict(newEntry.getKey(), prevValue, nextValue);
                }

            }
        };
        mergedProperties.mergeAll(node.mergedProperties, merger);
        mergedRevisions.addAllFrom(node.mergedRevisions);
    }

    private boolean handleMergeConflict(K key, VersionProperty<V> prevValue, VersionProperty<V> nextValue) {
        boolean resolveToNext = shouldResolveToNext(prevValue, nextValue);
        if (resolveToNext) {
            conflicts.put(key, prevValue);
        } else {
            conflicts.put(key, nextValue);
        }
        return resolveToNext;
    }

    private void firstVersion(final Merge<K, V, M> node) {
        first = false;
        mergedProperties = node.mergedProperties.toMutableMap();
        mergedRevisions = node.mergedRevisions.toMutableSet();
        heads.addAll(node.getMergeHeads());
    }

    protected boolean shouldResolveToNext(VersionProperty<V> prevValue, VersionProperty<V> nextValue) {
        return nextValue.isAfter(prevValue);
    }

    private void ensureInitialized() {
        if (first) {
            first = false;
            mergedProperties = new MutableHashMap<>();
            mergedRevisions = new MutableHashSet<>();
        }
    }

}
