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
import static com.mysema.query.support.Expressions.constant;
import static com.mysema.query.support.Expressions.predicate;
import static com.mysema.query.types.Ops.EQ;
import static com.mysema.query.types.Ops.IN;
import static java.lang.System.arraycopy;
import static java.util.Collections.singleton;
import static java.util.Map.Entry;
import static org.javersion.store.jdbc.RevisionType.REVISION_TYPE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javersion.core.OptimizedGraphBuilder;
import org.javersion.core.Persistent;
import org.javersion.core.Revision;
import org.javersion.core.VersionNode;
import org.javersion.core.VersionType;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionBuilder;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.javersion.util.Check;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.*;
import com.mysema.query.ResultTransformer;
import com.mysema.query.Tuple;
import com.mysema.query.group.Group;
import com.mysema.query.group.GroupBy;
import com.mysema.query.group.QPair;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLQueryFactory;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.sql.types.EnumByNameType;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.SubQueryExpression;
import com.mysema.query.types.expr.SimpleExpression;

public class DocumentVersionStoreJdbc<Id, M> {

    public static void registerTypes(String tablePrefix, Configuration configuration) {
        configuration.register(tablePrefix + "VERSION", "TYPE", new EnumByNameType<>(VersionType.class));
        configuration.register(tablePrefix + "VERSION", "REVISION", REVISION_TYPE);

        configuration.register(tablePrefix + "VERSION_PARENT", "REVISION", REVISION_TYPE);
        configuration.register(tablePrefix + "VERSION_PARENT", "PARENT_REVISION", REVISION_TYPE);

        configuration.register(tablePrefix + "VERSION_PROPERTY", "REVISION", REVISION_TYPE);
    }

    protected final Expression<Long> nextOrdinal;

    protected final JVersion<Id> jVersion;

    protected final JVersionParent jParent;

    protected final JVersionProperty jProperty;

    protected final JRepository jRepository;

    protected final SQLQueryFactory queryFactory;

    protected final Expression<?>[] versionAndParents;

    protected final QPair<Revision, Id> revisionAndDocId;

    protected final ResultTransformer<Map<Revision, List<Tuple>>> properties;

    protected static final String REPOSITORY_ID = "repository";

    protected final Map<PropertyPath, Path<?>> versionTableProperties;

    protected final FetchResults<Id, M> noResults = new FetchResults<>();

    @SuppressWarnings("unused")
    protected DocumentVersionStoreJdbc() {
        nextOrdinal = null;
        jVersion = null;
        jParent = null;
        jProperty = null;
        jRepository = null;

        versionTableProperties = null;
        versionAndParents = null;
        revisionAndDocId = null;
        properties = null;
        queryFactory = null;
    }

    public <P extends SimpleExpression<Id> & Path<Id>> DocumentVersionStoreJdbc(
            JRepository jRepository,
            Expression<Long> nextOrdinal,
            JVersion<Id> jVersion,
            JVersionParent jParent,
            JVersionProperty jProperty,
            SQLQueryFactory queryFactory) {
        this(jRepository, nextOrdinal, jVersion, jParent, jProperty, queryFactory, ImmutableMap.of());
    }

