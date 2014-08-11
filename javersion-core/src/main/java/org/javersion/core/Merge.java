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

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Maps.filterValues;

import java.util.Map;
import java.util.Set;

import org.javersion.util.Check;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public final class Merge<K, V> {
    
    public final Map<K, VersionProperty<V>> mergedProperties;

    public final Multimap<K, VersionProperty<V>> conflicts;

    public final Set<Long> heads;
    
    public final Function<VersionProperty<V>, V> getVersionPropertyValue = new Function<VersionProperty<V>, V>() {

        @Override
        public V apply(VersionProperty<V> input) {
            return input != null ? input.value : null;
        }
        
    };

    public Merge(Iterable<? extends AbstractMergeNode<K, V>> versions) {
        Check.notNull(versions, "versions");

        MergeHelper<K, V> mergeHelper = new MergeHelper<K, V>() {
            protected boolean replaceWith(VersionProperty<V> oldValue, VersionProperty<V> newValue) {
                return false;
            }
        };
        
        for (AbstractMergeNode<K, V> node : versions) {
            mergeHelper.merge(node);
        }
        
        this.mergedProperties = mergeHelper.getMergedProperties().asMap();
        this.heads = mergeHelper.getHeads();
        this.conflicts = mergeHelper.getConflicts();
    }

    public Map<K, V> getProperties() {
        return filterValues(Maps.transformValues(mergedProperties, getVersionPropertyValue), notNull());
    }

}
