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
import static com.querydsl.core.types.dsl.Expressions.constant;
import static com.querydsl.core.types.dsl.Expressions.predicate;
import static com.querydsl.core.types.Ops.EQ;
import static com.querydsl.core.types.Ops.IN;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.javersion.core.VersionNode;
import org.javersion.path.PropertyPath;

import com.google.common.collect.ImmutableSet;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;

public class EntityUpdateBatch<Id extends Comparable, M, V extends JEntityVersion<Id>>
        extends AbstractUpdateBatch<Id, M, V, EntityStoreOptions<Id, M, V>, EntityUpdateBatch<Id, M, V>> {

    protected final SQLInsertClause entityCreateBatch;

    protected final Set<Id> lockedDocIds;

    private final Map<Id, Long> entityOrdinals;

    public EntityUpdateBatch(EntityStoreOptions<Id, M, V> options, Collection<Id> docIds) {
        super(options);
        entityCreateBatch = options.queryFactory.insert(options.entity);

        lockedDocIds = ImmutableSet.copyOf(docIds);
        entityOrdinals = lockEntitiesForUpdate(options, docIds);
    }

    public boolean contains(Id docId) {
        return lockedDocIds.contains(docId);
    }

    public boolean isCreate(Id docId) {
        return contains(docId) && !entityOrdinals.containsKey(docId);
    }

    public boolean isUpdate(Id docId) {
        return contains(docId) && entityOrdinals.containsKey(docId);
    }

    public EntityUpdateBatch<Id, M, V> addVersion(Id docId, VersionNode<PropertyPath, Object, M> version) {
        verifyDocId(docId);

        if (isCreate(docId)) {
            insertEntity(docId, version);
        } else {
            updateEntity(docId, version);
        }

        return super.addVersion(docId, version);
    }

    @Override
    public void execute() {
        if (isNotEmpty(entityCreateBatch)) {
            entityCreateBatch.execute();
        }
        super.execute();
    }

    protected void insertEntity(Id docId, VersionNode<PropertyPath, Object, M> version) {
        entityCreateBatch
                .set(options.entity.id, docId)
                .addBatch();
    }

    protected void updateEntity(Id docId, VersionNode<PropertyPath, Object, M> version) {}

    @Override
    protected void insertVersion(Id docId, VersionNode<PropertyPath, Object, M> version) {
        versionBatch.set(options.version.localOrdinal, nextLocalOrdinal(docId));
        super.insertVersion(docId, version);
    }

    protected Map<Id, Long> lockEntitiesForUpdate(EntityStoreOptions<Id, M, V> options, Collection<Id> docIds) {
        return options.queryFactory
                .from(options.entity)
                .where(predicate(IN, options.entity.id, constant(docIds)))
                .orderBy(new OrderSpecifier<>(Order.ASC, options.entity.id))
                .forUpdate()
                .transform(groupBy(options.entity.id).as(maxLocalOrdinalByEntity(options)));
    }

    protected SQLQuery<Long> maxLocalOrdinalByEntity(EntityStoreOptions<Id, M, V> options) {
        return options.queryFactory
                .select(options.version.localOrdinal.max())
                .from(options.version)
                .where(predicate(EQ, options.version.docId, options.entity.id))
                .groupBy(options.entity.id);
    }

    protected void verifyDocId(Id docId) {
        if (!contains(docId)) {
            throw new IllegalStateException("docId not marked for inclusion in this batch: " + docId);
        }
    }

    protected long nextLocalOrdinal(Id docId) {
        long currentTime = System.currentTimeMillis();
        long prevOrdinal = getPrevOrdinal(docId);
        long nextOrdinal = prevOrdinal < currentTime ? currentTime : prevOrdinal + 1;
        setPrevOrdinal(docId, nextOrdinal);
        return nextOrdinal;
    }

    private long getPrevOrdinal(Id docId) {
        Long prevOrdinal = entityOrdinals.get(docId);
        return prevOrdinal != null ? prevOrdinal : Long.MIN_VALUE;
    }

    private void setPrevOrdinal(Id docId, long ordinal) {
        entityOrdinals.put(docId, ordinal);
    }
}
