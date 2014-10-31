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

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Maps.transformValues;
import static java.util.Collections.unmodifiableMap;
import static org.javersion.util.Check.notNull;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class Version<K, V> {

    public static final String DEFAULT_BRANCH = "default";

    private final Function<V, VersionProperty<V>> toVersionProperties = new Function<V, VersionProperty<V>>() {

        @Override
        public VersionProperty<V> apply(V input) {
            return new VersionProperty<V>(revision, input);
        }

    };

    public final Revision revision;

    public final String branch;

    public final Set<Revision> parentRevisions;

    public final Map<K, V> changeset;

    public final VersionType type;

    protected Version(Builder<K, V, ?> builder) {
        this.revision = builder.revision;
        this.branch = builder.branch;
        this.type = builder.type;
        this.parentRevisions = copyOf(builder.parentRevisions);
        this.changeset = unmodifiableMap(newLinkedHashMap(builder.changeset));
    }

    public Map<K, VersionProperty<V>> getVersionProperties() {
        return transformValues(changeset, toVersionProperties);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("revision", revision)
                .add("branch", branch)
                .add("parentRevisions", parentRevisions)
                .add("type", type)
                .add("changeset", changeset)
                .toString();
    }

    public static class Builder<K, V, B extends Builder<K, V, B>> {

        private static final Set<Revision> EMPTY_PARENTS = ImmutableSet.of();

        protected final Revision revision;

        protected VersionType type = VersionType.NORMAL;

        protected String branch = DEFAULT_BRANCH;

        protected Set<Revision> parentRevisions = EMPTY_PARENTS;

        protected Map<K, V> changeset = ImmutableMap.of();

        public Builder() {
            this(new Revision());
        }

        public Builder(Revision revision) {
            this.revision = revision;
        }

        public B type(VersionType versionType) {
            this.type = notNull(versionType, "type");
            return self();
        }

        public B branch(String branch) {
            this.branch = notNull(branch, "branch");
            return self();
        }

        public B parents(Revision... parentRevisions) {
            return parents(copyOf(parentRevisions));
        }

        public B parents(Set<Revision> parentRevisions) {
            this.parentRevisions = notNull(parentRevisions, "parentRevisions");
            return self();
        }

        public B changeset(Map<K, V> changeset) {
            this.changeset = notNull(changeset, "changeset");
            return self();
        }

        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }

        public Version<K, V> build() {
            return new Version<>(this);
        }

    }

}

