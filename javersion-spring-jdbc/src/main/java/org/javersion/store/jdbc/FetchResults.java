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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.javersion.core.Revision;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

public class FetchResults<Id, M> {

    public final ListMultimap<Id, ObjectVersion<M>> versionsByDocId;

    public final Revision latestRevision;

    public FetchResults() {
        versionsByDocId = ImmutableListMultimap.of();
        latestRevision = null;
    }
    public FetchResults(ListMultimap<Id, ObjectVersion<M>> versionsByDocId, Revision latestRevision) {
        this.versionsByDocId = versionsByDocId;
        this.latestRevision = latestRevision;
    }

    public int size() {
        return versionsByDocId.size();
    }

    public Set<Id> getDocIds() {
        return versionsByDocId.keySet();
    }

    public boolean containsKey(Id key) {
        return versionsByDocId.containsKey(key);
    }

    public Optional<List<ObjectVersion<M>>> getVersions(Id docId) {
        return Optional.ofNullable(versionsByDocId.get(docId));
    }

    public Optional<ObjectVersionGraph<M>> getVersionGraph(Id docId) {
        List<ObjectVersion<M>> versions = versionsByDocId.get(docId);
        return versions != null ? Optional.of(ObjectVersionGraph.init(versions)) : Optional.empty();
    }
}
