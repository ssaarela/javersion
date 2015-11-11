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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.javersion.util.Check;
import org.javersion.util.MutableSortedMap;
import org.javersion.util.PersistentSortedMap;

import com.google.common.collect.ImmutableSet;

public final class VersionNode<K, V, M> extends Merge<K, V, M> {

    public final VersionNode<K, V, M> previousVersionNode;

    public final Revision revision;

    public final String branch;

    public final Set<Revision> parentRevisions;

    public final VersionType type;

    public final M meta;

    public final PersistentSortedMap<BranchAndRevision, VersionNode<K, V, M>> heads;

    public VersionNode(Version<K, V, M> version,
                       VersionNode<K, V, M> previousVersionNode,
                       MergeBuilder<K, V, M> mergeBuilder,
                       MutableSortedMap<BranchAndRevision, VersionNode<K, V, M>> mutableHeads) {
        super(mergeBuilder);
        Check.notNull(version, "version");
        this.revision = version.revision;
        this.previousVersionNode = previousVersionNode;
        this.branch = version.branch;
        this.parentRevisions = version.parentRevisions;
        this.type = version.type;
        this.meta = version.meta;
        mutableHeads.put(new BranchAndRevision(this), this);
        this.heads = mutableHeads.toPersistentMap();
    }

    public Revision getRevision() {
        return revision;
    }

    public String getBranch() {
        return branch;
    }

    public M getMeta() {
        return meta;
    }

    @Override
    public Set<Revision> getMergeHeads() {
        return ImmutableSet.of(revision);
    }

    @Override
    protected void setMergeHeads(Set<Revision> heads) {}

    public Map<K, V> getChangeset() {
        // TODO: cache result
        final Map<K, V> changeset = new HashMap<>();
        mergedProperties.stream().forEach(entry -> {
            if (entry.getValue().revision.equals(revision)) {
                changeset.put(entry.getKey(), entry.getValue().value);
            }
        });
        return changeset;
    }

    public Version<K, V, M> getVersion() {
        return new Version.Builder<K, V, M>(revision)
                .type(type)
                .branch(branch)
                .meta(meta)
                .parents(parentRevisions)
                .changeset(getChangeset())
                .build();
    }

    public Set<Revision> getParentRevisions() {
        return parentRevisions;
    }

    public String toString() {
        return revision.toString();
    }
}
