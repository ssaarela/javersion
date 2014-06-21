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

import com.google.common.collect.ImmutableSet;

public final class VersionNode<K, V, T extends Version<K, V>> extends AbstractMergeNode<K, V> implements Comparable<VersionNode<K, V, T>> {
    
    public final T version;

    public final Set<VersionNode<K, V, T>> parents;

    public final VersionNode<K, V, T> previous;

    public VersionNode(VersionNode<K, V, T> previous, T version, Iterable<VersionNode<K, V, T>> parents) {
    	super(toMergeNodeIterable(parents), version);

        if (previous != null && version.revision <= previous.getRevision()) {
            throw new IllegalVersionOrderException(previous.getRevision(), version.revision);
        }

        this.previous = previous;
        this.version = version;
        this.parents = ImmutableSet.copyOf(parents);
    }
    
    public long getRevision() {
        return version.revision;
    }
    
    public Map<K, VersionProperty<V>> getVersionProperties() {
        return version.getVersionProperties();
    }

	@Override
	public Set<Long> getHeads() {
		return ImmutableSet.of(version.revision);
	}

    @Override
    public int compareTo(VersionNode<K, V, T> o) {
        return Long.compare(getRevision(), o.getRevision());
    }
    
    @Override
    public String toString() {
        return version.toString();
    }

	@Override
	protected void setHeads(Set<Long> heads) {}

}
