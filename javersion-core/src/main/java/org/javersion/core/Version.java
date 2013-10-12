package org.javersion.core;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Collections.unmodifiableMap;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class Version<K, V, M> {
    
    public static final String DEFAULT_BRANCH = "default";
    
    private Function<V, VersionProperty<V>> toVersionProperties = new Function<V, VersionProperty<V>>() {

        @Override
        public VersionProperty<V> apply(V input) {
            return new VersionProperty<V>(revision, input);
        }
        
    };

    public final long revision;
    
    public final String branch;
	
    public final Set<Long> parentRevisions;

    public final Map<K, V> properties;

    public final M metadata;
    
    protected Version(Builder<K, V, M> builder) {
        this.revision = builder.revision;
        this.branch = builder.branch;
        this.parentRevisions = ImmutableSet.copyOf(builder.parentRevisions);
        this.properties = unmodifiableMap(newLinkedHashMap(builder.properties));
        this.metadata = builder.metadata;
    }
    
    public Map<K, VersionProperty<V>> getVersionProperties() {
        return Maps.transformValues(properties, toVersionProperties);
    }

    public static class Builder<K, V, M> {

        private static Set<Long> EMPTY_PARENTS = ImmutableSet.of();
        
        public final long revision;
        
        public String branch = DEFAULT_BRANCH;
        
        public Set<Long> parentRevisions = EMPTY_PARENTS;

        public Map<K, V> properties = ImmutableMap.of();

        public M metadata;
        
        public Builder(long revision) {
            this.revision = revision;
        }

        public Builder<K, V, M> branch(String branch) {
            this.branch = branch;
            return this;
        }

        public Builder<K, V, M> parents(Set<Long> parentRevisions) {
            this.parentRevisions = parentRevisions;
            return this;
        }

        public Builder<K, V, M> properties(Map<K, V> properties) {
            this.properties = properties;
            return this;
        }

        public Builder<K, V, M> metadata(M metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Version<K, V, M> build() {
            return new Version<>(this);
        }
        
    }
    
}

