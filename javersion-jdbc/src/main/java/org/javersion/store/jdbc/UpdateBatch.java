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

import java.util.function.Predicate;

import org.javersion.core.VersionNode;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;

public interface UpdateBatch<Id, M> {
    UpdateBatch<Id, M> addVersion(Id docId, VersionNode<PropertyPath, Object, M> version);

    UpdateBatch<Id, M> prune(ObjectVersionGraph<M> graph, Predicate<VersionNode<PropertyPath, Object, M>> keep);

    UpdateBatch<Id, M> optimize(ObjectVersionGraph<M> graph, Predicate<VersionNode<PropertyPath, Object, M>> keep);

    void execute();
}
