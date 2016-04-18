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
import static com.google.common.collect.Lists.reverse;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.javersion.core.BranchAndRevision.max;
import static org.javersion.core.BranchAndRevision.min;
import static org.javersion.util.MapUtils.mapValueFunction;

import java.util.*;
import java.util.function.Predicate;

import javax.annotation.concurrent.Immutable;

import org.javersion.util.PersistentMap;
import org.javersion.util.PersistentSortedMap;
import org.javersion.util.PersistentTreeMap;

import com.google.common.base.Function;
import com.google.common.collect.*;

@Immutable
public abstract class AbstractVersionGraph<K, V, M,
                          This extends AbstractVersionGraph<K, V, M, This, B>,
                          B extends VersionGraphBuilder<K, V, M, This, B>>
        implements Function<Revision, VersionNode<K, V, M>>, VersionGraph<K, V, M, This> {

    final PersistentMap<Revision, VersionNode<K, V, M>> versionNodes;

    private final VersionNode<K, V, M> at;

    private final VersionNode<K, V, M> tip;

    public AbstractVersionGraph() {
        this(PersistentTreeMap.<Revision, VersionNode<K, V, M>> empty(), null, null);
    }

    protected AbstractVersionGraph(VersionGraphBuilder<K, V, M, This, B> builder) {
        this(builder.versionNodes.toPersistentMap(), builder.tip, builder.at);
    }

    private AbstractVersionGraph(PersistentMap<Revision, VersionNode<K, V, M>> versionNodes, VersionNode<K, V, M> tip, VersionNode<K, V, M> at) {
        this.versionNodes = versionNodes;
        this.tip = tip;
        this.at = (at != null ? at : tip);
    }

    @Override
    public final This commit(Version<K, V, M> version) {
        B builder = newBuilder();
        builder.add(version);
        return builder.build();
    }

    @Override
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

    @Override
    public final VersionNode<K, V, M> getVersionNode(Revision revision) {
        VersionNode<K, V, M> node = versionNodes.get(revision);
        if (node == null) {
            throw new VersionNotFoundException(revision);
        }
        return node;
    }

    @Override
    public final Merge<K, V, M> mergeBranches(String... branches) {
        return mergeBranches(asList(branches));
    }

    @Override
    public final Merge<K, V, M> mergeBranches(Iterable<String> branches) {
        List<VersionMerge<K, V, M>> mergedBranches = Lists.newArrayList();
        for (String branch : branches) {
            mergedBranches.add(new VersionMerge<K, V, M>(getHeads(branch)));
        }
        return new BranchMerge<K, V, M>(mergedBranches);
    }

    @Override
    public final Merge<K, V, M> mergeRevisions(Revision... revisions) {
        return mergeRevisions(asList(revisions));
    }

    @Override
    public final Merge<K, V, M> mergeRevisions(Iterable<Revision> revisions) {
        return new VersionMerge<K, V, M>(transform(revisions, this));
    }

    @Override
    public final Iterable<VersionNode<K, V, M>> getHeads(String branch) {
        return transform(getHeads().range(min(branch), max(branch)), mapValueFunction());
    }

    @Override
    public final VersionNode<K, V, M> getHead(String branch) {
        return getFirst(transform(getHeads().range(min(branch), max(branch), false), mapValueFunction()), null);
    }

    @Override
    public final PersistentSortedMap<BranchAndRevision, VersionNode<K, V, M>> getHeads() {
        return at != null ? at.heads : PersistentTreeMap.empty();
    }

    @Override
    public final Iterable<Revision> getHeadRevisions() {
        return getHeads().valueStream().map(VersionNode::getRevision).collect(toList());
    }

    @Override
    public final Iterable<Revision> getHeadRevisions(String branch) {
        return Iterables.transform(getHeads(branch), VersionNode::getRevision);
    }

    @Override
    public final This at(Revision revision) {
        return newBuilder().at(getVersionNode(revision)).build();
    }

    @Override
    public final This atTip() {
        return newBuilder().at(getTip()).build();
    }

    @Override
    public final boolean isEmpty() {
        return versionNodes.isEmpty();
    }

    @Override
    public int size() {
        return versionNodes.size();
    }

    @Override
    public boolean contains(Revision revision) {
        return versionNodes.containsKey(revision);
    }

    @Override
    public final VersionNode<K, V, M> getTip() {
        return tip;
    }

    @Override
    public final Set<String> getBranches() {
        return getHeads().keyStream().map(k -> k.branch).collect(toSet());
    }

    /**
     * @return versions in newest first (or reverse topological) order.
     */
    @Override
    public final Iterable<Version<K, V, M>> getVersions() {
        return Iterables.transform(getVersionNodes(), VersionNode::getVersion);
    }

    /**
     * @return versions in newest first (or reverse topological) order.
     */
    @Override
    public final Iterable<VersionNode<K, V, M>> getVersionNodes() {
        return new VersionNodeIterable<>(getTip());
    }

    public OptimizedGraph<K, V, M, This> optimize(Revision... revisions) {
        return optimize(ImmutableSet.copyOf(revisions));
    }

    @Override
    public OptimizedGraph<K, V, M, This> optimize(Set<Revision> revisions) {
        return optimize(versionNode -> revisions.contains(versionNode.revision));
    }

    @Override
    public OptimizedGraph<K, V, M, This> optimize(Predicate<VersionNode<K, V, M>> keep) {
        if (isEmpty()) {
            return new OptimizedGraph<>(self(), emptyList(), emptyList());
        }
        return new Optimizer().optimize(keep);
    }

    @SuppressWarnings("unchecked")
    protected This self() {
        return (This) this;
    }


    private class Optimizer {

        private final int size = versionNodes.size();

        /**
         * Child candidates: a version is always kept if it has more than one child that is also kept (LCA).
         */
        private final Multimap<Revision, Revision> parentToChildRevisions = HashMultimap.create(size, 2);
        /**
         * Optimized child-to-parent relations.
         */
        private final Multimap<Revision, Revision> childToParentRevisions = HashMultimap.create(size, 2);
        /**
         * HashSet of kept revisions for quick check.
         */
        private final Set<Revision> keptRevisions = new HashSet<>(size);
        /**
         * Newest first list of kept nodes.
         */
        private final List<VersionNode<K, V, M>> keptNodes = new ArrayList<>(size);
        private final List<Revision> squashedRevisions = new ArrayList<>(size);
        private final Revision tipRevision = getTip().revision;

        public OptimizedGraph<K, V, M, This> optimize(Predicate<VersionNode<K, V, M>> keepPredicate) {
            for (VersionNode<K, V, M> node : getVersionNodes()) {
                List<Revision> keptChildRevisions = parentToChildRevisions.get(node.revision).stream()
                        .filter(childRevision -> isRequiredChild(node.revision, childRevision))
                        .collect(toList());

                if (keepPredicate.test(node) || keptChildRevisions.size() > 1 || tipRevision.equals(node.revision)) {
                    keep(node, keptChildRevisions);
                } else {
                    squash(node, keptChildRevisions);
                }
            }
            if (squashedRevisions.isEmpty()) {
                return unmodifiableOptimizedGraph(
                        self(),
                        Lists.transform(reverse(keptNodes), VersionNode::getRevision),
                        squashedRevisions);
            }
            return toOptimizedGraph();
        }

        private boolean isRequiredChild(Revision revision, Revision childRevision) {
            // Has a child that is kept
            return keptRevisions.contains(childRevision) &&
                    // And is not ancestor
                    childToParentRevisions.get(childRevision).stream()
                            .noneMatch(parentRevision -> versionNodes.get(parentRevision).contains(revision));
        }

        private void keep(VersionNode<K, V, M> node, Collection<Revision> keptChildRevisions) {
            keptRevisions.add(node.revision);
            keptNodes.add(node);
            for (Revision childRevision : keptChildRevisions) {
                childToParentRevisions.put(childRevision, node.revision);
            }
            node.getParentRevisions().forEach(parent -> parentToChildRevisions.put(parent, node.revision));
        }

        private void squash(VersionNode<K, V, M> node, Collection<Revision> keptChildRevisions) {
            squashedRevisions.add(node.revision);
            for (Revision childRevision : keptChildRevisions) {
                node.getParentRevisions().forEach(parent -> parentToChildRevisions.put(parent, childRevision));
            }
        }

        private OptimizedGraph<K, V, M, This> toOptimizedGraph() {
            B builder = newEmptyBuilder();
            List<Revision> keptRevisions = new ArrayList<>(keptNodes.size());
            for (int i = keptNodes.size() - 1; i >= 0; i--) {
                VersionNode<K, V, M> node = keptNodes.get(i);
                keptRevisions.add(node.revision);
                Version<K, V, M> version = optimizedVersion(node, childToParentRevisions.get(node.revision));
                builder.add(version);
            }
            return unmodifiableOptimizedGraph(builder.build(), keptRevisions, squashedRevisions);
        }

        private OptimizedGraph<K, V, M, This> unmodifiableOptimizedGraph(This graph, List<Revision> keptRevisions, List<Revision> squashedRevisions) {
            return new OptimizedGraph<>(graph, unmodifiableList(keptRevisions), unmodifiableList(squashedRevisions));
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

}
