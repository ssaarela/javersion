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

import com.google.common.collect.ImmutableMap;
import org.javersion.core.Revision;
import org.javersion.object.ObjectVersionGraph;

import java.util.Map;
import java.util.Set;

public class GraphResults<Id, M> {

    public final Map<Id, ObjectVersionGraph<M>> graphsByDocId;

    public final Revision latestRevision;

    public GraphResults(Map<Id, ObjectVersionGraph<M>> graphsByDocId, Revision latestRevision) {
        this.graphsByDocId = ImmutableMap.copyOf(graphsByDocId);
        this.latestRevision = latestRevision;
    }

    public boolean isEmpty() {
        return graphsByDocId.isEmpty();
    }

    public int size() {
        return graphsByDocId.size();
    }

    public Set<Id> getDocIds() {
        return graphsByDocId.keySet();
    }

    public boolean containsKey(Id key) {
        return graphsByDocId.containsKey(key);
    }

    public ObjectVersionGraph<M> getVersionGraph(Id docId) {
        return graphsByDocId.get(docId);
    }
}
