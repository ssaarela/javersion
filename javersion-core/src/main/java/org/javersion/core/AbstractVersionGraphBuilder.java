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

public abstract class AbstractVersionGraphBuilder<K,
                               V,
                               T extends Version<K, V>,
                               G extends AbstractVersionGraph<K, V, T, G, B>,
                               B extends AbstractVersionGraphBuilder<K, V, T, G, B>> {

    PersistentSortedMap<BranchAndRevision, VersionNode<K, V, T>> heads;

    MutableSortedMap<Revision, VersionNode<K, V, T>> versionNodes;

    private Function<Revision, VersionNode<K, V, T>> revisionToVersionNode = new Function<Revision, VersionNode<K, V, T>>() {
        @Override
        public VersionNode<K, V, T> apply(Revision input) {
            return getVersionNode(Check.notNull(input, "input"));
        }
    };


    protected AbstractVersionGraphBuilder() {
        reset();
    }

    protected AbstractVersionGraphBuilder(G parentGraph) {
        this.versionNodes = parentGraph.versionNodes.toMutableMap();
        this.heads = parentGraph.getHeads();
    }

    private void reset() {
        this.versionNodes = new MutableTreeMap<>();
        this.heads = PersistentTreeMap.empty();
    }

    public final void add(T version) {
        Check.notNull(version, "version");
        if (version.type == VersionType.ROOT) {
            reset();
        }
        Iterable<VersionNode<K, V, T>> parents = revisionsToNodes(version.parentRevisions);
        VersionNode<K, V, T> versionNode = new VersionNode<K, V, T>(version, parents, heads);
        heads = versionNode.heads;
        versionNodes.put(version.revision, versionNode);
    }

    Iterable<VersionNode<K, V, T>> revisionsToNodes(Iterable<Revision> revisions) {
        return transform(revisions, revisionToVersionNode);
    }

    private VersionNode<K, V, T> getVersionNode(Revision revision) {
        VersionNode<K, V, T> node = versionNodes.get(revision);
        if (node == null) {
            throw new VersionNotFoundException(revision);
        }
        return node;
    }

    protected abstract G build();

}