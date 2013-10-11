package org.oliverlib.core;

import java.util.Collections;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class VersionGraph<K, V, M> extends VersionGraphBase<K, V, M> {

    public final VersionNode<K, V, M> tip;
    
    VersionGraph(VersionGraph<K, V, M> parentGraph, Map<Long, VersionNode<K, V, M>> versionNodes, VersionNode<K, V, M> tip) {
        super(parentGraph, Collections.unmodifiableMap(versionNodes));
        this.tip = tip;
    }
    
    public static <K, V, M> VersionGraph<K, V, M> init(Version<K, V, M> version) {
        Builder<K, V, M> builder = new Builder<>();
        builder.add(version);
        return builder.build();
    }
    
    public static <K, V, M> VersionGraph<K, V, M> init(Iterable<Version<K, V, M>> versions) {
        Builder<K, V, M> builder = new Builder<>();
        for (Version<K, V, M> version : versions) {
            builder.add(version);
        }
        return builder.build();
    }
    
    
    public VersionGraph<K, V, M> commit(Version<K, V, M> version) {
        Builder<K, V, M> builder = new Builder<>(this);
        builder.add(version);
        return builder.build();
    }
    
    public VersionGraph<K, V, M> commit(Iterable<Version<K, V, M>> versions) {
        Builder<K, V, M> builder = new Builder<>(this);
        for (Version<K, V, M> version : versions) {
            builder.add(version);
        }
        return builder.build();
    }
    
    
    
    private static class Builder<K, V, M> extends VersionGraphBase<K, V, M> {
        
        private VersionNode<K, V, M> tip;
        
        Builder() {
            this(null);
        }
        Builder(VersionGraph<K, V, M> parentGraph) {
            super(parentGraph, Maps.<Long, VersionNode<K, V, M>>newLinkedHashMap());
            this.tip = parentGraph.tip;
        }
        
        void add(Version<K, V, M> version) {
            checkRevision(tip, version);
            
            tip = new VersionNode<>(version, revisionsToNodes(version.parentRevisions));
            versionNodes.put(version.revision, tip);
        }

        VersionGraph<K, V, M> build() {
            return new VersionGraph<K, V, M>(parentGraph, versionNodes, tip);
        }

        
        private static void checkRevision(VersionNode<?, ?, ?> tip, Version<?, ?, ?> version) {
            Preconditions.checkNotNull(version, "version");
            if (tip != null && version.revision <= tip.getRevision()) {
                throw new IllegalVersionOrderException(tip.getRevision(), version.revision);
            }
        }
    }
    
}
