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

import java.util.Set;

import org.javersion.util.Check;

import com.google.common.collect.Maps;

public abstract class VersionGraphBuilder<K, 
                               V, 
                               T extends Version<K, V>, 
                               G extends VersionGraph<K, V, T, G, B>,
                               B extends VersionGraphBuilder<K, V, T, G, B>>
          extends VersionGraphBase<K, V, T, G, B> {
    
    VersionNode<K, V, T> tip;
    
    protected VersionGraphBuilder() {
        this(null, null);
    }
    
    protected VersionGraphBuilder(G parentGraph) {
        this(null, Check.notNull(parentGraph, "parentGraph"));
        this.tip = parentGraph.tip;
    }
    
    protected VersionGraphBuilder(Lock lock) {
        this(Check.notNull(lock, "lock"), null);
    }
    
    private VersionGraphBuilder(Lock lock, G parentGraph) {
        super(lock, parentGraph, Maps.<Long, VersionNode<K, V, T>>newLinkedHashMap());
    }
    
    final B add(T version) {
        Check.notNull(version, "version");
        if (version.type == VersionType.ROOT) {
            return newBuilder(this.lock).addInternal(version);
        } else {
            return addInternal(version);
        }
    }
    
    final B addInternal(T version) {
        Set<VersionNode<K, V, T>> parentsDescending = revisionsToNodes(version.parentRevisions);
        tip = new VersionNode<K, V, T>(tip, version, parentsDescending);
        versionNodes.put(version.revision, tip);
        return self();
    }
    
    protected abstract B self();
    
    protected abstract B newBuilder(Lock lock);

    protected abstract G build();

}