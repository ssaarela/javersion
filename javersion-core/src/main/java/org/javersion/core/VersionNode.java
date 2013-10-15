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

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class VersionNode<K, V, T extends Version<K, V>> implements Comparable<VersionNode<K, V, T>> {

    private class VersionDetails {
        final Set<Long> allRevisions;
        final Map<K, VersionProperty<V>> properties;
        public VersionDetails(Map<K, VersionProperty<V>> properties, Set<Long> allRevisions) {
            this.properties = unmodifiableMap(properties);
            this.allRevisions = unmodifiableSet(allRevisions);
        }
        
    }

    public final T version;
    
    public final Set<VersionNode<K, V, T>> parents;
    
    public final VersionNode<K, V, T> previous;
    
    private volatile SoftReference<VersionDetails> softDetails;

    public VersionNode(VersionNode<K, V, T> previous, T version, Set<VersionNode<K, V, T>> parents) {
        Preconditions.checkNotNull(version, "version");
        Preconditions.checkNotNull(parents, "parents");

        if (previous != null && version.revision <= previous.getRevision()) {
            throw new IllegalVersionOrderException(previous.getRevision(), version.revision);
        }

        this.previous = previous;
        this.version = version;
        this.parents = ImmutableSet.copyOf(parents);
        this.softDetails = softReference(null);
    }
    
    private static <T> SoftReference<T> softReference(T value) {
        return new SoftReference<T>(value);
    }
    
    public Map<K, VersionProperty<V>> getProperties() {
        return getDetails().properties;
    }
    
    public Set<Long> getAllRevisions() {
        return getDetails().allRevisions;
    }

    private VersionDetails getDetails() {
        VersionDetails details = softDetails.get();
        if (details == null) {
            details = buildDetails();
            softDetails = softReference(details);
        }
        return details;
    }
    
    public long getRevision() {
        return version.revision;
    }

    private VersionDetails buildDetails() {
        Map<K, VersionProperty<V>> properties = new LinkedHashMap<>();
        Set<Long> allRevisions = Sets.newHashSet();
        properties.putAll(version.getVersionProperties());
        allRevisions.add(version.revision);
        
        if (!parents.isEmpty()) {
            mergeParents(properties, allRevisions);
        }
        
        return new VersionDetails(properties, allRevisions);
    }

    private void mergeParents(Map<K, VersionProperty<V>> properties, Set<Long> allRevisions) {
        TreeSet<VersionNode<K, V, T>> parentStack = new TreeSet<>();
        parentStack.addAll(parents);
        VersionNode<K, V, T> parent;
        while ((parent = parentStack.pollLast()) != null) {
            for (Map.Entry<K, VersionProperty<V>> entry : parent.getVersionProperties().entrySet()) {
                K key = entry.getKey();
                if (!properties.containsKey(key)) {
                    properties.put(key, entry.getValue());
                }
            }
            allRevisions.add(parent.getRevision());
            parentStack.addAll(parent.parents);
        }
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