    public <P extends SimpleExpression<Id> & Path<Id>> DocumentVersionStoreJdbc(
            JRepository jRepository,
            Expression<Long> nextOrdinal,
            JVersion<Id> jVersion,
            JVersionParent jParent,
            JVersionProperty jProperty,
            SQLQueryFactory queryFactory,
            Map<PropertyPath, Path<?>> versionTableProperties) {
        this.nextOrdinal = nextOrdinal;
        this.jRepository = jRepository;
        this.jVersion = jVersion;
        this.jParent = jParent;
        this.jProperty = jProperty;
        this.queryFactory = queryFactory;

        this.versionTableProperties = ImmutableMap.copyOf(versionTableProperties);
        versionAndParents = concat(jVersion.all(), GroupBy.set(jParent.parentRevision));
        revisionAndDocId = new QPair<>(jVersion.revision, jVersion.docId);
        properties = groupBy(jProperty.revision).as(GroupBy.list(new QTuple(jProperty.all())));
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
        SQLInsertClause versionBatch = queryFactory.insert(jVersion);
        SQLInsertClause parentBatch = queryFactory.insert(jParent);
        SQLInsertClause propertyBatch = queryFactory.insert(jProperty);

        for (Id docId : versionsByDocId.keySet()) {
            for (VersionNode<PropertyPath, Object, M> version : versionsByDocId.get(docId)) {
                addVersion(docId, version, versionBatch);
                addParents(docId, version, parentBatch);
                addProperties(docId, version, propertyBatch);
            }
        }

        if (!versionBatch.isEmpty()) {
            versionBatch.execute();
        }
        if (!parentBatch.isEmpty()) {
            parentBatch.execute();
        }
        if (!propertyBatch.isEmpty()) {
            propertyBatch.execute();
        }
    }

