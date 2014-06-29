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

import org.javersion.util.Check;
import org.javersion.util.MutableTreeMap;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public abstract class AbstractVersionGraphBuilder<K, 
                               V, 
                               T extends Version<K, V>, 
                               G extends AbstractVersionGraph<K, V, T, G, B>,
                               B extends AbstractVersionGraphBuilder<K, V, T, G, B>> {
    
    VersionNode<K, V, T> tip;
    
    MutableTreeMap<Long, VersionNode<K, V, T>> versions;

    private Function<Long, VersionNode<K, V, T>> revisionToVersionNode = new Function<Long, VersionNode<K, V, T>>() {
        @Override
        public VersionNode<K, V, T> apply(Long input) {
            return getVersionNode(Check.notNull(input, "input"));
        }
    };

    
    protected AbstractVersionGraphBuilder() {
    	this.versions = new MutableTreeMap<>();
    }
    
    protected AbstractVersionGraphBuilder(G parentGraph) {
    	this.versions = parentGraph.versionNodes.toMutableMap();
        this.tip = parentGraph.tip;
    }
    
    public final void add(T version) {
        Check.notNull(version, "version");
        if (version.type == VersionType.ROOT) {
        	this.versions = new MutableTreeMap<>();
        	this.tip = null;
        }
        Iterable<VersionNode<K, V, T>> parentsDescending = revisionsToNodes(version.parentRevisions);
        tip = new VersionNode<K, V, T>(tip, version, parentsDescending);
        versions.put(version.revision, tip);
    }
    
    Iterable<VersionNode<K, V, T>> revisionsToNodes(Iterable<Long> revisions) {
        return Iterables.transform(revisions, revisionToVersionNode);
    }

    private VersionNode<K, V, T> getVersionNode(long revision) {
        VersionNode<K, V, T> node = versions.get(revision);
        if (node == null) {
            throw new VersionNotFoundException(revision);
        }
        return node;
    }

    protected abstract G build();

}