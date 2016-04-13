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

    ObjectVersionGraph<M> load(Id docId);

    ObjectVersionGraph<M> loadOptimized(Id docId);

    GraphResults<Id, M> load(Collection<Id> docIds);

    List<ObjectVersion<M>> fetchUpdates(Id docId, Revision since);

    Multimap<Id, Revision> publish();

    void prune(Id docId, Function<ObjectVersionGraph<M>, Predicate<VersionNode<PropertyPath, Object, M>>> keep);

    void reset(Id docId);

    UpdateBatch<Id, M> updateBatch(Id id);

    UpdateBatch<Id, M> updateBatch(Collection<Id> ids);
}
