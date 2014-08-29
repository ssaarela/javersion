/*
 * Copyright 2013 Samppa Saarela
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

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Maps.transformValues;
import static java.util.Collections.unmodifiableMap;
import static org.javersion.util.Check.notNull;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class Version<K, V> {
    
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
    
    public final VersionType type;
    
    protected Version(Builder<K, V, ?> builder) {
        this.revision = builder.revision;
        this.branch = builder.branch;
        this.type = builder.type;
        this.parentRevisions = copyOf(builder.parentRevisions);
        this.properties = unmodifiableMap(newLinkedHashMap(builder.properties));
    }
    
    public Map<K, VersionProperty<V>> getVersionProperties() {
        return transformValues(properties, toVersionProperties);
    }
    
    public String toString() {
        return "#" + revision;
    }

    public static class Builder<K, V, B extends Builder<K, K, B>> {

        private static Set<Long> EMPTY_PARENTS = ImmutableSet.of();
        
        private final long revision;
        
        private VersionType type = VersionType.NORMAL;

        private String branch = DEFAULT_BRANCH;
        
        private Set<Long> parentRevisions = EMPTY_PARENTS;

        private Map<K, V> properties = ImmutableMap.of();

        public Builder(long revision) {
            this.revision = revision;
        }

        public B type(VersionType versionType) {
            this.type = notNull(versionType, "type");
            return self();
        }

        public B branch(String branch) {
            this.branch = notNull(branch, "branch");
            return self();
        }

        public B parents(Set<Long> parentRevisions) {
            this.parentRevisions = notNull(parentRevisions, "parentRevisions");
            return self();
        }

        public B properties(Map<K, V> properties) {
            this.properties = notNull(properties, "properties");
            return self();
        }
        
        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }
        
        public Version<K, V> build() {
            return new Version<>(this);
        }
        
    }
    
}

