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
package org.javersion.store.jdbc;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.javersion.core.Revision;
import org.javersion.core.VersionNode;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;

import com.google.common.collect.Multimap;

public interface VersionStore<Id, M> {

    ObjectVersionGraph<M> getGraph(Id docId);

    ObjectVersionGraph<M> getGraph(Id docId, Iterable<Revision> revisions);

    ObjectVersionGraph<M> getFullGraph(Id docId);

    ObjectVersionGraph<M> getOptimizedGraph(Id docId);

    GraphResults<Id, M> getGraphs(Collection<Id> docIds);

    List<ObjectVersion<M>> fetchUpdates(Id docId, Revision since);

    Multimap<Id, Revision> publish();

    void prune(Id docId, Function<ObjectVersionGraph<M>, Predicate<VersionNode<PropertyPath, Object, M>>> keep);

    void optimize(Id docId, Function<ObjectVersionGraph<M>, Predicate<VersionNode<PropertyPath, Object, M>>> keep);

    void reset(Id docId);

    UpdateBatch<Id, M> updateBatch(Id id);

    UpdateBatch<Id, M> updateBatch(Collection<Id> ids);

}
