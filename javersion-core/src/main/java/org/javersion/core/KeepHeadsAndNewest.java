/*
 * Copyright 2016 Samppa Saarela
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

import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.concurrent.NotThreadSafe;

import org.javersion.util.Check;

@NotThreadSafe
public class KeepHeadsAndNewest<K, V, M> implements Predicate<VersionNode<K, V, M>> {

    private int keepCount;

    private final Set<Revision> heads;

    public KeepHeadsAndNewest(VersionGraph<K, V, M> graph, int count) {
        Check.that(count >= 0, "count should be >= 0");
        keepCount = count;
        heads = graph.getHeads().keyStream()
                .map(branchAndRevision -> branchAndRevision.revision)
                .collect(toSet());
    }

    @Override
    public boolean test(VersionNode<K, V, M> versionNode) {
        return heads.contains(versionNode.revision) || keepCount-- > 0;
    }

}
