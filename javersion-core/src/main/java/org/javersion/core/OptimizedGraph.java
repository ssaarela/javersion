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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;


public class OptimizedGraph<K, V, M> {

    private final List<OptimizedVersionBuilder<K, V, M>> optimizedVersions;

    public OptimizedGraph(VersionGraph<K, V, M, ?, ?> versionGraph, Iterable<Revision> revisions) {
        Map<Revision, OptimizedVersionBuilder<K, V, M>> optimizedVersionsByRevision = new LinkedHashMap<>();
        for (Revision revision : revisions) {
            optimizedVersionsByRevision.put(revision, new OptimizedVersionBuilder<>(versionGraph.getVersionNode(revision)));
        }
        for (VersionNode<K, V, M> versionNode : versionGraph.getVersionNodes()) {
            if (!optimizedVersionsByRevision.containsKey(versionNode.revision)) {
                Set<Revision> childRevisions = getChildRevisions(optimizedVersionsByRevision.values(), versionNode.revision);
                if (childRevisions.size() > 1) {
                    for (Revision childRevision : childRevisions) {
                        OptimizedVersionBuilder<K, V, M> versionBuilder = optimizedVersionsByRevision.get(childRevision);
                        versionBuilder.addParent(versionNode);
                    }
                    optimizedVersionsByRevision.put(versionNode.revision, new OptimizedVersionBuilder<>(versionNode));
                }
            }
        }
        optimizedVersions = ImmutableList.copyOf(optimizedVersionsByRevision.values()).reverse();
    }

    private Set<Revision> getChildRevisions(Collection<OptimizedVersionBuilder<K, V, M>> optimizedVersions, Revision currentRevision) {
        return optimizedVersions.stream()
                .filter(childCandidate -> childCandidate.requiresParent(currentRevision))
                .map(OptimizedVersionBuilder::getRevision)
                .collect(Collectors.toSet());
    }

    public Iterable<Version<K, V, M>> getOptimizedVersions() {
        return optimizedVersions.stream()
                .map(optimizedVersion -> optimizedVersion.build(identity()))
                .collect(Collectors.toList());
    }

    private static class OptimizedVersionBuilder<K, V, M> {

        private VersionNode<K, V, M> versionNode;

        private Set<VersionNode<K, V, M>> newParents = new HashSet<>();

        OptimizedVersionBuilder(VersionNode<K, V, M> versionNode) {
            this.versionNode = versionNode;
        }

        boolean requiresParent(Revision revision) {
            return versionNode.mergedRevisions.contains(revision)
                    && newParents.stream().noneMatch(parent -> parent.mergedRevisions.contains(revision));
        }

        public Revision getRevision() {
            return versionNode.revision;
        }

        public void addParent(VersionNode<K, V, M> parentCandidate) {
            for (VersionNode<K, V, M> parent : newParents) {
                // parentCandidate is inherited
                if (parent.mergedRevisions.contains(parentCandidate)) {
                    return;
                }
            }
            newParents.add(parentCandidate);
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
