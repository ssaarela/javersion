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

public class BranchMerge<K, V> extends Merge<K, V> {

    private static class Builder<K, V> extends MergeBuilder<K, V> {

        public Builder(Iterable<? extends Merge<K, V>> nodes) {
            super(nodes);
        }

        protected boolean replaceWith(VersionProperty<V> oldValue, VersionProperty<V> newValue) {
            return false;
        }

    }

    private Set<Revision> heads;

    public <T extends Version<K, V>> BranchMerge(Iterable<? extends Merge<K, V>> versions) {
        super(new Builder<K, V>(versions));
    }

    @Override
    public Set<Revision> getMergeHeads() {
        return heads;
    }

    @Override
    protected void setMergeHeads(Set<Revision> heads) {
        this.heads = heads;
    }

}
