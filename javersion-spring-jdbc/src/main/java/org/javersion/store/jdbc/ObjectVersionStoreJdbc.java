package org.javersion.store.jdbc;

import static com.mysema.query.group.GroupBy.groupBy;
import static java.lang.System.arraycopy;
import static java.util.Collections.singleton;
import static java.util.Map.Entry;
import static org.javersion.store.jdbc.RevisionType.REVISION_TYPE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mysema.query.ResultTransformer;
import com.mysema.query.Tuple;
import com.mysema.query.group.Group;
import com.mysema.query.group.GroupBy;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.SQLExpressions;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLQueryFactory;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.types.EnumByNameType;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Path;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.expr.SimpleExpression;

public abstract class ObjectVersionStoreJdbc<Id, M> {

    public static void registerTypes(String tablePrefix, Configuration configuration) {
        configuration.register(tablePrefix + "VERSION", "TYPE", new EnumByNameType<>(VersionType.class));
        configuration.register(tablePrefix + "VERSION", "REVISION", REVISION_TYPE);

        configuration.register(tablePrefix + "VERSION_PARENT", "REVISION", REVISION_TYPE);
        configuration.register(tablePrefix + "VERSION_PARENT", "PARENT_REVISION", REVISION_TYPE);

        configuration.register(tablePrefix + "VERSION_PROPERTY", "REVISION", REVISION_TYPE);
    }

    protected final Expression<Long> nextOrdinal;

    protected final JVersion jVersion;

    protected final JVersionParent jParent;

    protected final JVersionProperty jProperty;

    protected final JRepository jRepository;

    protected final SQLQueryFactory queryFactory;

    protected final Expression<?>[] versionAndParents;

    protected final ResultTransformer<Map<Revision, List<Tuple>>> properties;

    protected static final String REPOSITORY_ID = "repository";

    @SuppressWarnings("unused")
    protected ObjectVersionStoreJdbc() {
        nextOrdinal = null;
        jVersion = null;
        jParent = null;
        jProperty = null;
        jRepository = null;
        versionAndParents = null;
        properties = null;
        queryFactory = null;
    }

    public ObjectVersionStoreJdbc(String schema, String tablePrefix, SQLQueryFactory queryFactory) {
        this.nextOrdinal = SQLExpressions.nextval(tablePrefix + "VERSION_ORDINAL_SEQ");
        this.jRepository = new JRepository(schema, tablePrefix, "repository");
        this.jVersion = new JVersion(schema, tablePrefix, "version");
        this.jParent = new JVersionParent(schema, tablePrefix, "parent");
        this.jProperty = new JVersionProperty(schema, tablePrefix, "property");
        this.queryFactory = queryFactory;

        versionAndParents = concat(allVersionColumns(), GroupBy.set(jParent.parentRevision));
        properties = groupBy(jProperty.revision).as(GroupBy.list(new QTuple(allPropertyColumns())));

        initIdColumns(jVersion, jProperty);
    }

