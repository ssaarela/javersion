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
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.javersion.core.Persistent;
import org.javersion.core.Revision;
import org.javersion.core.VersionNode;
import org.javersion.core.VersionType;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionBuilder;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.mysema.query.ResultTransformer;
import com.mysema.query.Tuple;
import com.mysema.query.group.Group;
import com.mysema.query.group.GroupBy;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.SQLExpressions;
import com.mysema.query.sql.SQLQueryFactory;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.types.EnumByNameType;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Path;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.query.NumberSubQuery;

public class ObjectVersionStoreJdbc<M> {

    public static void registerTypes(String tablePrefix, Configuration configuration) {
        configuration.register(tablePrefix + "VERSION", "TYPE", new EnumByNameType<>(VersionType.class));
        configuration.register(tablePrefix + "VERSION", "REVISION", REVISION_TYPE);

        configuration.register(tablePrefix + "VERSION_PARENT", "REVISION", REVISION_TYPE);
        configuration.register(tablePrefix + "VERSION_PARENT", "PARENT_REVISION", REVISION_TYPE);

        configuration.register(tablePrefix + "VERSION_PROPERTY", "REVISION", REVISION_TYPE);
    }

    private static final class OrdinalAndRevision implements Comparable<OrdinalAndRevision> {
        final long ordinal;
        final Revision revision;

        private OrdinalAndRevision(long ordinal, Revision revision) {
            this.ordinal = ordinal;
            this.revision = revision;
        }

        @Override
        public int compareTo(OrdinalAndRevision other) {
            int result = Long.compare(this.ordinal, other.ordinal);
            return result != 0 ? result : this.revision.compareTo(other.revision);
        }
    }

    protected final Expression<Long> nextOrdinal = SQLExpressions.nextval("VERSION_ORDINAL_SEQ");

    protected final JVersion qVersion;

    protected final JVersionParent qParent;

    protected final JVersionProperty qProperty;

    protected final JRepository qRepository;

    protected final SQLQueryFactory queryFactory;

    protected final Expression<?>[] versionAndParents;

    protected final ResultTransformer<Map<Revision, List<Tuple>>> properties;

    protected static final String REPOSITORY_ID = "repository";

    @SuppressWarnings("unused")
    protected ObjectVersionStoreJdbc() {
        qVersion = null;
        qParent = null;
        qProperty = null;
        qRepository = null;
        versionAndParents = null;
        properties = null;
        queryFactory = null;
    }

    public ObjectVersionStoreJdbc(String schema, String tablePrefix, SQLQueryFactory queryFactory) {
        this.qRepository = new JRepository(schema, tablePrefix, "repository");
        this.qVersion = new JVersion(schema, tablePrefix, "version");
        this.qParent = new JVersionParent(schema, tablePrefix, "parent");
        this.qProperty = new JVersionProperty(schema, tablePrefix, "property");
        this.queryFactory = queryFactory;

        versionAndParents = concat(allVersionColumns(), GroupBy.set(qParent.parentRevision));
        properties = groupBy(qProperty.revision).as(GroupBy.list(new QTuple(allPropertyColumns())));
    }

