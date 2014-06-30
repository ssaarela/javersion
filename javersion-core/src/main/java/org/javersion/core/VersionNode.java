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

public final class VersionNode<K, V, T extends Version<K, V>> extends AbstractMergeNode<K, V> {
    
    public final T version;

    public final Set<VersionNode<K, V, T>> parents;

    public VersionNode(T version, Iterable<VersionNode<K, V, T>> parents) {
    	super(toMergeNodeIterable(parents), version);

        this.version = version;
        this.parents = ImmutableSet.copyOf(parents);
    }
    
    public long getRevision() {
        return version.revision;
    }
    
    public String getBranch() {
    	return version.branch;
    }
    
    public Map<K, VersionProperty<V>> getVersionProperties() {
        return version.getVersionProperties();
    }

	@Override
	public Set<Long> getHeads() {
		return ImmutableSet.of(version.revision);
	}
	
    @Override
	public int hashCode() {
		return Long.hashCode(version.revision);
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}
    
    @Override
    public String toString() {
        return version.toString();
    }

	@Override
	protected void setHeads(Set<Long> heads) {}

}
