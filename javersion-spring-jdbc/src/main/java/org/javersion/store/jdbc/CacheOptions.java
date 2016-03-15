/*
 * Copyright 2015 Samppa Saarela
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
package org.javersion.store.jdbc;

import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.javersion.core.Revision;
import org.javersion.core.VersionGraph;
import org.javersion.core.VersionNode;
import org.javersion.path.PropertyPath;
import org.javersion.util.Check;

import com.google.common.base.Function;

@Immutable
public class CacheOptions<Id, M> {

    public static class KeepHeadsAndNewest<M> implements Predicate<VersionNode<PropertyPath, Object, M>> {

        private int keepCount;

        private final Set<Revision> heads;

        public KeepHeadsAndNewest(VersionGraph<PropertyPath, Object, M, ?, ?> graph, int count) {
            Check.that(count >= 0, "count should be >= 0");
            keepCount = count;
            heads = graph.getHeads().keyStream()
                    .map(branchAndRevision -> branchAndRevision.revision)
                    .collect(toSet());
        }

        @Override
        public boolean test(VersionNode<PropertyPath, Object, M> versionNode) {
            return heads.contains(versionNode.revision) || keepCount-- > 0;
        }
    }

    public static <Id, M> CacheOptions<Id, M> keepHeadsAndNewest(final int count, final int compactThreshold) {
        Check.that(count >= 0, "count should be >= 0");
        Check.that(compactThreshold > count, "compactThreshold should be > count");
        return new CacheOptions<>(
                g -> g.versionNodes.size() - g.getHeads().size() >=  compactThreshold,
                (g) -> new KeepHeadsAndNewest<>(g, count)
        );
    }

    @Nonnull
    public final Predicate<VersionGraph<PropertyPath, Object, M, ?, ?>> compactWhen;

    @Nonnull
    public final Function<VersionGraph<PropertyPath, Object, M, ?, ?>, Predicate<VersionNode<PropertyPath, Object, M>>> compactKeep;

    public CacheOptions() {
        this(null, null);
    }

    public CacheOptions(@Nullable Predicate<VersionGraph<PropertyPath, Object, M, ?, ?>> compactWhen,
                        @Nullable Function<VersionGraph<PropertyPath, Object, M, ?, ?>, Predicate<VersionNode<PropertyPath, Object, M>>> compactKeep) {
        if (compactWhen != null) {
            if (compactKeep == null) {
                throw new IllegalArgumentException("compactWhen requires compactKeep");
            }
            this.compactWhen = compactWhen;
            this.compactKeep = compactKeep;
        } else {
            if (compactKeep != null) {
                throw new IllegalArgumentException("compactKeep requires compactWhen");
            }
            this.compactWhen = g -> false;
            this.compactKeep = (g) -> v -> true;
        }
    }

}
