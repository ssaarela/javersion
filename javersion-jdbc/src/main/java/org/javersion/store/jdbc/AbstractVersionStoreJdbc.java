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
import static com.querydsl.core.types.Ops.EQ;
import static com.querydsl.core.types.Ops.IN;
import static com.querydsl.core.types.Projections.tuple;
import static com.querydsl.core.types.dsl.Expressions.constant;
import static com.querydsl.core.types.dsl.Expressions.predicate;
import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.synchronizedSet;
import static org.javersion.store.jdbc.RevisionType.REVISION_TYPE;
import static org.javersion.store.jdbc.VersionStatus.ACTIVE;
import static org.javersion.store.jdbc.VersionStatus.REDUNDANT;
import static org.javersion.store.jdbc.VersionStatus.SQUASHED;

import java.math.BigDecimal;
import java.util.*;
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
import org.javersion.util.Check;

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
import com.querydsl.core.types.dsl.BooleanOperation;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.types.EnumByNameType;
import com.querydsl.sql.types.EnumByOrdinalType;

public abstract class AbstractVersionStoreJdbc<Id, M, V extends JVersion<Id>,
        Batch extends AbstractUpdateBatch<Id, M, V, Options, Batch>,
        Options extends StoreOptions<Id, M, V>>
        implements VersionStore<Id, M> {

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

    protected final ResultTransformer<Map<Revision, List<Tuple>>> properties;

    protected final FetchResults<Id, M> noResults = new FetchResults<>();

    protected final Set<Id> runningOptimizations = synchronizedSet(new HashSet<Id>());

    public AbstractVersionStoreJdbc(Options options) {
        this.options = options;

        versionAndParentColumns = without(concat(options.version.all(), GroupBy.set(options.parent.parentRevision)), options.version.revision);
        versionAndParents = groupBy(options.version.revision).list(versionAndParentColumns);

        revisionAndDocId = new QPair<>(options.version.revision, options.version.docId);

        Expression<?>[] propertyColumns = without(options.property.all(), options.property.revision);
        properties = groupBy(options.property.revision).as(GroupBy.list(tuple(propertyColumns)));
    }

    @Override
    public final ObjectVersionGraph<M> load(Id docId) {
        return options.transactions.readOnly(() -> doLoad(docId));
    }

    @Override
    public final ObjectVersionGraph<M> loadOptimized(Id docId) {
        return options.transactions.readOnly(() -> doLoadOptimized(docId));
    }

    @Override
    public final GraphResults<Id, M> load(Collection<Id> docIds) {
        return options.transactions.readOnly(() -> doLoad(docIds));
    }

    @Override
    public final List<ObjectVersion<M>> fetchUpdates(Id docId, Revision since) {
        return options.transactions.readOnly(() -> doFetchUpdates(docId, since));
    }

    /**
     * NOTE: publish() needs to be called in a separate transaction from append()!
     * E.g. (a)synchronously from TransactionSynchronization.afterCommit.
     *
     * Calling publish() in the same transaction with append() severely limits concurrency
     * and might end up in deadlock.
     */
    @Override
    public final Multimap<Id, Revision> publish() {
        return options.transactions.writeRequired(this::doPublish);
    }

    @Override
    public final void prune(Id docId, Function<ObjectVersionGraph<M>, Predicate<VersionNode<PropertyPath, Object, M>>> keep) {
        options.transactions.writeRequired(() -> {
            doPrune(docId, keep);
            return null;
        });
    }

    @Override
    public final void reset(Id docId) {
        options.transactions.writeRequired(() -> {
            lockAndReset(docId);
            return null;
        });
    }

    @Override
    public Batch updateBatch(Id id) {
        return updateBatch(singleton(id));
    }

    @Override
    public abstract Batch updateBatch(Collection<Id> ids);

    protected abstract FetchResults<Id, M> doLoad(Id docId, boolean optimized);

    protected abstract List<ObjectVersion<M>> doFetchUpdates(Id docId, Revision since);

    protected abstract SQLUpdateClause setOrdinal(SQLUpdateClause versionUpdateBatch, long ordinal);

    protected abstract Map<Revision, Id> getUnpublishedRevisionsForUpdate();

    protected final ObjectVersionGraph<M> doLoad(Id docId) {
        FetchResults<Id, M> results = doLoad(docId, false);
        return results.containsKey(docId) ? results.getVersionGraph(docId) : ObjectVersionGraph.init();
    }

    protected final ObjectVersionGraph<M> doLoadOptimized(Id docId) {
        return toVersionGraph(docId, doLoad(docId, true));
    }

    protected GraphResults<Id, M> doLoad(Collection<Id> docIds) {
        Check.notNull(docIds, "docIds");
        final boolean optimized = true;

        BooleanExpression predicate =
                predicate(IN, options.version.docId, constant(docIds))
                        .and(options.version.ordinal.isNotNull());

        List<Group> versionsAndParents = fetchVersionsAndParents(optimized, predicate,
                options.version.ordinal.asc());

        return toGraphResults(fetch(versionsAndParents, optimized, predicate));
    }

    protected GraphResults<Id, M> toGraphResults(FetchResults<Id, M> fetchResults) {
        ImmutableMap.Builder<Id, ObjectVersionGraph<M>> graphs = ImmutableMap.builder();
        for (Id docId : fetchResults.getDocIds()) {
            graphs.put(docId, toVersionGraph(docId, fetchResults));
        }
        return new GraphResults<>(graphs.build(), fetchResults.latestRevision);
    }

    protected ObjectVersionGraph<M> toVersionGraph(Id docId, FetchResults<Id, M> fetchResults) {
        if (fetchResults.containsKey(docId)) {
            ObjectVersionGraph<M> graph;
            try {
                graph = ObjectVersionGraph.init(fetchResults.getVersions(docId));
                if (options.graphOptions.optimizeWhen.test(graph)) {
                    optimizeAsync(docId, graph, false);
                }
            } catch (VersionNotFoundException e) {
                graph = doLoad(docId);
                optimizeAsync(docId, graph, true);
            }
            return graph;
        } else {
            return ObjectVersionGraph.init();
        }
    }

    protected Multimap<Id, Revision> doPublish() {
        Map<Revision, Id> uncommittedRevisions = getUnpublishedRevisionsForUpdate();
        long lastOrdinal = getMaxOrdinal();

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

    protected void doPrune(Id docId, Function<ObjectVersionGraph<M>, Predicate<VersionNode<PropertyPath, Object, M>>> keep) {
        lockForMaintenance(docId);
        doReset(docId);
        ObjectVersionGraph<M> graph = doLoad(docId);
        updateBatch(ImmutableSet.of())
                .prune(graph, keep.apply(graph))
                .execute();
    }

    protected void optimizeAsync(Id docId, ObjectVersionGraph<M> baseGraph, boolean reset) {
        if (runningOptimizations.add(docId)) {
            options.executor.execute(() -> {
                try {
                    options.transactions.writeNewRequired(() -> {
                        optimize(docId, baseGraph, reset);
                        return null;
                    });
                } finally {
                    runningOptimizations.remove(docId);
                }
            });
        }
    }

    protected void optimize(Id docId, ObjectVersionGraph<M> graph, boolean reset) {
        lockForMaintenance(docId);
        if (reset) {
            long revived = doReset(docId);
            if (revived == 0) {
                throw new ConcurrentMaintenanceException("Expected to revive some versions");
            }
        }
        updateBatch(ImmutableSet.of())
                .optimize(graph, options.graphOptions.optimizeKeep.apply(graph))
                .execute();
    }

    protected abstract void lockForMaintenance(Id docId);

    protected void lockAndReset(Id docId) {
        lockForMaintenance(docId);
        doReset(docId);
    }

    protected long doReset(Id docId) {
        final BooleanOperation docIdEquals = predicate(EQ, options.version.docId, constant(docId));

        final SQLQuery<Revision> docRevisions = options.queryFactory
                .select(options.version.revision)
                .from(options.version)
                .where(docIdEquals);

        // Revive squashed versions
        long revived = options.queryFactory
                .update(options.version)
                .set(options.version.status, ACTIVE)
                .where(docIdEquals, options.version.status.eq(SQUASHED))
                .execute();

        // Delete redundant parents
        options.queryFactory
                .delete(options.parent)
                .where(options.parent.revision.in(docRevisions), options.parent.status.eq(REDUNDANT))
                .execute();

        // Revive squashed parents
        options.queryFactory
                .update(options.parent)
                .set(options.parent.status, ACTIVE)
                .where(options.parent.revision.in(docRevisions), options.parent.status.eq(SQUASHED))
                .execute();

        // Delete redundant properties
        options.queryFactory
                .delete(options.property)
                .where(options.property.revision.in(docRevisions), options.property.status.eq(REDUNDANT))
                .execute();

        // Revive squashed properties
        options.queryFactory
                .update(options.property)
                .set(options.property.status, ACTIVE)
                .where(options.property.revision.in(docRevisions), options.property.status.eq(SQUASHED))
                .execute();

        return revived;
    }

    protected M getMeta(Group versionAndParents) {
        return null;
    }

    @SuppressWarnings("unused")
    protected void afterPublish(Multimap<Id, Revision> publishedDocs) {
        // After publish hook for sub classes to override
    }

    protected long getMaxOrdinal() {
        Long maxOrdinal = options.queryFactory
                .select(options.version.ordinal.max())
                .from(options.version)
                .fetchFirst();
        return maxOrdinal != null ? maxOrdinal : 0;
    }

    protected FetchResults<Id, M> fetch(List<Group> versionsAndParents, boolean optimized, BooleanExpression predicate) {
        if (versionsAndParents.isEmpty()) {
            return noResults;
        }

        Map<Revision, List<Tuple>> properties = fetchProperties(optimized, predicate);
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

    protected Map<Revision, List<Tuple>> fetchProperties(boolean optimized, BooleanExpression predicate) {
        SQLQuery<?> qry = options.queryFactory
                .from(options.property)
                .where(predicate);

        if (optimized) {
            qry.innerJoin(options.version).on(
                    options.version.revision.eq(options.property.revision),
                    options.version.status.goe(ACTIVE));
            qry.where(options.property.status.goe(ACTIVE));
        } else {
            qry.innerJoin(options.version).on(options.version.revision.eq(options.property.revision));
            qry.where(options.property.status.loe(ACTIVE));
        }
        return qry.transform(properties);
    }

    protected List<Group> fetchVersionsAndParents(boolean optimized, BooleanExpression predicate, OrderSpecifier<?> orderBy) {
        SQLQuery<?> qry = options.queryFactory
                .from(options.version)
                .where(predicate)
                .orderBy(orderBy);

        if (optimized) {
            qry.leftJoin(options.parent).on(options.parent.revision.eq(options.version.revision), options.parent.status.goe(ACTIVE));
            qry.where(options.version.status.goe(ACTIVE));
        } else {
            qry.leftJoin(options.parent).on(options.parent.revision.eq(options.version.revision), options.parent.status.loe(ACTIVE));
            qry.where(options.version.status.loe(ACTIVE));
        }

        return qry.transform(versionAndParents);
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
