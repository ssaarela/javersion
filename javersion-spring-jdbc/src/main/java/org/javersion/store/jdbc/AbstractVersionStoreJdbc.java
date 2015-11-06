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

import static com.mysema.query.group.GroupBy.groupBy;
import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;
import static org.javersion.store.jdbc.RevisionType.REVISION_TYPE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javersion.core.*;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionBuilder;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.*;
import com.mysema.query.ResultTransformer;
import com.mysema.query.Tuple;
import com.mysema.query.group.Group;
import com.mysema.query.group.GroupBy;
import com.mysema.query.group.QPair;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.sql.types.EnumByNameType;
import com.mysema.query.types.Expression;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Path;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.query.NumberSubQuery;


public abstract class AbstractVersionStoreJdbc<Id, M, V extends JVersion<Id>, Options extends StoreOptions<Id, V>> {

    public static void registerTypes(String tablePrefix, Configuration configuration) {
        configuration.register(tablePrefix + "VERSION", "TYPE", new EnumByNameType<>(VersionType.class));
        configuration.register(tablePrefix + "VERSION", "REVISION", REVISION_TYPE);

        configuration.register(tablePrefix + "VERSION_PARENT", "REVISION", REVISION_TYPE);
        configuration.register(tablePrefix + "VERSION_PARENT", "PARENT_REVISION", REVISION_TYPE);

        configuration.register(tablePrefix + "VERSION_PROPERTY", "REVISION", REVISION_TYPE);
    }


    protected final Options options;

    protected final Expression<?>[] versionAndParentColumns;

    protected final ResultTransformer<List<Group>>  versionAndParents;

    protected final QPair<Revision, Id> revisionAndDocId;

    protected final NumberSubQuery<Long> maxOrdinalSubQuery;

    protected final ResultTransformer<Map<Revision, List<Tuple>>> properties;

    protected final FetchResults<Id, M> noResults = new FetchResults<>();

    protected  AbstractVersionStoreJdbc() {
        options = null;
        versionAndParentColumns = null;
        versionAndParents = null;
        revisionAndDocId = null;
        maxOrdinalSubQuery = null;
        properties = null;
    }

    public AbstractVersionStoreJdbc(Options options) {
        this.options = options;

        // Querydsl 3.5 doesn't remove duplicates from select
        versionAndParentColumns = without(concat(options.version.all(), GroupBy.set(options.parent.parentRevision)), options.version.revision);
        versionAndParents = groupBy(options.version.revision).list(versionAndParentColumns);

        revisionAndDocId = new QPair<>(options.version.revision, options.version.docId);
        maxOrdinalSubQuery = maxOrdinalSubQuery(options);

        Expression<?>[] propertyColumns = without(options.property.all(), options.property.revision);
        properties = groupBy(options.property.revision).as(GroupBy.list(new QTuple(propertyColumns)));
    }

    public abstract ObjectVersionGraph<M> load(Id docId);

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public abstract FetchResults<Id, M> load(Collection<Id> docIds);

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public abstract List<ObjectVersion<M>> fetchUpdates(Id docId, Revision since);

