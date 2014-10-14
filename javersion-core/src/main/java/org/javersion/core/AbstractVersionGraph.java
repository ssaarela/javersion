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

import static com.google.common.collect.Iterables.transform;
import static org.javersion.core.BranchAndRevision.max;
import static org.javersion.core.BranchAndRevision.min;

import java.util.List;

import org.javersion.util.MapUtils;
import org.javersion.util.PersistentSortedMap;
import org.javersion.util.PersistentTreeMap;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public abstract class AbstractVersionGraph<K, V,
                          T extends Version<K, V>,
                          This extends AbstractVersionGraph<K, V, T, This, B>,
                          B extends AbstractVersionGraphBuilder<K, V, T, This, B>>
        implements Function<Revision, VersionNode<K, V, T>> {

    public final PersistentSortedMap<Revision, VersionNode<K, V, T>> versionNodes;

    public AbstractVersionGraph() {
        this(PersistentTreeMap.<Revision, VersionNode<K, V, T>> empty());
    }

    protected AbstractVersionGraph(AbstractVersionGraphBuilder<K, V, T, This, B> builder) {
        this(builder.versionNodes.toPersistentMap());
    }

    protected AbstractVersionGraph(PersistentSortedMap<Revision, VersionNode<K, V, T>> versionNodes) {
        this.versionNodes = versionNodes;
    }

    public final This commit(T version) {
        B builder = newBuilder();
        builder.add(version);
        return builder.build();
    }

    public final This commit(Iterable<T> versions) {
        B builder = newBuilder();
        for (T version : versions) {
            builder.add(version);
        }
        return builder.build();
    }

    protected abstract B newBuilder();

    @Override
    public VersionNode<K, V, T> apply(Revision input) {
        return input != null ? getVersionNode(input) : null;
    }

    public VersionNode<K, V, T> getVersionNode(Revision revision) {
        VersionNode<K, V, T> node = versionNodes.get(revision);
        if (node == null) {
            throw new VersionNotFoundException(revision);
        }
        return node;
    }

    public final Merge<K, V> mergeBranches(Iterable<String> branches) {
        List<VersionMerge<K, V>> mergedBranches = Lists.newArrayList();
        for (String branch : branches) {
            mergedBranches.add(new VersionMerge<>(getHeads(branch)));
        }
        return new BranchMerge<K, V>(mergedBranches);
    }

    public final Merge<K, V> mergeRevisions(Iterable<Revision> revisions) {
        return new VersionMerge<>(transform(revisions, this));
    }

    public Iterable<VersionNode<K, V, T>> getHeads(String branch) {
        return transform(getHeads().range(min(branch), max(branch)),
                MapUtils.<VersionNode<K, V, T>>mapValueFunction());
    }

    public PersistentSortedMap<BranchAndRevision, VersionNode<K, V, T>> getHeads() {
        if (versionNodes.isEmpty()) {
            return PersistentTreeMap.empty();
        }
        return getTip().heads;
    }

    public boolean isEmpty() {
        return versionNodes.isEmpty();
    }

    public VersionNode<K, V, T> getTip() {
        return versionNodes.getLastEntry().getValue();
    }
}
