package org.javersion.core;

import static com.google.common.collect.Lists.reverse;
import static java.util.function.Function.identity;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class OptimizedGraph<K, V, M> {

    private List<OptimizedVersionBuilder<K, V, M>> optimizedVersions;

    public OptimizedGraph(VersionGraph<K, V, M, ?, ?> versionGraph, Iterable<Revision> revisions) {
        Map<Revision, OptimizedVersionBuilder<K, V, M>> optimizedVersionsByRevision = new LinkedHashMap<>();
        for (Revision revision : revisions) {
            optimizedVersionsByRevision.put(revision, new OptimizedVersionBuilder<>(versionGraph.getVersionNode(revision)));
        }
        VersionNode<K, V, M> current = versionGraph.getTip();
        while (current != null) {
            if (!optimizedVersionsByRevision.containsKey(current.getRevision())) {
                Set<Revision> childRevisions = new HashSet<>();
                for (OptimizedVersionBuilder<K, V, M> versionBuilder : optimizedVersionsByRevision.values()) {
                    if (versionBuilder.requiresParent(current.revision)) {
                        childRevisions.add(versionBuilder.getRevision());
                    }
                }
                if (childRevisions.size() > 1) {
                    for (Revision childRevision : childRevisions) {
                        OptimizedVersionBuilder<K, V, M> versionBuilder = optimizedVersionsByRevision.get(childRevision);
                        versionBuilder.addParent(current);
                    }
                    optimizedVersionsByRevision.put(current.revision, new OptimizedVersionBuilder<>(current));
                }
            }
            current = (current.previousRevision != null ? versionGraph.getVersionNode(current.previousRevision) : null);
        }
        optimizedVersions = reverse(new ArrayList<>(optimizedVersionsByRevision.values()));
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