    /**
     * NOTE: publish() needs to be called in a separate transaction from append()!
     * E.g. asynchronously in TransactionSynchronization.afterCommit.
     *
     * Calling publish() in the same transaction with append() severely limits concurrency
     * and might end up in deadlock.
     */
    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public Multimap<Id, Revision> publish() {
        // Lock repository with select for update
        long lastOrdinal = lockRepositoryAndGetMaxOrdinal();

        Map<Revision, Id> uncommittedRevisions = findUnpublishedRevisions();
        if (uncommittedRevisions.isEmpty()) {
            return ImmutableMultimap.of();
        }

        Multimap<Id, Revision> publishedDocs = ArrayListMultimap.create();

        SQLUpdateClause versionUpdateBatch = options.queryFactory.update(options.version);

        for (Map.Entry<Revision, Id> entry : uncommittedRevisions.entrySet()) {
            Revision revision = entry.getKey();
            Id docId = entry.getValue();
            publishedDocs.put(docId, revision);
            setOrdinal(versionUpdateBatch, ++lastOrdinal)
                    .where(options.version.revision.eq(revision))
                    .addBatch();
        }

        versionUpdateBatch.execute();

        afterPublish(publishedDocs);
        return publishedDocs;
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public void optimize(Id docId, java.util.function.Predicate<VersionNode<PropertyPath, Object, M>> keep) {
        ObjectVersionGraph<M> graph = load(docId);
        OptimizedGraphBuilder<PropertyPath, Object, M> optimizedGraphBuilder = new OptimizedGraphBuilder<>(graph, keep);

        List<Revision> keptRevisions = optimizedGraphBuilder.getKeptRevisions();
        List<Revision> squashedRevisions = optimizedGraphBuilder.getSquashedRevisions();

        if (squashedRevisions.isEmpty()) {
            return;
        }
        if (keptRevisions.isEmpty()) {
            throw new IllegalArgumentException("keep-predicate didn't match any version");
        }
        deleteOldParentsAndProperties(squashedRevisions, keptRevisions);
        deleteSquashedVersions(squashedRevisions);
        insertOptimizedParentsAndProperties(docId, ObjectVersionGraph.init(optimizedGraphBuilder.getOptimizedVersions()), keptRevisions);
    }

    protected abstract AbstractUpdateBatch<Id, M, V, ?> optimizationUpdateBatch();

    protected abstract SQLUpdateClause setOrdinal(SQLUpdateClause versionUpdateBatch, long ordinal);

    protected abstract Map<Revision, Id> findUnpublishedRevisions();

    protected M getMeta(Group versionAndParents) {
        return null;
    }

    protected void afterPublish(Multimap<Id, Revision> publishedDocs) {
        // After publish hook for sub classes to override
    }

    protected long lockRepositoryAndGetMaxOrdinal() {
        // Use List-result as a safe-guard against missing repository row
        List<Long> results = options.queryFactory
                .from(options.repository)
                .where(options.repository.id.eq(options.repositoryId))
                .forUpdate()
                .list(maxOrdinalSubQuery);

        if (results.isEmpty()) {
            throw new IllegalStateException("Repository with id " + options.repositoryId + " not found from " + options.repository.getTableName());
        }
        Long maxOrdinal = results.get(0);
        return maxOrdinal != null ? maxOrdinal : 0;
    }

    private NumberSubQuery<Long> maxOrdinalSubQuery(Options options) {
        return options.queryFactory.subQuery().from(options.version).unique(options.version.ordinal.max());
    }

    protected FetchResults<Id, M> fetch(List<Group> versionsAndParents, BooleanExpression predicate) {
        if (versionsAndParents.isEmpty()) {
            return noResults;
        }

        Map<Revision, List<Tuple>> properties = fetchProperties(predicate);
        ListMultimap<Id, ObjectVersion<M>> results = ArrayListMultimap.create();
        Revision latestRevision = null;

        for (Group versionAndParents : versionsAndParents) {
            Id id = versionAndParents.getOne(options.version.docId);
            latestRevision = versionAndParents.getOne(options.version.revision);
            Map<PropertyPath, Object> changeset = toChangeSet(properties.get(latestRevision));

            results.put(id, buildVersion(latestRevision, versionAndParents, changeset));
        }
        return new FetchResults<>(results, latestRevision);
    }

    protected Map<Revision, List<Tuple>> fetchProperties(BooleanExpression predicate) {
        SQLQuery qry = options.queryFactory
                .from(options.property)
                .innerJoin(options.version).on(options.version.revision.eq(options.property.revision))
                .where(predicate);

        return qry.transform(properties);
    }

    protected List<Group> fetchVersionsAndParents(BooleanExpression predicate, OrderSpecifier<?> orderBy) {
        return options.queryFactory
                .from(options.version)
                .leftJoin(options.parent).on(options.parent.revision.eq(options.version.revision))
                .where(predicate)
                .orderBy(orderBy)
                .transform(versionAndParents);
    }

    protected List<Group> verifyVersionsAndParentsSince(List<Group> versionsAndParents, Revision since) {
        if (versionsAndParents.isEmpty()) {
            throw new VersionNotFoundException(since);
        }
        if (versionsAndParents.size() == 1 && versionsAndParents.get(0).getOne(options.version.revision) == null) {
            return ImmutableList.of();
        }
        return versionsAndParents;
    }

    private void insertOptimizedParentsAndProperties(Id docId, ObjectVersionGraph<M> optimizedGraph, List<Revision> keptRevisions) {
        AbstractUpdateBatch<Id, M, V, ?> batch = optimizationUpdateBatch();
        for (Revision revision : keptRevisions) {
            VersionNode<PropertyPath, Object, M> version = optimizedGraph.getVersionNode(revision);
            batch.insertParents(docId, version);
            batch.insertProperties(docId, version);
        }
        batch.execute();
    }

    private void deleteOldParentsAndProperties(List<Revision> squashedRevisions, List<Revision> keptRevisions) {
        options.queryFactory.delete(options.parent)
                .where(options.parent.revision.in(keptRevisions).or(options.parent.revision.in(squashedRevisions)))
                .execute();
        options.queryFactory.delete(options.property)
                .where(options.property.revision.in(keptRevisions).or(options.property.revision.in(squashedRevisions)))
                .execute();
    }

    private void deleteSquashedVersions(List<Revision> squashedRevisions) {
        // Delete squashed versions
        options.queryFactory.delete(options.version)
                .where(options.version.revision.in(squashedRevisions))
                .execute();
    }

    protected ObjectVersion<M> buildVersion(Revision rev, Group versionAndParents, Map<PropertyPath, Object> changeset) {
        if (!options.versionTableProperties.isEmpty()) {
            if (changeset == null) {
                changeset = new HashMap<>();
            }
            for (Map.Entry<PropertyPath, Path<?>> entry : options.versionTableProperties.entrySet()) {
                PropertyPath path = entry.getKey();
                @SuppressWarnings("unchecked")
                Path<Object> column = (Path<Object>) entry.getValue();
                changeset.put(path, versionAndParents.getOne(column));
            }
        }
        return new ObjectVersionBuilder<M>(rev)
                .branch(versionAndParents.getOne(options.version.branch))
                .type(versionAndParents.getOne(options.version.type))
                .parents(versionAndParents.getSet(options.parent.parentRevision))
                .changeset(changeset)
                .meta(getMeta(versionAndParents))
                .build();
    }

    protected Map<PropertyPath, Object> toChangeSet(List<Tuple> properties) {
        if (properties == null) {
            return null;
        }
        Map<PropertyPath, Object> changeset = Maps.newHashMapWithExpectedSize(properties.size());
        for (Tuple tuple : properties) {
            PropertyPath path = PropertyPath.parse(tuple.get(options.property.path));
            Object value = getPropertyValue(path, tuple);
            changeset.put(path, value);
        }
        return changeset;
    }

    protected Object getPropertyValue(PropertyPath path, Tuple tuple) {
        String type = tuple.get(options.property.type);
        String str = tuple.get(options.property.str);
        Long nbr = tuple.get(options.property.nbr);

        switch (type.charAt(0)) {
            case 'O': return Persistent.object(str);
            case 'A': return Persistent.array();
            case 's': return str;
            case 'b': return nbr != 0;
            case 'l': return nbr;
            case 'd': return Double.longBitsToDouble(nbr);
            case 'D': return new BigDecimal(str);
            case 'n': return null;
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    protected static Expression<?>[] concat(Expression<?>[] expr1, Expression<?>... expr2) {
        Expression<?>[] expressions = new Expression<?>[expr1.length + expr2.length];
        arraycopy(expr1, 0, expressions, 0, expr1.length);
        arraycopy(expr2, 0, expressions, expr1.length, expr2.length);
        return expressions;
    }

    protected static Expression<?>[] without(Expression<?>[] expressions, Expression<?> expr) {
        List<Expression<?>> list = new ArrayList<>(asList(expressions));
        list.remove(expr);
        return list.toArray(new Expression<?>[list.size()]);
    }

}
