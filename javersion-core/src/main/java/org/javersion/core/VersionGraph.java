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

import org.javersion.util.PersistentSortedMap;

import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.StreamSupport.stream;

public interface VersionGraph<K, V, M> {

    VersionGraph<K, V, M> commit(Version<K, V, M> version);

    VersionGraph<K, V, M> commit(Iterable<? extends Version<K, V, M>> versions);

    VersionNode<K, V, M> getVersionNode(Revision revision);

    Merge<K, V, M> mergeBranches(String... branches);

    Merge<K, V, M> mergeBranches(Iterable<String> branches);

    Merge<K, V, M> mergeRevisions(Revision... revisions);

    Merge<K, V, M> mergeRevisions(Iterable<Revision> revisions);

    Iterable<VersionNode<K, V, M>> getHeads(String branch);

    VersionNode<K, V, M> getHead(String branch);

    PersistentSortedMap<BranchAndRevision, VersionNode<K, V, M>> getHeads();

    Set<Revision> getHeadRevisions();

    Set<Revision> getHeadRevisions(String branch);

    VersionGraph<K, V, M> at(Revision revision);

    VersionGraph<K, V, M> atTip();

    boolean isEmpty();

    int size();

    boolean contains(Revision revision);

    default boolean containsAll(Iterable<Revision> revisions) {
        return stream(revisions.spliterator(), false).allMatch(this::contains);
    }

    VersionNode<K, V, M> getTip();

    Set<String> getBranches();

    Iterable<Version<K, V, M>> getVersions();

    Iterable<VersionNode<K, V, M>> getVersionNodes();

    OptimizedGraph<K, V, M, ? extends VersionGraph<K, V, M>> optimize(Set<Revision> revisions);

    OptimizedGraph<K, V, M, ? extends VersionGraph<K, V, M>> optimize(Predicate<VersionNode<K, V, M>> keep);

}
