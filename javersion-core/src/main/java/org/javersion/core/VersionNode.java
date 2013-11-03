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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.javersion.util.Check;
import org.javersion.util.PersistentSet;
import org.javersion.util.PersistentMap;

import com.google.common.collect.ImmutableSet;

public final class VersionNode<K, V, T extends Version<K, V>> implements Comparable<VersionNode<K, V, T>> {

    public final T version;
    
    public final Set<VersionNode<K, V, T>> parents;
    
    public final VersionNode<K, V, T> previous;

    public final PersistentMap<K, VersionProperty<V>> allProperties;
    
    public final PersistentSet<Long> allRevisions;

    public VersionNode(VersionNode<K, V, T> previous, T version, Set<VersionNode<K, V, T>> parents) {
        Check.notNull(version, "version");
        Check.notNull(parents, "parents");

        if (previous != null && version.revision <= previous.getRevision()) {
            throw new IllegalVersionOrderException(previous.getRevision(), version.revision);
        }

        this.previous = previous;
        this.version = version;
        this.parents = ImmutableSet.copyOf(parents);
        
        Iterator<VersionNode<K, V, T>> iter = parents.iterator();
        
        if (!iter.hasNext()) {
            this.allRevisions = new PersistentSet<Long>().conj(version.revision);
            this.allProperties = PersistentMap.copyOf(version.getVersionProperties());
        } else {
            VersionNode<K, V, T> parent = iter.next();
            PersistentSet<Long> revisions = parent.allRevisions;
            PersistentMap<K, VersionProperty<V>> properties = parent.allProperties;
            
            while (iter.hasNext()) {
                parent = iter.next();
                revisions = revisions.conjAll(parent.allRevisions);

                for (Map.Entry<K, VersionProperty<V>> entry : parent.allProperties) {
                    K key = entry.getKey();
                    VersionProperty<V> value = entry.getValue();
                    VersionProperty<V> prevValue = properties.get(key);
                    if (prevValue == null || prevValue.revision < value.revision) {
                        properties = properties.assoc(entry);
                    }
                }
            }
            this.allRevisions = revisions.conj(version.revision);
            this.allProperties = properties.assocAll(version.getVersionProperties());
        }
        
        
    }
    
    public long getRevision() {
        return version.revision;
    }
    
    public Map<K, VersionProperty<V>> getVersionProperties() {
        return version.getVersionProperties();
    }

    @Override
    public int compareTo(VersionNode<K, V, T> o) {
        return Long.compare(getRevision(), o.getRevision());
    }
    
    @Override
    public String toString() {
        return version.toString();
    }
}
