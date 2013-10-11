package org.oliverlib.core;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class VersionNode<K, V, M> {

    private final Version<K, V, M> version;
    
    private final Set<VersionNode<K, V, M>> parents;
    
    private volatile SoftReference<Map<K, VersionProperty<V>>> softProperties;
    
    private volatile SoftReference<Set<Long>> softRevisions;

    public VersionNode(Version<K, V, M> version, Set<VersionNode<K, V, M>> parents) {
        Preconditions.checkNotNull(version, "version");
        Preconditions.checkNotNull(parents, "parents");
        
        this.version = version;
        this.parents = ImmutableSet.copyOf(parents);
        this.softProperties = softReference(null);
        this.softRevisions = softReference(null);
    }
    
    private static <T> SoftReference<T> softReference(T value) {
        return new SoftReference<T>(value);
    }
    
    public Map<K, VersionProperty<V>> getProperties() {
        Map<K, VersionProperty<V>> properties = softProperties.get();
        if (properties == null) {
            properties = mergeProperties();
            softProperties = softReference(properties);
        }
        return properties;
    }
    
    public Set<Long> getRevisions() {
        Set<Long> revisions = softRevisions.get();
        if (revisions == null) {
            ImmutableSet.Builder<Long> builder = ImmutableSet.builder();
            collectRevisions(builder);
            revisions = builder.build();
            softRevisions = softReference(revisions);
        }
        return revisions;
    }
    
    private void collectRevisions(ImmutableSet.Builder<Long> revisions) {
        revisions.add(getRevision());
        for (VersionNode<K, V, M> parent : parents) {
            revisions.addAll(parent.getRevisions());
        }
    }
        
    public Map<K, VersionProperty<V>> mergeProperties() {
        Map<K, VersionProperty<V>> properties = Maps.newLinkedHashMap();
        
        for (VersionNode<K, V, M> parent : parents) {
            for (Map.Entry<K, VersionProperty<V>> entry : parent.getProperties().entrySet()) {
                K key = entry.getKey();
                VersionProperty<V> nextValue = entry.getValue();
                VersionProperty<V> prevValue = properties.get(key);
                
                if (prevValue == null) {
                    properties.put(key, nextValue);
                } else if (prevValue.revision < nextValue.revision) {
                    properties.put(key, nextValue);
                }
            }
        }
        
        properties.putAll(version.getVersionProperties());
        
        return Collections.unmodifiableMap(properties);
    }

    public long getRevision() {
        return version.revision;
    }
    
}
