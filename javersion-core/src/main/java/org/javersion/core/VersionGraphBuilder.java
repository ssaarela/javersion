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

import static org.javersion.core.VersionType.RESET;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.javersion.util.Check;
import org.javersion.util.MutableSortedMap;
import org.javersion.util.MutableTreeMap;
import org.javersion.util.PersistentSortedMap;
import org.javersion.util.PersistentTreeMap;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public abstract class VersionGraphBuilder<K, V, M,
                               G extends VersionGraph<K, V, M, G, B>,
                               B extends VersionGraphBuilder<K, V, M, G, B>> {

    PersistentSortedMap<BranchAndRevision, VersionNode<K, V, M>> heads;

    MutableSortedMap<Revision, VersionNode<K, V, M>> versionNodes;

    VersionNode<K, V, M> at;

    private Function<Revision, VersionNode<K, V, M>> revisionToVersionNode = input -> getVersionNode(Check.notNull(input, "input"));


    protected VersionGraphBuilder() {
        this.versionNodes = new MutableTreeMap<>();
        this.heads = PersistentTreeMap.empty();
    }

    protected VersionGraphBuilder(G parentGraph) {
        this.versionNodes = parentGraph.versionNodes.toMutableMap();
        this.heads = parentGraph.getHeads();
    }

    public final B at(VersionNode<K, V, M> at) {
        this.at = at;
        return (B) this;
    }

    public final void add(Version<K, V, M> version) {
        Check.notNull(version, "version");
        MutableSortedMap<BranchAndRevision, VersionNode<K, V, M>> mutableHeads = heads.toMutableMap();
        MergeBuilder<K, V, M> mergeBuilder = new MergeBuilder<K, V, M>();

        if (version.type == RESET) {
            resetVersion(version, mutableHeads);
        } else {
            normalVersion(version, mutableHeads, mergeBuilder);
        }
        mergeBuilder.overwrite(version);
        VersionNode<K, V, M> versionNode = new VersionNode<>(version, mergeBuilder, mutableHeads);
        heads = versionNode.heads;
        versionNodes.put(version.revision, versionNode);
    }

    private void normalVersion(Version<K, V, M> version, MutableSortedMap<BranchAndRevision, VersionNode<K, V, M>> mutableHeads, MergeBuilder<K, V, M> mergeBuilder) {
        Iterable<VersionNode<K, V, M>> parents = toVersionNodes(version.parentRevisions);
        mergeBuilder.mergeAll(parents);
        for (VersionNode<K, V, M> parent : parents) {
            if (parent.version.branch.equals(version.branch)) {
                mutableHeads.remove(new BranchAndRevision(parent));
            }
        }
    }

    private void resetVersion(Version<K, V, M> version, MutableSortedMap<BranchAndRevision, VersionNode<K, V, M>> mutableHeads) {
        if (!mutableHeads.isEmpty()) {
            Iterable<VersionNode<K, V, M>> parents = toVersionNodes(version.parentRevisions);
            for (BranchAndRevision branchAndRevision : new ArrayList<>(mutableHeads.keySet())) {
                for (VersionNode<K, V, M> parent : parents) {
                    if (parent.mergedRevisions.contains(branchAndRevision.revision)) {
                        mutableHeads.remove(branchAndRevision);
                        break;
                    }
                }
            }
        }
    }

    private List<VersionNode<K, V, M>> toVersionNodes(Set<Revision> revisions) {
        return revisions.stream().map(this::getVersionNode).collect(Collectors.toList());
    }

    private VersionNode<K, V, M> getVersionNode(Revision revision) {
        VersionNode<K, V, M> node = versionNodes.get(revision);
        if (node == null) {
            throw new VersionNotFoundException(revision);
        }
        return node;
    }

    public abstract G build();

}