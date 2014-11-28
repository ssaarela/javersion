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

import org.javersion.util.Check;
import org.javersion.util.MutableSortedMap;
import org.javersion.util.MutableTreeMap;
import org.javersion.util.PersistentSortedMap;
import org.javersion.util.PersistentTreeMap;

import com.google.common.base.Function;

public abstract class VersionGraphBuilder<K, V, M,
                               G extends VersionGraph<K, V, M, G, B>,
                               B extends VersionGraphBuilder<K, V, M, G, B>> {

    PersistentSortedMap<BranchAndRevision, VersionNode<K, V, M>> heads;

    MutableSortedMap<Revision, VersionNode<K, V, M>> versionNodes;

    private Function<Revision, VersionNode<K, V, M>> revisionToVersionNode = new Function<Revision, VersionNode<K, V, M>>() {
        @Override
        public VersionNode<K, V, M> apply(Revision input) {
            return getVersionNode(Check.notNull(input, "input"));
        }
    };


    protected VersionGraphBuilder() {
        reset();
    }

    protected VersionGraphBuilder(G parentGraph) {
        this.versionNodes = parentGraph.versionNodes.toMutableMap();
        this.heads = parentGraph.getHeads();
    }

    private void reset() {
        this.versionNodes = new MutableTreeMap<>();
        this.heads = PersistentTreeMap.empty();
    }

    public final void add(Version<K, V, M> version) {
        Check.notNull(version, "version");
        if (version.type == VersionType.ROOT) {
            reset();
        }
        Iterable<VersionNode<K, V, M>> parents = revisionsToNodes(version.parentRevisions);
        VersionNode<K, V, M> versionNode = new VersionNode<K, V, M>(version, parents, heads);
        heads = versionNode.heads;
        versionNodes.put(version.revision, versionNode);
    }

    Iterable<VersionNode<K, V, M>> revisionsToNodes(Iterable<Revision> revisions) {
        return transform(revisions, revisionToVersionNode);
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