    private NumberSubQuery<Long> maxOrdinalQuery() {
        return queryFactory.subQuery().from(qVersion).unique(qVersion.ordinal.max());
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public void append(String id, VersionNode<PropertyPath, Object, M> version) {
        append(id, singleton(version));
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public void append(String docId, Iterable<VersionNode<PropertyPath, Object, M>> versions) {
        String tx = null;

        SQLInsertClause versionBatch = queryFactory.insert(qVersion);
        SQLInsertClause parentBatch = queryFactory.insert(qParent);
        SQLInsertClause propertyBatch = queryFactory.insert(qProperty);

        for (VersionNode<PropertyPath, Object, M> version : versions) {
            if (tx == null) {
                tx = version.revision.toString();
            }
            addVersion(docId, version, tx, versionBatch);
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
    public void commit() {
        long repositoryOrdinal = getLastOrdinalForUpdate();

        for (Map.Entry<String, Long> entry : findUncommittedTransactions().entrySet()) {
            String tx = entry.getKey();
            long versionOrdinal = entry.getValue();
            if (versionOrdinal <= repositoryOrdinal) {
                shiftOrdinalsAndClearTx(tx, repositoryOrdinal - versionOrdinal + 1);
            } else {
                clearTx(tx);
            }
        }

        queryFactory
                .update(qRepository)
                .where(qRepository.id.eq(REPOSITORY_ID))
                .set(qRepository.ordinal, maxOrdinalQuery())
                .execute();
    }

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public ObjectVersionGraph<M> load(String docId) {
        SortedMap<OrdinalAndRevision, ObjectVersion<M>> versions = new TreeMap<>();
        Map<Revision, Group> versionsAndParents = getVersionsAndParents(docId);
        Map<Revision, List<Tuple>> properties = getPropertiesByDocId(docId);

        for (Group versionAndParents : versionsAndParents.values()) {
            Revision rev = versionAndParents.getOne(qVersion.revision);
            long ordinal = versionAndParents.getOne(qVersion.ordinal);
            Map<PropertyPath, Object> changeset = toChangeSet(properties.get(rev));

            ObjectVersion<M> version = buildVersion(rev, versionAndParents, changeset);

            versions.put(new OrdinalAndRevision(ordinal, rev), version);
        }
        return ObjectVersionGraph.init(versions.values());
    }

    private long getLastOrdinalForUpdate() {
        Long lastOrdinal = queryFactory
                .from(qRepository)
                .where(qRepository.id.eq(REPOSITORY_ID))
                .forUpdate()
                .singleResult(qRepository.ordinal);
        return lastOrdinal != null ? lastOrdinal : 0;
    }

    private void clearTx(String tx) {
        queryFactory
                .update(qVersion)
                .setNull(qVersion.tx)
                .where(qVersion.tx.eq(tx))
                .execute();
    }

    private void shiftOrdinalsAndClearTx(String tx, long shift) {
        queryFactory
                .update(qVersion)
                .set(qVersion.ordinal, qVersion.ordinal.add(shift))
                .setNull(qVersion.tx)
                .where(qVersion.tx.eq(tx))
                .execute();
    }

    private Map<String, Long> findUncommittedTransactions() {
        return queryFactory
                .from(qVersion)
                .where(qVersion.tx.isNotNull())
                .groupBy(qVersion.tx)
                .map(qVersion.tx, qVersion.ordinal.min());
    }

    protected void addProperties(String docId, VersionNode<PropertyPath, Object, M> version, SQLInsertClause propertyBatch) {
        addProperties(docId, version.revision, version.getChangeset(), propertyBatch);
    }

    protected void addProperties(String docId, Revision revision, Map<PropertyPath, Object> changeset, SQLInsertClause propertyBatch) {
        for (Entry<PropertyPath, Object> entry : changeset.entrySet()) {
            propertyBatch
                    .set(qProperty.revision, revision)
                    .set(qProperty.docId, docId)
                    .set(qProperty.path, entry.getKey().toString());
            setValue(entry.getKey(), entry.getValue(), propertyBatch);
            propertyBatch.addBatch();
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
                .set(qProperty.type, Character.toString(type))
                .set(qProperty.str, str)
                .set(qProperty.nbr, nbr);
    }

    protected void addParents(VersionNode<PropertyPath, Object, M> version, SQLInsertClause parentBatch) {
        for (Revision parentRevision : version.parentRevisions) {
            parentBatch
                    .set(qParent.revision, version.revision)
                    .set(qParent.parentRevision, parentRevision)
                    .addBatch();
        }
    }

    protected void addVersion(String docId, VersionNode<PropertyPath, Object, M> version, String tx, SQLInsertClause versionBatch) {
        versionBatch
                .set(qVersion.revision, version.revision)
                .set(qVersion.docId, docId)
                .set(qVersion.tx, tx)
                .set(qVersion.ordinal, nextOrdinal)
                .set(qVersion.type, version.type)
                .set(qVersion.branch, version.branch)
                .addBatch();
    }

    protected ObjectVersion<M> buildVersion(Revision rev, Group versionAndParents, Map<PropertyPath, Object> changeset) {
        return new ObjectVersionBuilder<M>(rev)
                .branch(versionAndParents.getOne(qVersion.branch))
                .type(versionAndParents.getOne(qVersion.type))
                .parents(versionAndParents.getSet(qParent.parentRevision))
                .changeset(changeset)
                .build();
    }

    protected Map<Revision, List<Tuple>> getPropertiesByDocId(String docId) {
        return queryFactory
                .from(qProperty)
                .where(qProperty.docId.eq(docId))
                .transform(properties);
    }

    protected Map<Revision, Group> getVersionsAndParents(String docId) {
        return queryFactory
                .from(qVersion)
                .leftJoin(qVersion._versionParentRevisionFk, qParent)
                .where(qVersion.docId.eq(docId), qVersion.tx.isNull())
                .transform(groupBy(qVersion.revision)
                        .as(versionAndParents));
    }

    protected Path<?>[] allVersionColumns() {
        return qVersion.all();
    }

    protected Path<?>[] allPropertyColumns() {
        return qProperty.all();
    }

    protected Map<PropertyPath, Object> toChangeSet(List<Tuple> properties) {
        if (properties == null) {
            return null;
        }
        Map<PropertyPath, Object> changeset = Maps.newHashMapWithExpectedSize(properties.size());
        for (Tuple tuple : properties) {
            PropertyPath path = PropertyPath.parse(tuple.get(qProperty.path));
            Object value = getPropertyValue(tuple);
            changeset.put(path, value);
        }
        return changeset;
    }

    protected Object getPropertyValue(Tuple tuple) {
        String type = tuple.get(qProperty.type);
        String str = tuple.get(qProperty.str);
        Long nbr = tuple.get(qProperty.nbr);

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
