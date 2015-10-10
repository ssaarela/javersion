package org.javersion.store.jdbc;

import static com.mysema.query.group.GroupBy.groupBy;
import static com.mysema.query.support.Expressions.constant;
import static com.mysema.query.support.Expressions.predicate;
import static com.mysema.query.types.Ops.EQ;
import static com.mysema.query.types.Ops.IS_NULL;
import static com.mysema.query.types.PathMetadataFactory.forVariable;
import static java.lang.System.arraycopy;
import static org.javersion.store.jdbc.RevisionType.REVISION_TYPE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javersion.core.*;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionBuilder;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.javersion.util.Check;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.mysema.query.ResultTransformer;
import com.mysema.query.Tuple;
import com.mysema.query.group.Group;
import com.mysema.query.group.GroupBy;
import com.mysema.query.group.QPair;
import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.types.EnumByNameType;
import com.mysema.query.types.*;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.SimplePath;

public abstract class AbstractVersionStoreJdbc<Id, M, Options extends StoreOptions<Id>> {

    protected static class JSinceVersion  extends RelationalPathBase<JSinceVersion> {

        public final SimplePath<Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

        public final NumberPath<Long> ordinal = createNumber("ordinal", Long.class);

        public final NumberPath<Long> localOrdinal = createNumber("localOrdinal", Long.class);

        public JSinceVersion(String schema, String table) {
            super(JSinceVersion.class, forVariable("SINCE"), schema, table);
            addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
            addMetadata(ordinal, ColumnMetadata.named("ORDINAL").withIndex(3).ofType(Types.BIGINT).withSize(19));
            addMetadata(localOrdinal, ColumnMetadata.named("LOCAL_ORDINAL").withIndex(4).ofType(Types.BIGINT).withSize(19));
        }

    }

    public static void registerTypes(String tablePrefix, Configuration configuration) {
        configuration.register(tablePrefix + "VERSION", "TYPE", new EnumByNameType<>(VersionType.class));
        configuration.register(tablePrefix + "VERSION", "REVISION", REVISION_TYPE);

        configuration.register(tablePrefix + "VERSION_PARENT", "REVISION", REVISION_TYPE);
        configuration.register(tablePrefix + "VERSION_PARENT", "PARENT_REVISION", REVISION_TYPE);

        configuration.register(tablePrefix + "VERSION_PROPERTY", "REVISION", REVISION_TYPE);
    }


    protected final Options options;

    protected final JSinceVersion sinceVersion;

    protected final Expression<?>[] versionAndParents;

    protected final Expression<?>[] versionAndParentsSince;

    protected final QPair<Revision, Id> revisionAndDocId;

    protected final ResultTransformer<Map<Revision, List<Tuple>>> properties;

    protected final FetchResults<Id, M> noResults = new FetchResults<>();

    protected  AbstractVersionStoreJdbc() {
        options = null;
        versionAndParents = null;
        versionAndParentsSince = null;
        sinceVersion = null;
        revisionAndDocId = null;
        properties = null;
    }

    public AbstractVersionStoreJdbc(Options options) {
        this.options = options;
        versionAndParents = concat(options.version.all(), GroupBy.set(options.parent.parentRevision));
        sinceVersion = new JSinceVersion(options.version.getSchemaName(), options.version.getTableName());
        versionAndParentsSince = concat(versionAndParents, sinceVersion.ordinal);
        revisionAndDocId = new QPair<>(options.version.revision, options.version.docId);
        properties = groupBy(options.property.revision).as(GroupBy.list(new QTuple(options.property.all())));
    }


