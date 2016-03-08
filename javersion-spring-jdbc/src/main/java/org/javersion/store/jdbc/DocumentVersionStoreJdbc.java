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

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.types.Ops.EQ;
import static com.querydsl.core.types.Ops.GT;
import static com.querydsl.core.types.Ops.IN;
import static com.querydsl.core.types.Ops.IS_NULL;
import static com.querydsl.core.types.dsl.Expressions.constant;
import static com.querydsl.core.types.dsl.Expressions.predicate;
import static java.util.Collections.singleton;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.javersion.core.Revision;
import org.javersion.core.VersionNode;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.javersion.util.Check;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.querydsl.core.group.Group;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLUpdateClause;

public class DocumentVersionStoreJdbc<Id, M, V extends JDocumentVersion<Id>> extends AbstractVersionStoreJdbc<Id, M, V, DocumentStoreOptions<Id, V>> {

    protected final Expression<?>[] versionAndParentsSince;

    @SuppressWarnings("unused")
    protected DocumentVersionStoreJdbc() {
        super();
        versionAndParentsSince = null;
    }

    public DocumentVersionStoreJdbc(DocumentStoreOptions<Id, V> options) {
        super(options);
        versionAndParentsSince = concat(versionAndParentColumns, options.sinceVersion.ordinal);
    }

    @Override
    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public ObjectVersionGraph<M> load(Id docId) {
        Check.notNull(docId, "docId");

        BooleanExpression predicate = versionsOf(docId);

        List<Group> versionsAndParents = fetchVersionsAndParents(predicate,
                options.version.ordinal.asc());

        FetchResults<Id, M> results = fetch(versionsAndParents, predicate);
        return results.containsKey(docId) ? results.getVersionGraph(docId) : ObjectVersionGraph.init();
    }

    @Override
    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public FetchResults<Id, M> load(Collection<Id> docIds) {
        Check.notNull(docIds, "docIds");

        BooleanExpression predicate =
                predicate(IN, options.version.docId, constant(docIds))
                        .and(options.version.ordinal.isNotNull());

        List<Group> versionsAndParents = fetchVersionsAndParents(predicate,
                options.version.ordinal.asc());

        return fetch(versionsAndParents, predicate);
    }

    @Override
    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public List<ObjectVersion<M>> fetchUpdates(Id docId, Revision since) {
        List<Group> versionsAndParents = versionsAndParentsSince(docId, since);
        if (versionsAndParents.isEmpty()) {
            return ImmutableList.of();
        }

        Long sinceOrdinal = versionsAndParents.get(0).getOne(options.sinceVersion.ordinal);

        BooleanExpression predicate = versionsOf(docId)
                .and(predicate(GT, options.version.ordinal, constant(sinceOrdinal)));

        FetchResults<Id, M> results = fetch(versionsAndParents, predicate);
        return results.containsKey(docId) ? results.getVersions(docId) : ImmutableList.of();
    }

    @Override
    public DocumentUpdateBatch<Id, M, V> updateBatch(Collection<Id> ids) {
        return new DocumentUpdateBatch<Id, M, V>(options);
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public void append(Id docId, VersionNode<PropertyPath, Object, M> version) {
        append(docId, singleton(version));
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public void append(Id docId, Iterable<VersionNode<PropertyPath, Object, M>> versions) {
        ImmutableMultimap.Builder<Id, VersionNode<PropertyPath, Object, M>> builder = ImmutableMultimap.builder();
        append(builder.putAll(docId, versions).build());
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public void append(Multimap<Id, VersionNode<PropertyPath, Object, M>> versionsByDocId) {
        DocumentUpdateBatch<Id, M, V> batch = updateBatch(versionsByDocId.keys());

        for (Id docId : versionsByDocId.keySet()) {
            for (VersionNode<PropertyPath, Object, M> version : versionsByDocId.get(docId)) {
                batch.addVersion(docId, version);
            }
        }

        batch.execute();
    }

    protected BooleanExpression versionsOf(Id docId) {
        return predicate(EQ, options.version.docId, constant(docId))
                .and(options.version.ordinal.isNotNull());
    }

    protected List<Group> versionsAndParentsSince(Id docId, Revision since) {
        SQLQuery<?> qry = options.queryFactory.from(options.sinceVersion);

        // Left join version version on version.ordinal > since.ordinal and version.doc_id = since.doc_id
        qry.leftJoin(options.version).on(
                options.version.ordinal.gt(options.sinceVersion.ordinal),
                predicate(EQ, options.version.docId, options.sinceVersion.docId));

        // Left join parents
        qry.leftJoin(options.parent).on(options.parent.revision.eq(options.version.revision));

        qry.where(options.sinceVersion.revision.eq(since),
                versionsOf(docId).or(predicate(IS_NULL, options.version.docId)));

        qry.orderBy(options.version.ordinal.asc());

        return verifyVersionsAndParentsSince(qry.transform(groupBy(options.version.revision).list(versionAndParentsSince)), since);
    }

    @Override
    protected SQLUpdateClause setOrdinal(SQLUpdateClause versionUpdateBatch, long ordinal) {
        return versionUpdateBatch
                .set(options.version.ordinal, ordinal)
                .setNull(options.version.txOrdinal);
    }

    @Override
    protected Map<Revision, Id> findUnpublishedRevisions() {
        return options.queryFactory
                .from(options.version)
                .where(options.version.txOrdinal.isNotNull())
                .orderBy(options.version.txOrdinal.asc())
                .transform(groupBy(options.version.revision).as(options.version.docId));
    }

}
