package org.javersion.core;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public abstract class VersionGraphBase<K, V, 
                                       T extends Version<K, V>,
                                       G extends VersionGraph<K, V, T, G>> 
                implements Function<Long, VersionNode<K, V, T>>{

    public final G parentGraph;
    
    public final Map<Long, VersionNode<K, V, T>> versionNodes;
    
    private final RevisionToVersionNode revisionToVersionNode = new RevisionToVersionNode();

    private class RevisionToVersionNode implements Function<Long, VersionNode<K, V, T>> {

        @Override
        public VersionNode<K, V, T> apply(Long input) {
            return getVersionNode(input);
        }
        
    }

    
    VersionGraphBase(G parentGraph, Map<Long, VersionNode<K, V, T>> versionNodes) {
        this.parentGraph = parentGraph;
        this.versionNodes = Preconditions.checkNotNull(versionNodes, "versionNodes");
    }
    
    Set<VersionNode<K, V, T>> revisionsToNodes(Iterable<Long> revisions) {
        return ImmutableSet.copyOf(Iterables.transform(revisions, revisionToVersionNode));
    }

    
    public VersionNode<K, V, T> getVersionNode(long revision) {
        VersionNode<K, V, T> node = versionNodes.get(revision);
        if (node == null) {
            if (parentGraph != null) {
                return parentGraph.getVersionNode(revision);
            }
            throw new VersionNotFoundException(revision);
        }
        return node;
    }

    @Override
    public VersionNode<K, V, T> apply(Long input) {
        return input != null ? getVersionNode(input) : null;
    }

}
