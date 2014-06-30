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
import static org.javersion.core.AbstractMergeNode.toMergeNodeIterable;

import org.javersion.util.PersistentSortedMap;
import org.javersion.util.PersistentTreeMap;

import com.google.common.base.Function;

public abstract class AbstractVersionGraph<K, V, 
                          T extends Version<K, V>,
                          This extends AbstractVersionGraph<K, V, T, This, B>,
                          B extends AbstractVersionGraphBuilder<K, V, T, This, B>> 
		implements Function<Long, VersionNode<K, V, T>> {

    public final PersistentSortedMap<Long, VersionNode<K, V, T>> versionNodes;

    public final PersistentSortedMap<BranchAndRevision, VersionNode<K, V, T>> heads;
    
    public AbstractVersionGraph() {
    	this(PersistentTreeMap.<Long, VersionNode<K, V, T>> empty(), 
    			PersistentTreeMap.<BranchAndRevision, VersionNode<K, V, T>> empty());
    }
    
    protected AbstractVersionGraph(AbstractVersionGraphBuilder<K, V, T, This, B> builder) {
    	this(builder.versionNodes.toPersistentMap(), builder.heads.toPersistentMap());
    }
    
    protected AbstractVersionGraph(PersistentSortedMap<Long, VersionNode<K, V, T>> versionNodes, PersistentSortedMap<BranchAndRevision, VersionNode<K, V, T>> leaves) {
		this.versionNodes = versionNodes;
		this.heads = leaves;
	}

	public final This commit(T version) {
    	B builder = newBuilder();
    	builder.add(version);
    	return builder.build();
    }
    
    public final This commit(Iterable<T> versions) {
    	B builder = newBuilder();
        for (T version : versions) {
            builder.add(version);
        }
        return builder.build();
    }
    
    protected abstract B newBuilder();

    @Override
    public VersionNode<K, V, T> apply(Long input) {
        return input != null ? getVersionNode(input) : null;
    }
    
    public VersionNode<K, V, T> getVersionNode(long revision) {
        VersionNode<K, V, T> node = versionNodes.get(revision);
        if (node == null) {
            throw new VersionNotFoundException(revision);
        }
        return node;
    }

    public final Merge<K, V> merge(Iterable<Long> revisions) {
        return new Merge<K, V>(toMergeNodeIterable(transform(revisions, this)));
    }
    
}
