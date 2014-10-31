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

import java.util.Map;
import java.util.Set;

import org.javersion.util.MutableSortedMap;
import org.javersion.util.PersistentSortedMap;

import com.google.common.collect.ImmutableSet;

public final class VersionNode<K, V, T extends Version<K, V>> extends Merge<K, V> {

    public final T version;

    public final PersistentSortedMap<BranchAndRevision, VersionNode<K, V, T>> heads;

    public VersionNode(T version, Iterable<VersionNode<K, V, T>> parents, PersistentSortedMap<BranchAndRevision, VersionNode<K, V, T>> heads) {
        super(new MergeBuilder<K, V>(toMergeNodes(parents)).overwrite(version));

        this.version = version;

        MutableSortedMap<BranchAndRevision, VersionNode<K, V, T>> mutableHeads = heads.toMutableMap();
        for (VersionNode<K, V, T> parent : parents) {
            if (parent.version.branch.equals(version.branch)) {
                mutableHeads.remove(new BranchAndRevision(parent));
            }
        }
        mutableHeads.put(new BranchAndRevision(this), this);
        this.heads = mutableHeads.toPersistentMap();
    }

    public Revision getRevision() {
        return version.revision;
    }

    public String getBranch() {
        return version.branch;
    }

    public Map<K, VersionProperty<V>> getVersionProperties() {
        return version.getVersionProperties();
    }

    @Override
    public Set<Revision> getMergeHeads() {
        return ImmutableSet.of(version.revision);
    }

    @Override
    public int hashCode() {
        return version.revision.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public String toString() {
        return version.toString();
    }

    @Override
    protected void setMergeHeads(Set<Revision> heads) {}

}
