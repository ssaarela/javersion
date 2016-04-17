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
package org.javersion.core;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.Lists;

@NotThreadSafe
public class OptimizedGraph<K, V, M,
        G extends VersionGraph<K, V, M, G, B>,
        B extends VersionGraphBuilder<K, V, M, G, B>> {

    private final G graph;

    private final List<Revision> keptRevisions;

    private final List<Revision> squashedRevisions;

    OptimizedGraph(G graph, List<Revision> keptRevisions, List<Revision> squashedRevisions) {
        this.graph = graph;
        this.keptRevisions = keptRevisions;
        this.squashedRevisions = squashedRevisions;
    }

    public G getGraph() {
        return graph;
    }

    public Iterable<Version<K, V, M>> getOptimizedVersions() {
        return Lists.transform(keptRevisions, revision -> graph.getVersionNode(revision).getVersion());
    }

    public List<Revision> getSquashedRevisions() {
        return squashedRevisions;
    }

    public List<Revision> getKeptRevisions() {
        return keptRevisions;
    }

}
