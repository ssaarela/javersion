/*
 * Copyright 2015 Samppa Saarela
 *
 * Licensed under the Apache License, Version 2.0 (the "License");x
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

import static com.mysema.query.group.GroupBy.groupBy;
import static com.mysema.query.support.Expressions.constant;
import static com.mysema.query.support.Expressions.predicate;
import static com.mysema.query.types.Ops.EQ;
import static com.mysema.query.types.Ops.GT;
import static com.mysema.query.types.Ops.IN;
import static com.mysema.query.types.Ops.IS_NULL;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.javersion.core.Revision;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.util.Check;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mysema.query.ResultTransformer;
import com.mysema.query.group.Group;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.Expression;
import com.mysema.query.types.expr.BooleanExpression;

public class EntityVersionStoreJdbc<Id extends Comparable, M, V extends JEntityVersion<Id>> extends AbstractVersionStoreJdbc<Id, M, V,
        EntityStoreOptions<Id, V>> {

    protected final ResultTransformer<List<Group>> versionAndParentsSince;

    @SuppressWarnings("unused")
    protected EntityVersionStoreJdbc() {
        super();
        versionAndParentsSince = null;
    }

    public EntityVersionStoreJdbc(EntityStoreOptions<Id, V> options) {
        super(options);
        Expression<?>[] values = concat(versionAndParentColumns, options.sinceVersion.localOrdinal);
        versionAndParentsSince = groupBy(options.version.revision).list(values);
    }

    @Override
    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public ObjectVersionGraph<M> load(Id docId) {
        Check.notNull(docId, "docId");

        BooleanExpression predicate = versionsOf(docId);

        List<Group> versionsAndParents = fetchVersionsAndParents(predicate,
                options.version.localOrdinal.asc());

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

        Long sinceOrdinal = versionsAndParents.get(0).getOne(options.sinceVersion.localOrdinal);

        BooleanExpression predicate = versionsOf(docId)
                .and(predicate(GT, options.version.localOrdinal, constant(sinceOrdinal)));

        FetchResults<Id, M> results = fetch(versionsAndParents, predicate);
        return results.containsKey(docId) ? results.getVersions(docId) : ImmutableList.of();
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = MANDATORY)
    public EntityUpdateBatch<Id, M, V> updateBatch(Id docId) {
        return updateBatch(ImmutableSet.of(docId));
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = MANDATORY)
    public EntityUpdateBatch<Id, M, V> updateBatch(Collection<Id> docIds) {
        return new EntityUpdateBatch<>(options, docIds);
    }

    @Override
    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = MANDATORY)
    protected EntityUpdateBatch<Id, M, V> optimizationUpdateBatch() {
        return new EntityUpdateBatch<>(options);
    }

    @Override
    protected SQLUpdateClause setOrdinal(SQLUpdateClause versionUpdateBatch, long ordinal) {
        return versionUpdateBatch.set(options.version.ordinal, ordinal);
    }

    private BooleanExpression versionsOf(Id docId) {
        return predicate(EQ, options.version.docId, constant(docId));
    }

    protected List<Group> versionsAndParentsSince(Id docId, Revision since) {
        SQLQuery qry = options.queryFactory.from(options.sinceVersion);

        // Left join version version on version.ordinal > since.ordinal and version.doc_id = since.doc_id
        qry.leftJoin(options.version).on(
                options.version.localOrdinal.gt(options.sinceVersion.localOrdinal),
                predicate(EQ, options.version.docId, options.sinceVersion.docId));

        // Left join parents
        qry.leftJoin(options.parent).on(options.parent.revision.eq(options.version.revision));

        qry.where(options.sinceVersion.revision.eq(since),
                versionsOf(docId).or(predicate(IS_NULL, options.version.docId)));

        qry.orderBy(options.version.localOrdinal.asc());

        return verifyVersionsAndParentsSince(qry.transform(versionAndParentsSince), since);
    }

    @Override
    protected Map<Revision, Id> findUnpublishedRevisions() {
        return options.queryFactory
                .from(options.version)
                .where(options.version.ordinal.isNull())
                .orderBy(options.version.localOrdinal.asc())
                .map(options.version.revision, options.version.docId);
    }

}
