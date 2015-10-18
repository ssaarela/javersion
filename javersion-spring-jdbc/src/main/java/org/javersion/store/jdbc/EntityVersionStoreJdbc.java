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

import static com.mysema.query.support.Expressions.constant;
import static com.mysema.query.support.Expressions.predicate;
import static com.mysema.query.types.Ops.EQ;
import static com.mysema.query.types.Ops.IN;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

import java.util.Collection;
import java.util.Map;

import org.javersion.core.Revision;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Path;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.expr.SimpleExpression;

public class EntityVersionStoreJdbc<Id extends Comparable, M> extends AbstractVersionStoreJdbc<Id, M, JEntityVersion<Id>, EntityStoreOptions<Id>> {

    public static void registerTypes(String tablePrefix, Configuration configuration) {
        AbstractVersionStoreJdbc.registerTypes(tablePrefix, configuration);
    }

    @SuppressWarnings("unused")
    protected EntityVersionStoreJdbc() {
        super();
    }

    public <P extends SimpleExpression<Id> & Path<Id>> EntityVersionStoreJdbc(EntityStoreOptions<Id> options) {
        super(options);
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public EntityUpdateBatch<Id, M> updateBatch(Collection<Id> docIds) {
        return new EntityUpdateBatch<>(options, docIds);
    }


    @Override
    protected EntityUpdateBatch<Id, M> optimizationUpdateBatch() {
        return new EntityUpdateBatch<>(options);
    }

    @Override
    protected BooleanExpression versionsOf(Id docId) {
        return predicate(EQ, options.version.docId, constant(docId));
    }

    @Override
    protected BooleanExpression versionsOf(Collection<Id> docIds) {
        return predicate(IN, constant(docIds));
    }

    @Override
    protected OrderSpecifier<?>[] versionsOfOneOrderBy() {
        return new OrderSpecifier<?>[] { options.version.localOrdinal.asc() };
    }

    @Override
    protected OrderSpecifier<?>[] versionsOfManyOrderBy() {
        return new OrderSpecifier<?>[] { options.version.ordinal.asc() };
    }

    @Override
    protected SQLUpdateClause setOrdinal(SQLUpdateClause versionUpdateBatch, long ordinal) {
        return versionUpdateBatch
                .set(options.version.ordinal, ordinal);
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
