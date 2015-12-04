/*
 * Copyright 2015 Samppa Saarela
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

import static java.util.Collections.unmodifiableList;
import static java.util.function.Function.identity;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@NotThreadSafe
public class OptimizedGraphBuilder<K, V, M> {

    private Set<OptimizedVersionBuilder<K, V, M>> heads = new LinkedHashSet<>();

    private List<OptimizedVersionBuilder<K, V, M>> optimizedVersions = new ArrayList<>();

    private List<Revision> squashedRevisions = new ArrayList<>();

    public OptimizedGraphBuilder(VersionGraph<K, V, M, ?, ?> versionGraph, Revision... keepRevisions) {
        this(versionGraph, ImmutableSet.copyOf(keepRevisions));
    }

    public OptimizedGraphBuilder(VersionGraph<K, V, M, ?, ?> versionGraph, Set<Revision> keepRevisions) {
        this(versionGraph, versionNode -> keepRevisions.contains(versionNode.revision));
    }

    public OptimizedGraphBuilder(VersionGraph<K, V, M, ?, ?> versionGraph, Predicate<VersionNode<K, V, M>> keep) {
        // optimizedVersions and heads are in reverse topological order (newest first)
        for (VersionNode<K, V, M> versionNode : versionGraph.getVersionNodes()) {
            List<OptimizedVersionBuilder<K, V, M>> children = findChildren(heads, versionNode.revision);
            Set<Revision> directChildRevisions = directChildrenRevisions(children);
            if (directChildRevisions.size() > 1 || keep.test(versionNode)) {
                keep(versionNode, children, directChildRevisions);
            } else {
                squash(versionNode, children);
            }
        }
        optimizedVersions.addAll(heads);
        heads = null;
        // Revert optimizedVersions to topological order
        optimizedVersions = unmodifiableList(Lists.reverse(optimizedVersions));
    }

    private void keep(VersionNode<K, V, M> versionNode, List<OptimizedVersionBuilder<K, V, M>> children, Set<Revision> directChildRevisions) {
        for (OptimizedVersionBuilder<K, V, M> child : children) {
            if (directChildRevisions.contains(child.getRevision())) {
                child.addParent(versionNode);
            } else {
                child.removeOldParent(versionNode);
            }
            handleResolvedChild(child);
        }
        handleNewOptimizedVersion(new OptimizedVersionBuilder<>(versionNode));
    }

    private void squash(VersionNode<K, V, M> versionNode, List<OptimizedVersionBuilder<K, V, M>> children) {
        children.forEach(child -> {
            child.squashParent(versionNode);
            handleResolvedChild(child);
        });
        squashedRevisions.add(versionNode.revision);
    }

    public Iterable<Version<K, V, M>> getOptimizedVersions() {
        return optimizedVersions.stream()
                .map(optimizedVersion -> optimizedVersion.build(identity()))
                .collect(Collectors.toList());
    }

    public List<Revision> getSquashedRevisions() {
        return squashedRevisions;
    }

    public List<Revision> getKeptRevisions() {
        return Lists.transform(optimizedVersions, optimizedVersion -> optimizedVersion.getRevision());
    }

    private void handleNewOptimizedVersion(OptimizedVersionBuilder<K, V, M> optimizedVersion) {
        if (optimizedVersion.isResolved()) {
            optimizedVersions.add(optimizedVersion);
        } else {
            heads.add(optimizedVersion);
        }
    }

    private void handleResolvedChild(OptimizedVersionBuilder<K, V, M> childVersion) {
        if (childVersion.isResolved()) {
            optimizedVersions.add(childVersion);
            heads.remove(childVersion);
        }
    }

    private List<OptimizedVersionBuilder<K, V, M>> findChildren(
            Collection<OptimizedVersionBuilder<K, V, M>> optimizedVersions, Revision parentRevision) {
        return optimizedVersions.stream()
                .filter(childCandidate -> childCandidate.hasDirectParent(parentRevision))
                .collect(Collectors.toList());
    }

    private Set<Revision> directChildrenRevisions(List<OptimizedVersionBuilder<K, V, M>> children) {
        Set<Revision> result = Sets.newHashSetWithExpectedSize(children.size());
        for (int i = children.size()-1; i >= 0; i--) {
            boolean redundant = false;
            OptimizedVersionBuilder<K, V, M> child = children.get(i);
            for (int j = i+1; j < children.size(); j++) {
                if (child.hasParent(children.get(j).getRevision())) {
                    redundant = true;
                    break;
                }
            }
            if (!redundant) {
                result.add(child.getRevision());
            }
        }
        return result;
    }

    private static class OptimizedVersionBuilder<K, V, M> {

        private Set<Revision> parentRevisions;

        private VersionNode<K, V, M> versionNode;

        private Set<VersionNode<K, V, M>> newParents = new HashSet<>();

        OptimizedVersionBuilder(VersionNode<K, V, M> versionNode) {
            this.versionNode = versionNode;
            this.parentRevisions = new HashSet<>(versionNode.parentRevisions);
        }

        public boolean hasDirectParent(Revision revision) {
            return parentRevisions.contains(revision);
        }

        public boolean hasParent(Revision revision) {
            return versionNode.getParentRevisions().contains(revision);
        }

        public Revision getRevision() {
            return versionNode.revision;
        }

        public void addParent(VersionNode<K, V, M> newParent) {
            removeOldParent(newParent);
            newParents.add(newParent);
        }

        public void removeOldParent(VersionNode<K, V, M> parent) {
            parentRevisions.remove(parent.revision);
        }

        public void squashParent(VersionNode<K, V, M> parent) {
            removeOldParent(parent);
            parentRevisions.addAll(parent.parentRevisions);
        }

        public boolean isResolved() {
            return parentRevisions.isEmpty();
        }

        public Version<K, V, M> build(Function<Revision, Revision> idMapper) {
            return new Version.Builder<K, V, M>(idMapper.apply(versionNode.getRevision()))
                    .branch(versionNode.getBranch())
                    .meta(versionNode.getMeta())
                    .parents(getParentRevisions(idMapper))
                    .changeset(versionNode.getProperties())
                    .build();
        }

        private Set<Revision> getParentRevisions(Function<Revision, Revision> idMapper) {
            return newParents.stream().map(VersionNode::getRevision).map(idMapper).collect(Collectors.toSet());
        }

        public String toString() {
            return versionNode.toString();
        }
    }
}