    protected abstract void initIdColumns(JVersion jVersion, JVersionProperty jProperty);

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public void append(Id docId, VersionNode<PropertyPath, Object, M> version) {
        append(docId, singleton(version));
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public void append(Id docId, Iterable<VersionNode<PropertyPath, Object, M>> versions) {
        SQLInsertClause versionBatch = queryFactory.insert(jVersion);
        SQLInsertClause parentBatch = queryFactory.insert(jParent);
        SQLInsertClause propertyBatch = queryFactory.insert(jProperty);

        for (VersionNode<PropertyPath, Object, M> version : versions) {
            addVersion(docId, version, versionBatch);
            addParents(version, parentBatch);
            addProperties(docId, version, propertyBatch);
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

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
    public void publish() {
        long lastOrdinal = getLastOrdinalForUpdate();

        for (Revision revision : findUncommittedRevisions()) {
            queryFactory
                    .update(jVersion)
                    .where(jVersion.revision.eq(revision))
                    .set(jVersion.ordinal, ++lastOrdinal)
                    .setNull(jVersion.txOrdinal)
                    .execute();
        }

        queryFactory
                .update(jRepository)
                .where(jRepository.id.eq(REPOSITORY_ID))
                .set(jRepository.ordinal, lastOrdinal)
                .execute();
    }

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public ObjectVersionGraph<M> load(Id docId) {
        return ObjectVersionGraph.init(fetch(docId, null));
    }

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public List<ObjectVersion<M>> fetchUpdates(Id docId, Revision since) {
        Check.notNull(since, "since");
        return fetch(docId, since);
    }

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    // TODO: 1) Return also revision of max(ordinal)
    // TODO: 2) Add "Revision until" parameter to searches
    public List<Id> findDocuments(@Nullable Revision updatedSince) {
        Long sinceOrdinal = getOrdinal(updatedSince);
        SQLQuery qry = queryFactory
                .from(jVersion)
                .groupBy(jVersion.revision)
                .orderBy(jVersion.ordinal.max().asc());

        if (sinceOrdinal == null) {
            qry.where(jVersion.ordinal.isNotNull());
        } else {
            qry.where(jVersion.ordinal.gt(sinceOrdinal));
        }

        return qry.list((Expression<Id>) versionDocId());
    }

    private List<ObjectVersion<M>> fetch(Id docId, Revision sinceRevision) {
        Check.notNull(docId, "docId");

        Long sinceOrdinal = getOrdinal(sinceRevision);

        List<Group> versionsAndParents = getVersionsAndParents(docId, sinceOrdinal);
        if (versionsAndParents.isEmpty()) {
            return ImmutableList.of();
        }

        List<ObjectVersion<M>> versions = new ArrayList<>(versionsAndParents.size());
        Map<Revision, List<Tuple>> properties = getPropertiesByDocId(docId, sinceOrdinal);

        for (Group versionAndParents : versionsAndParents) {
            Revision rev = versionAndParents.getOne(jVersion.revision);
            Map<PropertyPath, Object> changeset = toChangeSet(properties.get(rev));

            versions.add(buildVersion(rev, versionAndParents, changeset));
        }
        return versions;
    }

    private Long getOrdinal(Revision sinceRevision) {
        if (sinceRevision == null) {
            return null;
        }
        return queryFactory
                .from(jVersion)
                .where(jVersion.revision.eq(sinceRevision))
                .singleResult(jVersion.ordinal);
    }

    private long getLastOrdinalForUpdate() {
        return queryFactory
                .from(jRepository)
                .where(jRepository.id.eq(REPOSITORY_ID))
                .forUpdate()
                .singleResult(jRepository.ordinal);
    }

    private List<Revision> findUncommittedRevisions() {
        return queryFactory
                .from(jVersion)
                .where(jVersion.txOrdinal.isNotNull())
                .orderBy(jVersion.txOrdinal.asc())
                .list(jVersion.revision);
    }

    protected void addProperties(Id docId, VersionNode<PropertyPath, Object, M> version, SQLInsertClause propertyBatch) {
        addProperties(docId, version.revision, version.getChangeset(), propertyBatch);
    }

    protected void addProperties(Id docId, Revision revision, Map<PropertyPath, Object> changeset, SQLInsertClause propertyBatch) {
        for (Entry<PropertyPath, Object> entry : changeset.entrySet()) {
            propertyBatch
                    .set(propertyDocId(), docId)
                    .set(jProperty.revision, revision)
                    .set(jProperty.path, entry.getKey().toString());
            setValue(entry.getKey(), entry.getValue(), propertyBatch);
            propertyBatch.addBatch();
        }
    }

    protected abstract <T extends SimpleExpression<Id> & Path<Id>> T versionDocId();

    protected abstract <T extends SimpleExpression<Id> & Path<Id>> T propertyDocId();

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

    protected void addParents(VersionNode<PropertyPath, Object, M> version, SQLInsertClause parentBatch) {
        for (Revision parentRevision : version.parentRevisions) {
            parentBatch
                    .set(jParent.revision, version.revision)
                    .set(jParent.parentRevision, parentRevision)
                    .addBatch();
        }
    }

    protected void addVersion(Id docId, VersionNode<PropertyPath, Object, M> version, SQLInsertClause versionBatch) {
        versionBatch
                .set(jVersion.revision, version.revision)
                .set(versionDocId(), docId)
                .set(jVersion.txOrdinal, nextOrdinal)
                .set(jVersion.type, version.type)
                .set(jVersion.branch, version.branch)
                .addBatch();
    }

    protected ObjectVersion<M> buildVersion(Revision rev, Group versionAndParents, Map<PropertyPath, Object> changeset) {
        return new ObjectVersionBuilder<M>(rev)
                .branch(versionAndParents.getOne(jVersion.branch))
                .type(versionAndParents.getOne(jVersion.type))
                .parents(versionAndParents.getSet(jParent.parentRevision))
                .changeset(changeset)
                .build();
    }

    protected Map<Revision, List<Tuple>> getPropertiesByDocId(Id docId, @Nullable Long sinceOrdinal) {
        SQLQuery qry = queryFactory.from(jProperty);

        if (sinceOrdinal == null) {
            qry.where(propertyDocId().eq(docId));
        } else {
            qry.innerJoin(jProperty.versionPropertyRevisionFk, jVersion);
            qry.where(versionDocId().eq(docId), jVersion.ordinal.gt(sinceOrdinal));
        }
        return qry.transform(properties);
    }

    protected List<Group> getVersionsAndParents(Id docId, @Nullable Long sinceOrdinal) {
        SQLQuery qry = queryFactory
                .from(jVersion)
                .leftJoin(jVersion._versionParentRevisionFk, jParent)
                .where(versionDocId().eq(docId), jVersion.ordinal.isNotNull())
                .orderBy(jVersion.ordinal.asc());

        if (sinceOrdinal != null) {
            qry.where(jVersion.ordinal.gt(sinceOrdinal));
        }

        return qry.transform(groupBy(jVersion.revision).list(versionAndParents));
    }

    protected Expression<?>[] allVersionColumns() {
        return jVersion.all();
    }

    protected Expression<?>[] allPropertyColumns() {
        return jProperty.all();
    }

    protected Map<PropertyPath, Object> toChangeSet(List<Tuple> properties) {
        if (properties == null) {
            return null;
        }
        Map<PropertyPath, Object> changeset = Maps.newHashMapWithExpectedSize(properties.size());
        for (Tuple tuple : properties) {
            PropertyPath path = PropertyPath.parse(tuple.get(jProperty.path));
            Object value = getPropertyValue(tuple);
            changeset.put(path, value);
        }
        return changeset;
    }

    protected Object getPropertyValue(Tuple tuple) {
        String type = tuple.get(jProperty.type);
        String str = tuple.get(jProperty.str);
        Long nbr = tuple.get(jProperty.nbr);

        switch (type.charAt(0)) {
            case 'O': return Persistent.object(str);
            case 'A': return Persistent.array();
            case 's': return str;
            case 'b': return Boolean.valueOf(nbr != 0);
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
