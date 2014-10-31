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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.javersion.util.Check;
import org.javersion.util.Merger;
import org.javersion.util.MergerAdapter;
import org.javersion.util.MutableHashMap;
import org.javersion.util.MutableHashSet;
import org.javersion.util.PersistentHashMap;
import org.javersion.util.PersistentHashSet;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class MergeBuilder<K, V> {

    private boolean first = true;

    private boolean locked = false;

    private MutableHashMap<K, VersionProperty<V>> mergedProperties = new MutableHashMap<>();

    private MutableHashSet<Revision> mergedRevisions = new MutableHashSet<>();

    private final ArrayListMultimap<K, VersionProperty<V>> conflicts = ArrayListMultimap.create();

    private final Set<Revision> heads = Sets.newHashSet();

    public MergeBuilder() {
    }

    public MergeBuilder(Iterable<? extends Merge<K, V>> nodes) {
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

    public final MergeBuilder<K, V> overwrite(Version<K, V> version) {
        Check.notNull(version, "version");
        ensureNotLocked();
        ensureInitialized();

        for (Map.Entry<K, V> entry : version.changeset.entrySet()) {
            mergedProperties.put(entry.getKey(), new VersionProperty<V>(version.revision, entry.getValue()));
            conflicts.removeAll(entry.getKey());
        }
        heads.removeAll(version.parentRevisions);
        mergedRevisions.addAllFrom(version.parentRevisions);
        mergedRevisions.add(version.revision);
        heads.add(version.revision);
        return this;
    }

    private void ensureNotLocked() {
        Check.that(!locked, "MergeHelper is locked");
    }

    public final MergeBuilder<K, V> mergeAll(final Iterable<? extends Merge<K, V>> nodes) {
        for (Merge<K, V> node : nodes) {
            merge(node);
        }
        return this;
    }

    public final MergeBuilder<K, V> merge(final Merge<K, V> node) {
        Check.notNull(node, "node");
        ensureNotLocked();

        if (first) {
            firstVersion(node);
        } else {
            nextVersion(node);
        }
        conflicts.putAll(node.conflicts);
        heads.addAll(node.getMergeHeads());
        return this;
    }

    private void nextVersion(final Merge<K, V> node) {
        Merger<Entry<K, VersionProperty<V>>> merger = new MergerAdapter<Entry<K, VersionProperty<V>>>() {
            @Override
            public boolean merge(
                    Entry<K, VersionProperty<V>> oldEntry,
                    Entry<K, VersionProperty<V>> newEntry) {
                VersionProperty<V> oldValue = oldEntry.getValue();
                VersionProperty<V> newValue = newEntry.getValue();

                // newValue from common ancestor?
                if (mergedRevisions.contains(newValue.revision)) {
                    return false;
                }
                // oldValue from common ancestor?
                else if (node.mergedRevisions.contains(oldValue.revision)) {
                    return true;
                }
                // Conflicting value?
                else if (!equal(oldValue.value, newValue.value)) {
                    K key = newEntry.getKey();
                    boolean retainNewer = replaceWith(oldValue, newValue);
                    if (retainNewer) {
                        conflicts.put(key, oldValue);
                    } else {
                        conflicts.put(key, newValue);
                    }
                    return retainNewer;
                }
                // Newer value
                else {
                    return true;
                }

            }
        };

        mergedProperties.mergeAll(node.mergedProperties, merger);
        mergedRevisions.addAllFrom(node.mergedRevisions);
        heads.removeAll(node.mergedRevisions.asSet());
    }

    private void firstVersion(final Merge<K, V> node) {
        first = false;
        mergedProperties = node.mergedProperties.toMutableMap();
        mergedRevisions = node.mergedRevisions.toMutableSet();
    }

    protected boolean replaceWith(VersionProperty<V> oldValue, VersionProperty<V> newValue) {
        return oldValue.revision.compareTo(newValue.revision) < 0;
    }

    private void ensureInitialized() {
        if (first) {
            first = false;
            mergedProperties = new MutableHashMap<>();
            mergedRevisions = new MutableHashSet<>();
        }
    }

}
