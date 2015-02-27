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
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class Version<K, V, M> {

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

    public final M meta;

    protected Version(Builder<K, V, M, ?> builder) {
        this.revision = builder.revision;
        this.branch = builder.branch;
        this.type = builder.type;
        this.parentRevisions = copyOf(builder.parentRevisions);
        this.changeset = unmodifiableMap(newLinkedHashMap(builder.changeset));
        this.meta = builder.meta;
    }

    public Map<K, VersionProperty<V>> getVersionProperties() {
        return transformValues(changeset, toVersionProperties);
    }

    public int hashCode() {
        int hashCode = revision.hashCode();
        hashCode = 31*hashCode + (branch==null ? 0 : branch.hashCode());
        hashCode = 31*hashCode + (parentRevisions==null ? 0 : parentRevisions.hashCode());
        hashCode = 31*hashCode + (changeset==null ? 0 : changeset.hashCode());
        hashCode = 31*hashCode + (type==null ? 0 : type.hashCode());
        hashCode = 31*hashCode + (meta==null ? 0 : meta.hashCode());
        return hashCode;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Version) {
            Version<?, ?, ?> other = (Version<?, ?, ?>) obj;
            return Objects.equals(this.revision, other.revision)
                    && Objects.equals(this.branch, other.branch)
                    && Objects.equals(this.parentRevisions, other.parentRevisions)
                    && Objects.equals(this.changeset, other.changeset)
                    && Objects.equals(this.type, other.type)
                    && Objects.equals(this.meta, other.meta);
        } else {
            return false;
        }
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

    public static class Builder<K, V, M, This extends Builder<K, V, M, This>> {

        private static final Set<Revision> EMPTY_PARENTS = ImmutableSet.of();

        protected Revision revision;

        protected VersionType type = VersionType.NORMAL;

        protected String branch = DEFAULT_BRANCH;

        protected Set<Revision> parentRevisions = EMPTY_PARENTS;

        protected Map<K, V> changeset = ImmutableMap.of();

        protected M meta;

        public Builder() {
            this(new Revision());
        }

        public Builder(Revision revision) {
            this.revision = revision;
        }

        public This type(VersionType versionType) {
            this.type = notNull(versionType, "type");
            return self();
        }

        public This branch(String branch) {
            this.branch = notNull(branch, "branch");
            return self();
        }

        public This parents(Revision... parentRevisions) {
            return parents(copyOf(parentRevisions));
        }

        public This parents(Iterable<Revision> parentRevisions) {
            return parents(copyOf(parentRevisions));
        }

        public This parents(Set<Revision> parentRevisions) {
            this.parentRevisions = (parentRevisions != null ? parentRevisions : EMPTY_PARENTS);
            return self();
        }

        public This changeset(Map<K, V> changeset) {
            this.changeset = (changeset != null ? changeset : ImmutableMap.of());
            return self();
        }

        public This meta(M meta) {
            this.meta = meta;
            return self();
        }

        @SuppressWarnings("unchecked")
        protected This self() {
            return (This) this;
        }

        public Version<K, V, M> build() {
            if (revision == null) {
                revision = new Revision();
            }
            return new Version<>(this);
        }

    }

}

