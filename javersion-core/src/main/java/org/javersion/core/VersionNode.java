package org.javersion.core;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Collections.unmodifiableMap;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

public class VersionNode<K, V, T extends Version<K, V>> {

    public final T version;
    
    public final Set<VersionNode<K, V, T>> parents;
    
    public final VersionNode<K, V, T> previous;
    
    private volatile SoftReference<Map<K, VersionProperty<V>>> softProperties;
    
    private volatile SoftReference<Set<Long>> softRevisions;

    public VersionNode(VersionNode<K, V, T> previous, T version, Set<VersionNode<K, V, T>> parents) {
        Preconditions.checkNotNull(version, "version");
        Preconditions.checkNotNull(parents, "parents");

        if (previous != null && version.revision <= previous.getRevision()) {
            throw new IllegalVersionOrderException(previous.getRevision(), version.revision);
        }

        this.previous = previous;
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

    public long getRevision() {
        return version.revision;
    }

    
    private void collectRevisions(ImmutableSet.Builder<Long> revisions) {
        revisions.add(getRevision());
        for (VersionNode<K, V, T> parent : parents) {
            revisions.addAll(parent.getRevisions());
        }
    }
        
    private Map<K, VersionProperty<V>> mergeProperties() {
        Map<K, VersionProperty<V>> properties = newLinkedHashMap();
        
        for (VersionNode<K, V, T> parent : parents) {
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
        
        return unmodifiableMap(properties);
    }
    
}