    /**
     * NOTE: publish() needs to be called in a separate transaction from append()!
     * E.g. asynchronously in TransactionSynchronization.afterCommit.
     *
     * Calling publish() in the same transaction with append() severely limits concurrency
     * and might end up in deadlock.
     *
     * @return
     */
    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public Multimap<Id, Revision> publish() {
        // Lock repository with select for update
        long lastOrdinal = getLastOrdinalForUpdate();

        Map<Revision, Id> uncommittedRevisions = findUncommittedRevisions();
        if (uncommittedRevisions.isEmpty()) {
            return ImmutableMultimap.of();
        }

        Multimap<Id, Revision> publishedDocs = ArrayListMultimap.create();

        SQLUpdateClause versionUpdateBatch = queryFactory.update(jVersion);

        for (Map.Entry<Revision, Id> entry : uncommittedRevisions.entrySet()) {
            Revision revision = entry.getKey();
            Id docId = entry.getValue();
            publishedDocs.put(docId, revision);
            versionUpdateBatch
                    .set(jVersion.ordinal, ++lastOrdinal)
                    .setNull(jVersion.txOrdinal)
                    .where(jVersion.revision.eq(revision))
                    .addBatch();
        }

        versionUpdateBatch.execute();

        queryFactory
                .update(jRepository)
                .set(jRepository.ordinal, lastOrdinal)
                .where(jRepository.id.eq(REPOSITORY_ID))
                .execute();

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

    private void insertOptimizedParentsAndProperties(Id docId, ObjectVersionGraph<M> optimizedGraph, List<Revision> keptRevisions) {;
        SQLInsertClause parentBatch = queryFactory.insert(jParent);
        SQLInsertClause propertyBatch = queryFactory.insert(jProperty);

        for (Revision revision : keptRevisions) {
            VersionNode<PropertyPath, Object, M> version = optimizedGraph.getVersionNode(revision);
            addParents(docId, version, parentBatch);
            addProperties(docId, version, propertyBatch);
        }

        if (!parentBatch.isEmpty()) {
            parentBatch.execute();
        }
        if (!propertyBatch.isEmpty()) {
            propertyBatch.execute();
        }
    }

    private void deleteOldParentsAndProperties(List<Revision> squashedRevisions, List<Revision> keptRevisions) {
        queryFactory.delete(jParent)
                .where(jParent.revision.in(keptRevisions).or(jParent.revision.in(squashedRevisions)))
                .execute();
        queryFactory.delete(jProperty)
                .where(jProperty.revision.in(keptRevisions).or(jProperty.revision.in(squashedRevisions)))
                .execute();
    }

    private void deleteSquashedVersions(List<Revision> squashedRevisions) {
        // Delete squashed versions
        queryFactory.delete(jVersion)
                .where(jVersion.revision.in(squashedRevisions))
                .execute();
    }

    protected void afterPublish(Multimap<Id, Revision> publishedDocs) {
        // After publish hook for sub classes to override
    }

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public ObjectVersionGraph<M> load(Id docId) {
        FetchResults<Id, M> results = fetch(predicate(EQ, jVersion.docId, constant(docId)));
        return results.containsKey(docId) ? results.getVersionGraph(docId).get() : ObjectVersionGraph.init();
    }

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public FetchResults<Id, M> load(Collection<Id> docIds) {
        return fetch(predicate(IN, constant(docIds)));
    }

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public List<ObjectVersion<M>> fetchUpdates(Id docId, Revision since) {
        FetchResults<Id, M> results = fetch(predicate(EQ, jVersion.docId, constant(docId)).and(jVersion.ordinal.gt(getOrdinal(since))));
        return results.containsKey(docId) ? results.getVersions(docId).get() : ImmutableList.of();
    }

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public FetchResults<Id, M> fetchUpdates(Collection<Id> docIds, Revision since) {
        return fetch(predicate(IN, jVersion.docId, constant(docIds)).and(jVersion.ordinal.gt(getOrdinal(since))));
    }

    private FetchResults<Id, M> fetch(Predicate predicate) {
        Check.notNull(predicate, "predicate");

        List<Group> versionsAndParents = getVersionsAndParents(predicate);
        if (versionsAndParents.isEmpty()) {
            return noResults;
        }

        ListMultimap<Id, ObjectVersion<M>> results = ArrayListMultimap.create();
        Map<Revision, List<Tuple>> properties = getPropertiesByDocId(predicate);
        Revision latestRevision = null;

        for (Group versionAndParents : versionsAndParents) {
            Id id = versionAndParents.getOne(jVersion.docId);
            latestRevision = versionAndParents.getOne(jVersion.revision);
            Map<PropertyPath, Object> changeset = toChangeSet(properties.get(latestRevision));

            results.put(id, buildVersion(latestRevision, versionAndParents, changeset));
        }
        return new FetchResults<>(results, latestRevision);
    }

    protected SubQueryExpression<Long> getOrdinal(Revision revision) {
        return queryFactory
                .subQuery(jVersion)
                .where(jVersion.revision.eq(revision))
                .unique(jVersion.ordinal);
    }

    private Long getLastOrdinalForUpdate() {
        return queryFactory
                .from(jRepository)
                .where(jRepository.id.eq(REPOSITORY_ID))
                .forUpdate()
                .singleResult(jRepository.ordinal);
    }

    private Map<Revision, Id> findUncommittedRevisions() {
        return queryFactory
                .from(jVersion)
                .where(jVersion.txOrdinal.isNotNull())
                .orderBy(jVersion.txOrdinal.asc())
                .map(jVersion.revision, jVersion.docId);
    }

    protected void addProperties(Id docId, VersionNode<PropertyPath, Object, M> version, SQLInsertClause propertyBatch) {
        addProperties(docId, version.revision, version.getChangeset(), propertyBatch);
    }

    protected void addProperties(Id docId, Revision revision, Map<PropertyPath, Object> changeset, SQLInsertClause propertyBatch) {
        for (Entry<PropertyPath, Object> entry : changeset.entrySet()) {
            if (!versionTableProperties.containsKey(entry.getKey())) {
                propertyBatch
                        .set(jProperty.revision, revision)
                        .set(jProperty.path, entry.getKey().toString());
                setValue(entry.getKey(), entry.getValue(), propertyBatch);
                propertyBatch.addBatch();
            }
        }
    }

    protected void setValue(PropertyPath path, Object value, SQLInsertClause propertyBatch) {
        // type:
        // n=null, O=object, A=array, s=string,
        // b=boolean, l=long, d=double, D=bigdecimal
        char type;
        String str = null;
        Long nbr = null;
        switch (Persistent.Type.of(value)) {
            case NULL:
                type = 'n';
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
                nbr = ((Boolean) value) ? 1l : 0l;
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
                .set(jProperty.type, Character.toString(type))
                .set(jProperty.str, str)
                .set(jProperty.nbr, nbr);
    }

    protected void addParents(Id docId, VersionNode<PropertyPath, Object, M> version, SQLInsertClause parentBatch) {
        for (Revision parentRevision : version.parentRevisions) {
            parentBatch
                    .set(jParent.revision, version.revision)
                    .set(jParent.parentRevision, parentRevision)
                    .addBatch();
        }
    }

    protected void addVersion(Id docId, VersionNode<PropertyPath, Object, M> version, SQLInsertClause versionBatch) {
        versionBatch
                .set(jVersion.docId, docId)
                .set(jVersion.revision, version.revision)
                .set(jVersion.txOrdinal, nextOrdinal)
                .set(jVersion.type, version.type)
                .set(jVersion.branch, version.branch);

        if (!versionTableProperties.isEmpty()) {
            Map<PropertyPath, Object> properties = version.getProperties();
            for (Entry<PropertyPath, Path<?>> entry : versionTableProperties.entrySet()) {
                PropertyPath path = entry.getKey();
                @SuppressWarnings("unchecked")
                Path<Object> column = (Path<Object>) entry.getValue();
                versionBatch.set(column, properties.get(path));
            }
        }
        versionBatch.addBatch();
    }

    protected ObjectVersion<M> buildVersion(Revision rev, Group versionAndParents, Map<PropertyPath, Object> changeset) {
        if (!versionTableProperties.isEmpty()) {
            if (changeset == null) {
                changeset = new HashMap<>();
            }
            for (Entry<PropertyPath, Path<?>> entry : versionTableProperties.entrySet()) {
                PropertyPath path = entry.getKey();
                @SuppressWarnings("unchecked")
                Path<Object> column = (Path<Object>) entry.getValue();
                changeset.put(path, versionAndParents.getOne(column));
            }
        }
        return new ObjectVersionBuilder<M>(rev)
                .branch(versionAndParents.getOne(jVersion.branch))
                .type(versionAndParents.getOne(jVersion.type))
                .parents(versionAndParents.getSet(jParent.parentRevision))
                .changeset(changeset)
                .build();
    }

    protected Map<Revision, List<Tuple>> getPropertiesByDocId(Predicate predicate) {
        SQLQuery qry = queryFactory
                .from(jProperty)
                .innerJoin(jVersion).on(jVersion.revision.eq(jProperty.revision))
                .where(jVersion.ordinal.isNotNull().and(predicate));

        return qry.transform(properties);
    }

    protected List<Group> getVersionsAndParents(Predicate predicate) {
        SQLQuery qry = queryFactory
                .from(jVersion)
                .leftJoin(jParent).on(jParent.revision.eq(jVersion.revision))
                .where(jVersion.ordinal.isNotNull().and(predicate))
                .orderBy(jVersion.ordinal.asc());

        return qry.transform(groupBy(jVersion.revision).list(versionAndParents));
    }

    protected Map<PropertyPath, Object> toChangeSet(List<Tuple> properties) {
        if (properties == null) {
            return null;
        }
        Map<PropertyPath, Object> changeset = Maps.newHashMapWithExpectedSize(properties.size());
        for (Tuple tuple : properties) {
            PropertyPath path = PropertyPath.parse(tuple.get(jProperty.path));
            Object value = getPropertyValue(path, tuple);
            changeset.put(path, value);
        }
        return changeset;
    }

    protected Object getPropertyValue(PropertyPath path, Tuple tuple) {
        String type = tuple.get(jProperty.type);
        String str = tuple.get(jProperty.str);
        Long nbr = tuple.get(jProperty.nbr);

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

    private static Expression<?>[] concat(Expression<?>[] expr1, Expression<?>... expr2) {
        Expression<?>[] expressions = new Expression<?>[expr1.length + expr2.length];
        arraycopy(expr1, 0, expressions, 0, expr1.length);
        arraycopy(expr2, 0, expressions, expr1.length, expr2.length);
        return expressions;
    }

}
