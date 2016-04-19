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

import static org.javersion.store.jdbc.VersionStatus.ACTIVE;
import static org.javersion.store.jdbc.VersionStatus.REDUNDANT;
import static org.javersion.store.jdbc.VersionStatus.SQUASHED;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.javersion.core.OptimizedGraph;
import org.javersion.core.Persistent;
import org.javersion.core.Revision;
import org.javersion.core.VersionNode;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;

import com.querydsl.core.dml.StoreClause;
import com.querydsl.core.group.Group;
import com.querydsl.core.types.Path;
import com.querydsl.sql.dml.SQLInsertClause;

public abstract class AbstractUpdateBatch<Id, M,
                V extends JVersion<Id>,
                Options extends StoreOptions<Id, M, V>,
                This extends AbstractUpdateBatch<Id, M, V, Options, This>>
        implements UpdateBatch<Id, M> {

    protected static boolean isNotEmpty(StoreClause<?> store) {
        return store != null && !store.isEmpty();
    }

    protected final Options options;

    protected final AbstractVersionStoreJdbc<Id, M, V, This, Options> store;

    protected final SQLInsertClause versionBatch;

    protected final SQLInsertClause parentBatch;

    protected final SQLInsertClause propertyBatch;

    public AbstractUpdateBatch(AbstractVersionStoreJdbc<Id, M, V, This, Options> store) {
        this.store = store;
        this.options = store.options;
        versionBatch = options.queryFactory.insert(options.version);
        parentBatch = options.queryFactory.insert(options.parent);
        propertyBatch = options.queryFactory.insert(options.property);
    }

    @Override
    public This addVersion(Id docId, VersionNode<PropertyPath, Object, M> version) {
        insertVersion(docId, version);
        insertParents(version);
        insertProperties(version);
        return self();
    }

    @Override
    public void execute() {
        if (isNotEmpty(versionBatch)) {
            versionBatch.execute();
            if (options.publisher != null) {
                options.transactions.afterCommit(this::publishAfterCommit);
            }
        }
        if (isNotEmpty(parentBatch)) {
            parentBatch.execute();
        }
        if (isNotEmpty(propertyBatch)) {
            propertyBatch.execute();
        }
    }

    protected This prune(ObjectVersionGraph<M> graph, Predicate<VersionNode<PropertyPath, Object, M>> keep) {
        OptimizedGraph<PropertyPath, Object, M, ObjectVersionGraph<M>> optimizedGraph = optimizedGraph(graph, keep);
        if (optimizedGraph != null) {
            List<Revision> keptRevisions = optimizedGraph.getKeptRevisions();
            List<Revision> squashedRevisions = optimizedGraph.getSquashedRevisions();
            List<Revision> modifiedRevisions = concat(keptRevisions, squashedRevisions);

            if (!squashedRevisions.isEmpty()) {
                deleteParents(modifiedRevisions);
                deleteProperties(modifiedRevisions);
                deleteVersions(squashedRevisions);
                insertOptimizedParentsAndProperties(optimizedGraph.getGraph());
            }
        }
        return self();
    }

    protected This optimize(ObjectVersionGraph<M> graph, Predicate<VersionNode<PropertyPath, Object, M>> keep) {
        OptimizedGraph<PropertyPath, Object, M, ObjectVersionGraph<M>> optimizedGraph = optimizedGraph(graph, keep);
        if (optimizedGraph != null) {
            List<Revision> squashedRevisions = optimizedGraph.getSquashedRevisions();

            if (!squashedRevisions.isEmpty()) {
                squashVersions(squashedRevisions);
                deleteRedundantParents(squashedRevisions);
                deleteRedundantProperties(squashedRevisions);
                optimizeParentsAndProperties(graph, optimizedGraph.getGraph());
            }
        }
        return self();
    }


    @SuppressWarnings("unchecked")
    protected This self() {
        return (This) this;
    }

    protected void publishAfterCommit() {
        options.publisher.execute(store::publish);
    }

    private void optimizeParentsAndProperties(ObjectVersionGraph<M> oldGraph, ObjectVersionGraph<M> newGraph) {
        newGraph.getVersionNodes().forEach(newVersionNode -> {
            VersionNode<PropertyPath, Object, M> oldVersionNode = oldGraph.getVersionNode(newVersionNode.revision);
            optimizeParents(newVersionNode.revision, oldVersionNode.getParentRevisions(), newVersionNode.getParentRevisions());
            optimizeProperties(newVersionNode.revision, oldVersionNode.getChangeset(), newVersionNode.getChangeset());
        });
    }

    private void optimizeProperties(Revision revision, Map<PropertyPath, Object> oldChangeset, Map<PropertyPath, Object> newChangeset) {
        newChangeset.forEach((path, value) -> {
            if (!oldChangeset.containsKey(path)) {
                insertProperty(revision, path, value, REDUNDANT);
            }
        });
        oldChangeset.forEach((path, value) -> {
            if (!newChangeset.containsKey(path)) {
                squashProperty(revision, path);
            }
        });
    }

    private void optimizeParents(Revision revision, Set<Revision> oldParentRevisions, Set<Revision> newParentRevisions) {
        newParentRevisions.forEach(newParentRevision -> {
            if (!oldParentRevisions.contains(newParentRevision)) {
                insertParent(revision, newParentRevision, REDUNDANT);
            }
        });
        oldParentRevisions.forEach(oldParentRevision -> {
            if (!newParentRevisions.contains(oldParentRevision)) {
                squashParent(revision, oldParentRevision);
            }
        });
    }

    private <T> List<T> concat(List<T> a, List<T> b) {
        List<T> combined = new ArrayList<>(a.size() + b.size());
        combined.addAll(a);
        combined.addAll(b);
        return combined;
    }

    private OptimizedGraph<PropertyPath, Object, M, ObjectVersionGraph<M>> optimizedGraph(ObjectVersionGraph<M> graph, Predicate<VersionNode<PropertyPath, Object, M>> keep) {
        OptimizedGraph<PropertyPath, Object, M, ObjectVersionGraph<M>> optimizedGraph = graph.optimize(keep);

        if (optimizedGraph.getSquashedRevisions().isEmpty()) {
            return null;
        }
        if (optimizedGraph.getKeptRevisions().isEmpty()) {
            throw new IllegalArgumentException("keep-predicate didn't match any version");
        }

        return optimizedGraph;
    }

    protected void insertVersion(Id docId, VersionNode<PropertyPath, Object, M> version) {
        versionBatch
                .set(options.version.docId, docId)
                .set(options.version.revision, version.revision)
                .set(options.version.status, ACTIVE)
                .set(options.version.type, version.type)
                .set(options.version.branch, version.branch);

        if (!options.versionTableProperties.isEmpty()) {
            Map<PropertyPath, Object> properties = version.getProperties();
            options.versionTableProperties.forEach((path, column) -> {
               @SuppressWarnings("unchecked")
                Path<Object> columnPath = (Path<Object>) column;
                versionBatch.set(columnPath, properties.get(path));
            });
        }

        setMeta(version.getMeta(), versionBatch);

        versionBatch.addBatch();
    }

    /**
     * Override to persist custom metadata into VERSION table.
     *
     * @see AbstractVersionStoreJdbc#getMeta(Group)
     */
    protected void setMeta(M meta, StoreClause versionBatch) {}

    protected void insertParents(VersionNode<PropertyPath, Object, M> version) {
        version.parentRevisions.forEach(parentRevision -> insertParent(version.revision, parentRevision, ACTIVE));
    }

    protected void insertParent(Revision revision, Revision parentRevision, VersionStatus status) {
        parentBatch
                .set(options.parent.revision, revision)
                .set(options.parent.parentRevision, parentRevision)
                .set(options.parent.status, status)
                .addBatch();
    }

    protected void insertProperties(VersionNode<PropertyPath, Object, M> version) {
        version.getChangeset().forEach((path, value) -> insertProperty(version.revision, path, value, ACTIVE));
    }

    protected void insertProperty(Revision revision, PropertyPath path, Object value, VersionStatus status) {
        if (!options.versionTableProperties.containsKey(path)) {
            propertyBatch
                    .set(options.property.revision, revision)
                    .set(options.property.path, path.toString())
                    .set(options.property.status, status);
            setValue(path, value);
            propertyBatch.addBatch();
        }
    }

    protected void setValue(@SuppressWarnings("unused") PropertyPath path, Object value) {
        // type:
        // n=null, O=object, A=array, s=string,
        // b=boolean, l=long, d=double, D=bigdecimal
        char type;
        String str = null;
        Long nbr = null;
        switch (Persistent.Type.of(value)) {
            case TOMBSTONE:
                type = 'n';
                break;
            case NULL:
                type = 'N';
                break;
            case OBJECT:
                type = 'O';
                str = ((Persistent.Object) value).type;
                break;
            case ARRAY:
                type = 'A';
                break;
            case STRING:
                type = 's';
                str = (String) value;
                break;
            case BOOLEAN:
                type = 'b';
                nbr = ((Boolean) value) ? 1L : 0L;
                break;
            case LONG:
                type = 'l';
                nbr = (Long) value;
                break;
            case DOUBLE:
                type = 'd';
                nbr = Double.doubleToRawLongBits((Double) value);
                break;
            case BIG_DECIMAL:
                type = 'D';
                str = value.toString();
                break;
            default:
                throw new IllegalArgumentException("Unsupported type: " + value.getClass());
        }
        propertyBatch
                .set(options.property.type, Character.toString(type))
                .set(options.property.str, str)
                .set(options.property.nbr, nbr);
    }

    private void insertOptimizedParentsAndProperties(ObjectVersionGraph<M> optimizedGraph) {
        optimizedGraph.getVersionNodes().forEach(node -> {
            insertParents(node);
            insertProperties(node);
        });
    }

    private void deleteParents(List<Revision> revisions) {
        options.queryFactory
                .delete(options.parent)
                .where(options.parent.revision.in(revisions))
                .execute();
    }

    private void deleteProperties(List<Revision> revisions) {
        options.queryFactory
                .delete(options.property)
                .where(options.property.revision.in(revisions))
                .execute();
    }

    private void deleteVersions(List<Revision> revisions) {
        // Delete squashed versions
        long count = options.queryFactory
                .delete(options.version)
                .where(options.version.revision.in(revisions))
                .execute();
        if (count != revisions.size()) {
            throw new ConcurrentMaintenanceException("Expected to delete " + revisions.size() + " revisions. Got " + count);
        }
    }

    private void deleteRedundantParents(List<Revision> revisions) {
        options.queryFactory
                .delete(options.parent)
                .where(options.parent.revision.in(revisions), options.parent.status.eq(REDUNDANT))
                .execute();
    }

    private void deleteRedundantProperties(List<Revision> revisions) {
        options.queryFactory
                .delete(options.property)
                .where(options.property.revision.in(revisions), options.property.status.eq(REDUNDANT))
                .execute();
    }

    private void squashVersions(List<Revision> revisions) {
        long count = options.queryFactory
                .update(options.version)
                .set(options.version.status, SQUASHED)
                .where(options.version.revision.in(revisions), options.version.status.ne(SQUASHED))
                .execute();
        if (count != revisions.size()) {
            throw new ConcurrentMaintenanceException("Expected to squash " + revisions.size() + " revisions. Got " + count);
        }
    }

    private void squashParent(Revision revision, Revision parentRevision) {
        options.queryFactory
                .update(options.parent)
                .set(options.parent.status, SQUASHED)
                .where(options.parent.revision.eq(revision), options.parent.parentRevision.eq(parentRevision))
                .execute();
    }

    private void squashProperty(Revision revision, PropertyPath path) {
        options.queryFactory
                .update(options.property)
                .set(options.property.status, SQUASHED)
                .where(options.property.revision.eq(revision), options.property.path.eq(path.toString()))
                .execute();
    }

}
