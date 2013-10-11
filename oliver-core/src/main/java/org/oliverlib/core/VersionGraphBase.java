package org.oliverlib.core;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public abstract class VersionGraphBase<K, V, M> {

    public final VersionGraph<K, V, M> parentGraph;
    
    public final Map<Long, VersionNode<K, V, M>> versionNodes;
    
    private final RevisionToVersionNode revisionToVersionNode = new RevisionToVersionNode();

    private class RevisionToVersionNode implements Function<Long, VersionNode<K, V, M>> {

        @Override
        public VersionNode<K, V, M> apply(Long input) {
            return getVersionNode(input);
        }
        
    }

    
    VersionGraphBase(VersionGraph<K, V, M> parentGraph, Map<Long, VersionNode<K, V, M>> versionNodes) {
        this.parentGraph = parentGraph;
        this.versionNodes = Preconditions.checkNotNull(versionNodes, "versionNodes");
    }
    
    Set<VersionNode<K, V, M>> revisionsToNodes(Iterable<Long> revisions) {
        return ImmutableSet.copyOf(Iterables.transform(revisions, revisionToVersionNode));
    }

    
    public VersionNode<K, V, M> getVersionNode(long revision) {
        VersionNode<K, V, M> node = versionNodes.get(revision);
        if (node == null) {
            if (parentGraph != null) {
                return parentGraph.getVersionNode(revision);
            }
            throw new VersionNotFoundException(revision);
        }
        return node;
    }

}
