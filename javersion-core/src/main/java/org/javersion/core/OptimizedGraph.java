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

import static java.util.function.Function.identity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;


public class OptimizedGraph<K, V, M> {

    private final List<OptimizedVersionBuilder<K, V, M>> optimizedVersions;

    public OptimizedGraph(VersionGraph<K, V, M, ?, ?> versionGraph, Predicate<VersionNode<K, V, M>> keep) {
        ImmutableList.Builder<OptimizedVersionBuilder<K, V, M>> results = ImmutableList.builder();
        List<OptimizedVersionBuilder<K, V, M>> heads = new ArrayList<>();
        for (VersionNode<K, V, M> versionNode : versionGraph.getVersionNodes()) {
            List<OptimizedVersionBuilder<K, V, M>> childVersions = findChildRevisions(heads, versionNode.revision);
            if (childVersions.size() > 1 || keep.test(versionNode)) {
                for (OptimizedVersionBuilder<K, V, M> childVersion : childVersions) {
                    childVersion.addParent(versionNode);
                    if (childVersion.isResolved()) {
                        results.add(childVersion);
                        heads.remove(childVersion);
                    }
                }
                OptimizedVersionBuilder<K, V, M> optimizedVersion = new OptimizedVersionBuilder<>(versionNode);
                if (optimizedVersion.isResolved()) {
                    results.add(optimizedVersion);
                } else {
                    heads.add(optimizedVersion);
                }
            } else if (!childVersions.isEmpty()){
                childVersions.get(0).squashParent(versionNode);
            }
        }
        results.addAll(heads);
        optimizedVersions = results.build().reverse();
    }

    private List<OptimizedVersionBuilder<K, V, M>> findChildRevisions (
            Collection<OptimizedVersionBuilder < K, V, M >> optimizedVersions, Revision parentRevision) {
        return optimizedVersions.stream()
                .filter(childCandidate -> childCandidate.hasParent(parentRevision))
                .collect(Collectors.toList());
    }

    public Iterable<Version<K, V, M>> getOptimizedVersions() {
        return optimizedVersions.stream()
                .map(optimizedVersion -> optimizedVersion.build(identity()))
                .collect(Collectors.toList());
    }

    private static class OptimizedVersionBuilder<K, V, M> {

        private Set<Revision> parentRevisions;

        private VersionNode<K, V, M> versionNode;

        private Set<VersionNode<K, V, M>> newParents = new HashSet<>();

        OptimizedVersionBuilder(VersionNode<K, V, M> versionNode) {
            this.versionNode = versionNode;
            this.parentRevisions = new HashSet<>(versionNode.parentRevisions);
        }

        public boolean hasParent(Revision revision) {
            return parentRevisions.contains(revision);
        }

        public Revision getRevision() {
            return versionNode.revision;
        }

        public void addParent(VersionNode<K, V, M> newParent) {
            newParents.add(newParent);
            parentRevisions.remove(newParent.revision);
        }

        public void squashParent(VersionNode<K, V, M> parent) {
            parentRevisions.remove(parent.revision);
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
    }
}
