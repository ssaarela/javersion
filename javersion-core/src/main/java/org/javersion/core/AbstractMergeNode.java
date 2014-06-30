/*
 * Copyright 2014 Samppa Saarela
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

import org.javersion.util.PersistentHashMap;
import org.javersion.util.PersistentHashSet;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

public abstract class AbstractMergeNode<K, V> {
	
	protected static <K, V, T extends Version<K, V>> Iterable<AbstractMergeNode<K, V>> toMergeNodes(Iterable<VersionNode<K, V, T>> nodes) {
		return Iterables.transform(nodes, new Function<VersionNode<K, V, T>, AbstractMergeNode<K, V>>() {
			@Override
			public AbstractMergeNode<K, V> apply(VersionNode<K, V, T> input) {
				return (AbstractMergeNode<K, V>) input;
			}
		});
	}
    
    public final PersistentHashMap<K, VersionProperty<V>> allProperties;
    
    public final PersistentHashSet<Long> allRevisions;
    
    public final Multimap<K, VersionProperty<V>> conflicts;
	
    public AbstractMergeNode(Iterable<AbstractMergeNode<K, V>> nodes) {
    	this(nodes, null);
    }
    
    protected AbstractMergeNode(Iterable<AbstractMergeNode<K, V>> parents, Version<K, V> newVersion) {
    	MergeHelper<K, V> mergeHelper = new MergeHelper<>();
    	for (AbstractMergeNode<K, V> node : parents) {
    		mergeHelper.merge(node);
    	}
    	if (newVersion != null) {
    		mergeHelper.overwrite(newVersion);
    	}
    	this.allProperties = mergeHelper.getMergedProperties();
    	this.allRevisions = mergeHelper.getMergedRevisions();
    	this.conflicts = mergeHelper.getConflicts();
    	setHeads(mergeHelper.getHeads());
    }
    
    public abstract Set<Long> getHeads();

    protected abstract void setHeads(Set<Long> heads);
    
}
