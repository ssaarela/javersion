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

import static com.mysema.query.support.Expressions.constant;
import static com.mysema.query.support.Expressions.predicate;
import static com.mysema.query.types.Ops.EQ;
import static com.mysema.query.types.Ops.IN;
import static java.util.Collections.singleton;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

import java.util.Collection;
import java.util.Map;

import org.javersion.core.Revision;
import org.javersion.core.VersionNode;
import org.javersion.path.PropertyPath;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.BooleanExpression;

public class DocumentVersionStoreJdbc<Id, M> extends AbstractVersionStoreJdbc<Id, M, JDocumentVersion<Id>, DocumentStoreOptions<Id>> {

    @SuppressWarnings("unused")
    protected DocumentVersionStoreJdbc() {
        super();
    }

    public DocumentVersionStoreJdbc(DocumentStoreOptions<Id> options) {
        super(options);
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
        DocumentUpdateBatch<Id, M> batch = optimizationUpdateBatch();

        for (Id docId : versionsByDocId.keySet()) {
            for (VersionNode<PropertyPath, Object, M> version : versionsByDocId.get(docId)) {
                batch.addVersion(docId, version);
            }
        }

        batch.execute();
    }

    @Override
    protected DocumentUpdateBatch<Id, M> optimizationUpdateBatch() {
        return new DocumentUpdateBatch<>(options);
    }

    @Override
    protected BooleanExpression versionsOf(Id docId) {
        return predicate(EQ, options.version.docId, constant(docId))
                .and(options.version.ordinal.isNotNull());
    }

    @Override
    protected BooleanExpression versionsOf(Collection<Id> docIds) {
        return predicate(IN, options.version.docId, constant(docIds))
                .and(options.version.ordinal.isNotNull());
    }

    @Override
    protected OrderSpecifier<?>[] versionsOfOneOrderBy() {
        return versionsOfManyOrderBy();
    }

    @Override
    protected OrderSpecifier<?>[] versionsOfManyOrderBy() {
        return new OrderSpecifier<?>[] { options.version.ordinal.asc() };
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
                .map(options.version.revision, options.version.docId);
    }

}
