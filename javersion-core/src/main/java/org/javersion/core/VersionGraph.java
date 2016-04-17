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

import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.javersion.core.BranchAndRevision.max;
import static org.javersion.core.BranchAndRevision.min;
import static org.javersion.util.MapUtils.mapValueFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.concurrent.Immutable;

import org.javersion.util.PersistentMap;
import org.javersion.util.PersistentSortedMap;
import org.javersion.util.PersistentTreeMap;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

@Immutable
public abstract class VersionGraph<K, V, M,
                          This extends VersionGraph<K, V, M, This, B>,
                          B extends VersionGraphBuilder<K, V, M, This, B>>
        implements Function<Revision, VersionNode<K, V, M>> {

    public final PersistentMap<Revision, VersionNode<K, V, M>> versionNodes;

    private final VersionNode<K, V, M> at;

    private final VersionNode<K, V, M> tip;

    public VersionGraph() {
        this(PersistentTreeMap.<Revision, VersionNode<K, V, M>> empty(), null, null);
    }

    protected VersionGraph(VersionGraphBuilder<K, V, M, This, B> builder) {
        this(builder.versionNodes.toPersistentMap(), builder.tip, builder.at);
    }

    private VersionGraph(PersistentMap<Revision, VersionNode<K, V, M>> versionNodes, VersionNode<K, V, M> tip, VersionNode<K, V, M> at) {
        this.versionNodes = versionNodes;
        this.tip = tip;
        this.at = (at != null ? at : tip);
    }

    public final This commit(Version<K, V, M> version) {
        B builder = newBuilder();
        builder.add(version);
        return builder.build();
    }

    public final This commit(Iterable<? extends Version<K, V, M>> versions) {
        B builder = newBuilder();
        for (Version<K, V, M> version : versions) {
            builder.add(version);
        }
        return builder.build();
    }

    protected abstract B newBuilder();

    protected abstract B newEmptyBuilder();

    @Override
    public final VersionNode<K, V, M> apply(Revision input) {
        return input != null ? getVersionNode(input) : null;
    }

    public final VersionNode<K, V, M> getVersionNode(Revision revision) {
        VersionNode<K, V, M> node = versionNodes.get(revision);
        if (node == null) {
            throw new VersionNotFoundException(revision);
        }
        return node;
    }

    public final Merge<K, V, M> mergeBranches(String... branches) {
        return mergeBranches(asList(branches));
    }

    public final Merge<K, V, M> mergeBranches(Iterable<String> branches) {
        List<VersionMerge<K, V, M>> mergedBranches = Lists.newArrayList();
        for (String branch : branches) {
            mergedBranches.add(new VersionMerge<K, V, M>(getHeads(branch)));
        }
        return new BranchMerge<K, V, M>(mergedBranches);
    }

    public final Merge<K, V, M> mergeRevisions(Revision... revisions) {
        return mergeRevisions(asList(revisions));
    }

    public final Merge<K, V, M> mergeRevisions(Iterable<Revision> revisions) {
        return new VersionMerge<K, V, M>(transform(revisions, this));
    }

    public final Iterable<VersionNode<K, V, M>> getHeads(String branch) {
        return transform(getHeads().range(min(branch), max(branch)), mapValueFunction());
    }

    public final VersionNode<K, V, M> getHead(String branch) {
        return getFirst(transform(getHeads().range(min(branch), max(branch), false), mapValueFunction()), null);
    }

    public final PersistentSortedMap<BranchAndRevision, VersionNode<K, V, M>> getHeads() {
        return at != null ? at.heads : PersistentTreeMap.empty();
    }

    public final Iterable<Revision> getHeadRevisions() {
        return getHeads().valueStream().map(VersionNode::getRevision).collect(toList());
    }

    public final Iterable<Revision> getHeadRevisions(String branch) {
        return Iterables.transform(getHeads(branch), VersionNode::getRevision);
    }

    public final This at(Revision revision) {
        return newBuilder().at(getVersionNode(revision)).build();
    }

    public final This atTip() {
        return newBuilder().at(getTip()).build();
    }

    public final boolean isEmpty() {
        return versionNodes.isEmpty();
    }

    public final VersionNode<K, V, M> getTip() {
        return tip;
    }

    public final Set<String> getBranches() {
        return getHeads().keyStream().map(k -> k.branch).collect(toSet());
    }

    /**
     * @return versions in newest first (or reverse topological) order.
     */
    public final Iterable<Version<K, V, M>> getVersions() {
        return Iterables.transform(getVersionNodes(), VersionNode::getVersion);
    }

    /**
     * @return versions in newest first (or reverse topological) order.
     */
    public final Iterable<VersionNode<K, V, M>> getVersionNodes() {
        return new VersionNodeIterable<>(getTip());
    }

    public OptimizedGraph<K, V, M, This, B> optimize(Revision... revisions) {
        return optimize(ImmutableSet.copyOf(revisions));
    }

    public OptimizedGraph<K, V, M, This, B> optimize(Set<Revision> revisions) {
        return optimize(versionNode -> revisions.contains(versionNode.revision));
    }

    public OptimizedGraph<K, V, M, This, B> optimize(Predicate<VersionNode<K, V, M>> keep) {
        final int size = versionNodes.size();
        final Multimap<Revision, Revision> parentToChildren = HashMultimap.create(size, 2);
        final Multimap<Revision, Revision> childToParents = HashMultimap.create(size, 2);
        final Set<Revision> keptRevisions = new HashSet<>(size);
        final List<VersionNode<K, V, M>> keptNodes = new ArrayList<>(size);
        final List<Revision> squashedRevisions = new ArrayList<>(size);

        for (VersionNode<K, V, M> node : getVersionNodes()) {
            Collection<Revision> childRevisions = parentToChildren.get(node.revision).stream()
                    .filter(childRevision ->
                            // Has child that is kept
                            keptRevisions.contains(childRevision) &&
                                    // And is not already a known ancestor
                                    childToParents.get(childRevision).stream()
                                            .noneMatch(parent -> versionNodes.get(parent).contains(node.revision)))
                    .collect(toList());

            if (keep.test(node) || childRevisions.size() > 1) {
                keptRevisions.add(node.revision);
                keptNodes.add(node);
                for (Revision childRevision : childRevisions) {
                    childToParents.put(childRevision, node.revision);
                }
                node.getParentRevisions().forEach(parent -> parentToChildren.put(parent, node.revision));
            } else {
                squashedRevisions.add(node.revision);
                for (Revision childRevision : childRevisions) {
                    node.getParentRevisions().forEach(parent -> parentToChildren.put(parent, childRevision));
                }
            }
        }
        if (keptNodes.isEmpty()) {
            throw new IllegalArgumentException("Keep predicate did't match any version");
        }
        if (squashedRevisions.isEmpty()) {
            return new OptimizedGraph<K, V, M, This, B>(
                    self(),
                    Lists.reverse(keptNodes).stream().map(node -> node.revision).collect(toList()),
                    squashedRevisions);
        };
        return optimizedGraph(keptNodes, childToParents, squashedRevisions);
    }

    @SuppressWarnings("unchecked")
    protected This self() {
        return (This) this;
    }

    private OptimizedGraph<K, V, M, This, B> optimizedGraph(List<VersionNode<K, V, M>> keptNodes,
                                                            Multimap<Revision, Revision> childToParents,
                                                            List<Revision> squashedRevisions) {
        B builder = newEmptyBuilder();
        List<Revision> keptRevisions = new ArrayList<>(keptNodes.size());
        for (int i = keptNodes.size() - 1; i >= 0; i--) {
            VersionNode<K, V, M> node = keptNodes.get(i);
            keptRevisions.add(node.revision);
            Version<K, V, M> version = optimizedVersion(node, childToParents.get(node.revision));
            builder.add(version);
        }
        return new OptimizedGraph<>(builder.build(), unmodifiableList(keptRevisions), unmodifiableList(squashedRevisions));
    }

    private Version<K, V, M> optimizedVersion(VersionNode<K, V, M> node, Collection<Revision> parents) {
        return new Version.Builder<K, V, M>(node.revision)
                .parents(parents)
                .changeset(node.getProperties())
                .type(node.type)
                .branch(node.branch)
                .meta(node.meta)
                .build();
    }

}
