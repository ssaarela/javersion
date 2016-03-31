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

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.types.Ops.EQ;
import static com.querydsl.core.types.Ops.GT;
import static com.querydsl.core.types.Ops.IS_NULL;
import static com.querydsl.core.types.dsl.Expressions.constant;
import static com.querydsl.core.types.dsl.Expressions.predicate;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.javersion.core.Revision;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.util.Check;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.querydsl.core.ResultTransformer;
import com.querydsl.core.group.Group;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLUpdateClause;

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
        return load(docId, false);
    }

    @Override
    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public ObjectVersionGraph<M> loadOptimized(Id docId) {
        return load(docId, true);
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

        FetchResults<Id, M> results = fetch(versionsAndParents, false, predicate);
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
    protected SQLUpdateClause setOrdinal(SQLUpdateClause versionUpdateBatch, long ordinal) {
        return versionUpdateBatch.set(options.version.ordinal, ordinal);
    }

    @Nonnull
    private BooleanExpression versionsOf(Id docId) {
        return predicate(EQ, options.version.docId, constant(docId));
    }

    protected ObjectVersionGraph<M> load(Id docId, boolean optimized) {
        Check.notNull(docId, "docId");

        BooleanExpression predicate = versionsOf(docId);

        List<Group> versionsAndParents = fetchVersionsAndParents(optimized, predicate,
                options.version.localOrdinal.asc());

        FetchResults<Id, M> results = fetch(versionsAndParents, optimized, predicate);

        return results.containsKey(docId) ? results.getVersionGraph(docId) : ObjectVersionGraph.init();
    }

    protected List<Group> versionsAndParentsSince(Id docId, Revision since) {
        SQLQuery<?> qry = options.queryFactory.from(options.sinceVersion);

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
                .transform(groupBy(options.version.revision).as(options.version.docId));
    }

}
