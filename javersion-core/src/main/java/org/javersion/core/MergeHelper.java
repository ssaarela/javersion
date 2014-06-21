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

import static com.google.common.base.Objects.equal;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.javersion.util.Check;
import org.javersion.util.Merger;
import org.javersion.util.MergerAdapter;
import org.javersion.util.MutableHashMap;
import org.javersion.util.MutableHashSet;
import org.javersion.util.PersistentHashMap;
import org.javersion.util.PersistentHashSet;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class MergeHelper<K, V> {
	
	private boolean first = true;
	
	private boolean locked = false;
	
	private MutableHashMap<K, VersionProperty<V>> mergedProperties;

	private MutableHashSet<Long> mergedRevisions;
	
	private ArrayListMultimap<K, VersionProperty<V>> conflicts = ArrayListMultimap.create();
	
	private Set<Long> heads = Sets.newHashSet();
	
	public PersistentHashMap<K, VersionProperty<V>> getMergedProperties() {
		ensureInitialized();
        locked = true;
		return mergedProperties.toPersistentMap();
	}
	
	public PersistentHashSet<Long> getMergedRevisions() {
		ensureInitialized();
        locked = true;
		return mergedRevisions.toPersistentSet();
	}
	
	public Multimap<K, VersionProperty<V>> getConflicts() {
		ensureInitialized();
        locked = true;
		return ImmutableMultimap.copyOf(conflicts);
	}
	
	public Set<Long> getHeads() {
		ensureInitialized();
        locked = true;
		return ImmutableSet.copyOf(heads);
	}
	
	public final void overwrite(Version<K, V> version) {
		Check.notNull(version, "version");
		ensureNotLocked();
		ensureInitialized();

		for (Map.Entry<K, V> entry : version.properties.entrySet()) {
        	mergedProperties.put(entry.getKey(), new VersionProperty<V>(version.revision, entry.getValue()));
        	conflicts.removeAll(entry.getKey());
    	}
        heads.removeAll(version.parentRevisions);
        mergedRevisions.conjAll(version.parentRevisions);
        mergedRevisions.conj(version.revision);
        heads.add(version.revision);
        locked = true;
	}

	private void ensureNotLocked() {
		Check.that(!locked, "MergeHelper is locked");
	}
	
	public final void merge(final AbstractMergeNode<K, V> node) {
		Check.notNull(node, "node");
		ensureNotLocked();
		
		if (first) {
			first = false;
			mergedProperties = node.allProperties.toMutableMap();
			mergedRevisions = node.allRevisions.toMutableSet();
		} else {
			Merger<Entry<K, VersionProperty<V>>> merger = new MergerAdapter<Entry<K, VersionProperty<V>>>() {
		        @Override
		        public boolean merge(
		                Entry<K, VersionProperty<V>> oldEntry,
		                Entry<K, VersionProperty<V>> newEntry) {
		        	VersionProperty<V> oldValue = oldEntry.getValue();
		        	VersionProperty<V> newValue = newEntry.getValue();
		        	
		            // newValue from common ancestor?
		            if (mergedRevisions.contains(newValue.revision)) {
		                return false;
		            }
		            // oldValue from common ancestor? 
		            else if (node.allRevisions.contains(oldValue.revision)) {
		            	return true;
		            }
		            // Conflicting value?
		            else if (!equal(oldValue.value, newValue.value)) {
		            	K key = newEntry.getKey();
		            	boolean retainNewer = replaceWith(oldValue, newValue);
	                	if (retainNewer) {
	                		conflicts.put(key, oldValue);
	                	} else {
	                		conflicts.put(key, newValue);
	                	}
		                return retainNewer;
		            } 
		            // Newer value
		            else {
		                return true;
		            }
		
		        }
		    };

            mergedProperties.mergeAll(node.allProperties, merger);
            mergedRevisions.conjAll(node.allRevisions);
            heads.removeAll(node.allRevisions.asSet());
		}
		conflicts.putAll(node.conflicts);
        heads.addAll(node.getHeads());
	}
	
	protected boolean replaceWith(VersionProperty<V> oldValue, VersionProperty<V> newValue) {
		return oldValue.revision < newValue.revision;
	}
	
	private void ensureInitialized() {
		if (first) {
			first = false;
			mergedProperties = new MutableHashMap<>();
			mergedRevisions = new MutableHashSet<>();
		}
	}

}