    static Expression<?>[] concat(Expression<?>[] expr1, Expression<?>... expr2) {
        Expression<?>[] expressions = new Expression<?>[expr1.length + expr2.length];
        arraycopy(expr1, 0, expressions, 0, expr1.length);
        arraycopy(expr2, 0, expressions, expr1.length, expr2.length);
        return expressions;
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

    private void insertOptimizedParentsAndProperties(Id docId, ObjectVersionGraph<M> optimizedGraph, List<Revision> keptRevisions) {
        DocumentUpdateBatch<Id, M> batch = updateBatch();
        for (Revision revision : keptRevisions) {
            VersionNode<PropertyPath, Object, M> version = optimizedGraph.getVersionNode(revision);
            batch.insertParents(docId, version);
            batch.insertProperties(docId, version);
        }
        batch.execute();
    }

    protected abstract DocumentUpdateBatch<Id, M> updateBatch();

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

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public ObjectVersionGraph<M> load(Id docId) {
        FetchResults<Id, M> results = fetch(publicVersionsOf(docId));
        return results.containsKey(docId) ? results.getVersionGraph(docId).get() : ObjectVersionGraph.init();
    }

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public FetchResults<Id, M> load(Collection<Id> docIds) {
        return fetch(publicVersionsOf(docIds));
    }

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public List<ObjectVersion<M>> fetchUpdates(Id docId, Revision since) {
        Predicate predicate = predicate(EQ, options.version.docId, constant(docId))
                .or(predicate(IS_NULL, options.version.docId));

        List<Group> versionsAndParents = versionsAndParentsSince(predicate, since);
        FetchResults<Id, M> results = fetch(versionsAndParents, predicate);
        return results.containsKey(docId) ? results.getVersions(docId).get() : ImmutableList.of();
    }

    protected abstract Predicate publicVersionsOf(Id docId);

    protected abstract Predicate publicVersionsOf(Collection<Id> docIds);

    protected FetchResults<Id, M> fetch(Predicate predicate) {
        Check.notNull(predicate, "predicate");

        List<Group> versionsAndParents = getVersionsAndParents(predicate);
        if (versionsAndParents.isEmpty()) {
            return noResults;
        }
        return fetch(versionsAndParents, predicate);
    }

    protected FetchResults<Id, M> fetch(List<Group> versionsAndParents, Predicate predicate) {
        Map<Revision, List<Tuple>> properties = getPropertiesByDocId(predicate);
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

    protected SubQueryExpression<Long> getOrdinal(Revision revision) {
        return options.queryFactory
                .subQuery(options.version)
                .where(options.version.revision.eq(revision))
                .unique(options.version.ordinal);
    }

    protected Map<Revision, List<Tuple>> getPropertiesByDocId(Predicate predicate) {
        SQLQuery qry = options.queryFactory
                .from(options.property)
                .innerJoin(options.version).on(options.version.revision.eq(options.property.revision))
                .where(predicate);

        return qry.transform(properties);
    }

    protected List<Group> getVersionsAndParents(Predicate predicate) {
        SQLQuery qry = options.queryFactory
                .from(options.version)
                .leftJoin(options.parent).on(options.parent.revision.eq(options.version.revision))
                .where(predicate)
                .orderBy(publicVersionsOrderBy());

        return qry.transform(groupBy(options.version.revision).list(versionAndParents));
    }

    protected List<Group> versionsAndParentsSince(Predicate predicate, Revision since) {
        SQLQuery qry = options.queryFactory
                .from(sinceVersion)
                .leftJoin(options.version).on(options.version.ordinal.gt(sinceVersion.ordinal))
                .leftJoin(options.parent).on(options.parent.revision.eq(options.version.revision))
                .where(sinceVersion.revision.eq(since), predicate)
                .orderBy(publicVersionsOrderBy());

        return verifyUpdateResults(qry.transform(groupBy(options.version.revision).list(versionAndParents)), since);
    }

    protected List<Group> verifyUpdateResults(List<Group> versionsAndParents, Revision since) {
        if (versionsAndParents.isEmpty()) {
            throw new VersionNotFoundException(since);
        }
        if (versionsAndParents.size() == 1 && versionsAndParents.get(0).getOne(options.version.revision) == null) {
            return ImmutableList.of();
        }
        return versionsAndParents;
    }

    protected abstract OrderSpecifier<?>[] publicVersionsOrderBy();

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
}
