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

import com.google.common.collect.Iterables;

public abstract class VersionGraph<K, V, 
                          T extends Version<K, V>,
                          G extends VersionGraph<K, V, T, G, B>,
                          B extends VersionGraphBuilder<K, V, T, G, B>> 
       extends VersionGraphBase<K, V, T, G, B> {

    public final VersionNode<K, V, T> tip;
    
    protected VersionGraph(VersionGraphBuilder<K, V, T, G, B> builder) {
        super(builder.lock, builder.parentGraph, Collections.unmodifiableMap(builder.versionNodes));
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
                      G extends VersionGraph<K, V, T, G, B>,
                      B extends VersionGraphBuilder<K, V, T, G, B>> 
              G build(VersionGraphBuilder<K, V, T, G, B> builder) {
        return builder.build();
    }
    

    protected static <K, 
                      V, 
                      
                      T extends Version<K, V>, 
                      G extends VersionGraph<K, V, T, G, B>,
                      B extends VersionGraphBuilder<K, V, T, G, B>> 
              G build(VersionGraphBuilder<K, V, T, G, B> builder, 
              T version) {
        builder = builder.add(version);
        return builder.build();
    }

    protected static <K, 
                      V, 
                      
                      T extends Version<K, V>, 
                      G extends VersionGraph<K, V, T, G, B>,
                      B extends VersionGraphBuilder<K, V, T, G, B>>
            G build(VersionGraphBuilder<K, V, T, G, B> builder, 
            Iterable<T> versions) {
        for (T version : versions) {
            builder = builder.add(version);
        }
        return builder.build();
    }
    
}
