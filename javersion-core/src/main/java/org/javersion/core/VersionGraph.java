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

import java.util.Collections;
import java.util.Set;

import org.javersion.util.Check;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public abstract class VersionGraph<K, V, 
                          T extends Version<K, V>,
                          G extends VersionGraph<K, V, T, G>> 
       extends VersionGraphBase<K, V, T, G> {

    public final VersionNode<K, V, T> tip;
    
    protected VersionGraph(Builder<K, V, T, G> builder) {
        super(builder.parentGraph, Collections.unmodifiableMap(builder.versionNodes));
        this.tip = builder.tip;
    }
    
    public abstract G commit(T version);
    
    public abstract G commit(Iterable<T> versions);
    
    public final Merge<K, V> merge(Set<Long> revisions) {
        return new Merge<K, V>(Iterables.transform(revisions, this));
    }
    

    protected static <K, 
                      V, 
                      
                      T extends Version<K, V>, 
                      G extends VersionGraph<K, V, T, G>> 
              G build(Builder<K, V, T, G> builder) {
        return builder.build();
    }
    

    protected static <K, 
                      V, 
                      
                      T extends Version<K, V>, 
                      G extends VersionGraph<K, V, T, G>> 
              G build(Builder<K, V, T, G> builder, T version) {
        builder.add(version);
        return builder.build();
    }

    protected static <K, 
                      V, 
                      
                      T extends Version<K, V>, 
                      G extends VersionGraph<K, V, T, G>>
            G build(Builder<K, V, T, G> builder, Iterable<T> versions) {
        for (T version : versions) {
            builder.add(version);
        }
        return builder.build();
    }
    
    public static abstract class Builder<K, 
                                   V, 
                                   
                                   T extends Version<K, V>, 
                                   G extends VersionGraph<K, V, T, G>>
              extends VersionGraphBase<K, V, T, G> {
        
        private VersionNode<K, V, T> tip;
        
        protected Builder() {
            this(null);
        }
        protected Builder(G parentGraph) {
            super(parentGraph, Maps.<Long, VersionNode<K, V, T>>newLinkedHashMap());
            if (parentGraph != null) {
                this.tip = parentGraph.tip;
            }
        }
        
        final void add(T version) {
            Check.notNull(version, "version");
            Set<VersionNode<K, V, T>> parentsDescending = revisionsToNodes(version.parentRevisions);
            tip = new VersionNode<K, V, T>(tip, version, parentsDescending);
            versionNodes.put(version.revision, tip);
        }

        protected abstract G build();

    }
    
}
