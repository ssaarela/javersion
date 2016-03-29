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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.types.Projections.tuple;
import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.javersion.store.jdbc.RevisionType.REVISION_TYPE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import org.javersion.core.Persistent;
import org.javersion.core.Revision;
import org.javersion.core.VersionNode;
import org.javersion.core.VersionNotFoundException;
import org.javersion.core.VersionType;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.*;
import com.querydsl.core.ResultTransformer;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.Group;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.group.QPair;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.types.EnumByNameType;
import com.querydsl.sql.types.EnumByOrdinalType;

public abstract class AbstractVersionStoreJdbc<Id, M, V extends JVersion<Id>, Options extends StoreOptions<Id, V>> {

    public static EnumByOrdinalType<VersionStatus> VERSION_STATUS_TYPE = new EnumByOrdinalType<>(VersionStatus.class);

    public static void registerTypes(String tablePrefix, Configuration configuration) {
        configuration.register(tablePrefix + "VERSION", "TYPE", new EnumByNameType<>(VersionType.class));
        configuration.register(tablePrefix + "VERSION", "REVISION", REVISION_TYPE);
        configuration.register(tablePrefix + "VERSION", "STATUS", VERSION_STATUS_TYPE);

        configuration.register(tablePrefix + "VERSION_PARENT", "REVISION", REVISION_TYPE);
        configuration.register(tablePrefix + "VERSION_PARENT", "PARENT_REVISION", REVISION_TYPE);
        configuration.register(tablePrefix + "VERSION_PARENT", "STATUS", VERSION_STATUS_TYPE);

        configuration.register(tablePrefix + "VERSION_PROPERTY", "REVISION", REVISION_TYPE);
        configuration.register(tablePrefix + "VERSION_PROPERTY", "STATUS", VERSION_STATUS_TYPE);
    }


    protected final Options options;

    protected final Expression<?>[] versionAndParentColumns;

    protected final ResultTransformer<List<Group>> versionAndParents;

    protected final QPair<Revision, Id> revisionAndDocId;

    protected final SQLQuery<Long> maxOrdinalSubQuery;

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
        properties = groupBy(options.property.revision).as(GroupBy.list(tuple(propertyColumns)));
    }

    public abstract ObjectVersionGraph<M> load(Id docId);

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public abstract FetchResults<Id, M> load(Collection<Id> docIds);

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public abstract List<ObjectVersion<M>> fetchUpdates(Id docId, Revision since);

    /**
     * NOTE: publish() needs to be called in a separate transaction from append()!
     * E.g. (a)synchronously from TransactionSynchronization.afterCommit.
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
    public void prune(Id docId, Function<ObjectVersionGraph<M>, Predicate<VersionNode<PropertyPath, Object, M>>> keep) {
        AbstractUpdateBatch<Id, M, V, Options> batch = updateBatch(singletonList(docId));
        ObjectVersionGraph<M> graph = load(docId);
        batch.prune(graph, keep.apply(graph));
        batch.execute();
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public void optimize(Id docId, Function<ObjectVersionGraph<M>, Predicate<VersionNode<PropertyPath, Object, M>>> keep) {
        AbstractUpdateBatch<Id, M, V, Options> batch = updateBatch(singletonList(docId));
        ObjectVersionGraph<M> graph = load(docId);
        batch.optimize(graph, keep.apply(graph));
        batch.execute();
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = MANDATORY)
    public abstract AbstractUpdateBatch<Id, M, V, Options> updateBatch(Collection<Id> ids);

    protected abstract SQLUpdateClause setOrdinal(SQLUpdateClause versionUpdateBatch, long ordinal);

    protected abstract Map<Revision, Id> findUnpublishedRevisions();

    protected M getMeta(Group versionAndParents) {
        return null;
    }

    @SuppressWarnings("unused")
    protected void afterPublish(Multimap<Id, Revision> publishedDocs) {
        // After publish hook for sub classes to override
    }

    protected long lockRepositoryAndGetMaxOrdinal() {
        // Use List-result as a safe-guard against missing repository row
        List<Long> results = options.queryFactory
                .select(maxOrdinalSubQuery)
                .from(options.repository)
                .where(options.repository.id.eq(options.repositoryId))
                .forUpdate()
                .fetch();

        if (results.isEmpty()) {
            throw new IllegalStateException("Repository with id " + options.repositoryId + " not found from " + options.repository.getTableName());
        }
        Long maxOrdinal = results.get(0);
        return maxOrdinal != null ? maxOrdinal : 0;
    }

    private SQLQuery<Long> maxOrdinalSubQuery(Options options) {
        return options.queryFactory.select(options.version.ordinal.max()).from(options.version);
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
        return options.queryFactory
                .from(options.property)
                .innerJoin(options.version).on(options.version.revision.eq(options.property.revision))
                .where(predicate)
                .transform(properties);
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
        return new ObjectVersion.Builder<M>(rev)
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

    @SuppressWarnings("unused")
    protected Object getPropertyValue(PropertyPath path, Tuple tuple) {
        String type = firstNonNull(tuple.get(options.property.type), "N");
        String str = tuple.get(options.property.str);
        Long nbr = tuple.get(options.property.nbr);

        switch (type.charAt(0)) {
            case 'O': return Persistent.object(str);
            case 'A': return Persistent.array();
            case 's': return str;
            case 'b': return nbr != null ? nbr != 0 : null;
            case 'l': return nbr;
            case 'd': return nbr != null ? Double.longBitsToDouble(nbr) : null;
            case 'D': return !isNullOrEmpty(str) ? new BigDecimal(str) : null;
            case 'N': return Persistent.NULL;
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

    @Nonnull
    protected static Expression<?>[] without(Expression<?>[] expressions, Expression<?> expr) {
        List<Expression<?>> list = new ArrayList<>(asList(expressions));
        list.remove(expr);
        return list.toArray(new Expression<?>[list.size()]);
    }

}
