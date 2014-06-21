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

import static com.google.common.collect.Iterables.transform;
import static java.util.Collections.unmodifiableMap;
import static org.javersion.core.AbstractMergeNode.toMergeNodeIterable;

public abstract class VersionGraph<K, V, 
                          T extends Version<K, V>,
                          This extends VersionGraph<K, V, T, This, B>,
                          B extends VersionGraphBuilder<K, V, T, This, B>> 
       extends VersionGraphBase<K, V, T, This, B> {

    public final VersionNode<K, V, T> tip;
    
    protected VersionGraph(VersionGraphBuilder<K, V, T, This, B> builder) {
        super(builder.lock, builder.parentGraph, unmodifiableMap(builder.versionNodes));
        this.tip = builder.tip;
    }
    
    public abstract This commit(T version);
    
    public abstract This commit(Iterable<T> versions);
    
    public final Merge<K, V> merge(Iterable<Long> revisions) {
        return new Merge<K, V>(toMergeNodeIterable(transform(revisions, this)));
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
