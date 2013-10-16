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

import java.util.Map;
import java.util.Set;

import org.javersion.util.Check;

import com.google.common.base.Function;
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
        this.versionNodes = Check.notNull(versionNodes, "versionNodes");
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
