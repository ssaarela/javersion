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

import java.util.Set;

import org.javersion.util.Check;
import org.javersion.util.MutableSortedMap;
import org.javersion.util.PersistentSortedMap;

import com.google.common.collect.ImmutableSet;

public final class VersionNode<K, V, M> extends Merge<K, V, M> {

    public final Version<K, V, M> version;

    public final PersistentSortedMap<BranchAndRevision, VersionNode<K, V, M>> heads;

    public VersionNode(Version<K, V, M> version,
                       MergeBuilder<K, V, M> mergeBuilder,
                       MutableSortedMap<BranchAndRevision, VersionNode<K, V, M>> mutableHeads) {
        super(mergeBuilder);
        this.version = Check.notNull(version, "version");
        mutableHeads.put(new BranchAndRevision(this), this);
        this.heads = mutableHeads.toPersistentMap();
    }

    public Revision getRevision() {
        return version.revision;
    }

    public String getBranch() {
        return version.branch;
    }

    @Override
    public Set<Revision> getMergeHeads() {
        return ImmutableSet.of(version.revision);
    }

    @Override
    protected void setMergeHeads(Set<Revision> heads) {}

}
