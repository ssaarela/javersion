package org.javersion.store;

import static com.mysema.query.group.GroupBy.groupBy;
import static java.util.Collections.singleton;
import static java.util.Map.Entry;
import static org.javersion.store.ObjectVersionStoreJdbc.ConfigProp.NODE;
import static org.javersion.store.ObjectVersionStoreJdbc.ConfigProp.ORDINAL;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.math.BigDecimal;
import java.util.*;

import org.javersion.core.Revision;
import org.javersion.core.Version;
import org.javersion.core.VersionType;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionBuilder;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.object.Persistent;
import org.javersion.path.PropertyPath;
import org.javersion.store.sql.QRepository;
import org.javersion.store.sql.QVersion;
import org.javersion.store.sql.QVersionParent;
import org.javersion.store.sql.QVersionProperty;
import org.javersion.util.Check;
import org.springframework.transaction.annotation.Transactional;

import com.eaio.uuid.UUIDGen;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mysema.query.Tuple;
import com.mysema.query.dml.StoreClause;
import com.mysema.query.group.Group;
import com.mysema.query.group.GroupBy;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.SQLExpressions;
import com.mysema.query.sql.SQLQueryFactory;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.types.EnumByNameType;
import com.mysema.query.types.Expression;
import com.mysema.query.types.MappingProjection;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.path.StringPath;
import com.mysema.query.types.query.NumberSubQuery;

public class ObjectVersionStoreJdbc<M> implements VersionStore<String,
        PropertyPath, Object, M,
        ObjectVersionGraph<M>,
        ObjectVersionGraph.Builder<M>> {

    public static class Initializer {
        private final SQLQueryFactory queryFactory;

        protected Initializer() {
            this.queryFactory = null;
        }

        public Initializer(SQLQueryFactory queryFactory) {
            this.queryFactory = Check.notNull(queryFactory, "queryFactory");
        }

        public SQLQueryFactory getQueryFactory() {
            return queryFactory;
        }

        @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
        public long initialize() {
            Long node = queryFactory.from(qRepository).where(qRepository.key.eq(NODE)).singleResult(qRepository.val);
            if (node == null) {
                node = UUIDGen.getClockSeqAndNode();
                queryFactory
                        .insert(qRepository)
                        .set(qRepository.key, NODE)
                        .set(qRepository.val, node)
                        .addBatch()
                        .set(qRepository.key, ORDINAL)
                        .setNull(qRepository.val)
                        .execute();
            }
            return node;
        }

    }

    public static enum ConfigProp {
        NODE, ORDINAL
    }

    public static void registerTypes(Configuration configuration) {
        configuration.register("VERSION", "TYPE", new EnumByNameType<>(VersionType.class));
        configuration.register("REPOSITORY", "KEY", new EnumByNameType<>(ConfigProp.class));
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

    private static final class RevisionMapping extends MappingProjection<Revision> {

        private final StringPath revisionPath;

        public RevisionMapping(StringPath revisionPath) {
            super(Revision.class, revisionPath);
            this.revisionPath = revisionPath;
        }

        @Override
        protected Revision map(Tuple row) {
            String rev = row.get(revisionPath);
            return rev != null ? new Revision(rev) : null;
        }

        public <T extends StoreClause<T>> T populate(Revision revision, T store) {
            return store.set(revisionPath, revision.toString());
        }
    }

    private static final Expression<Long> nextOrdinal = SQLExpressions.nextval("version_ordinal_seq");

    private static final QVersion qVersion = QVersion.version;

    private static final QVersionParent qParent = QVersionParent.versionParent;

    private static final QVersion qParentVersion = new QVersion("parent");

    private static final QVersionProperty qProperty = QVersionProperty.versionProperty;

    private static final QRepository qRepository = QRepository.repository;

    private static final RevisionMapping versionRevision = new RevisionMapping(qVersion.revision);

    private static final RevisionMapping versionChild = new RevisionMapping(qParent.childRevision);

    private static final RevisionMapping versionParent = new RevisionMapping(qParent.parentRevision);

    private static final RevisionMapping propertyRevision = new RevisionMapping(qProperty.revision);

    private final SQLQueryFactory queryFactory;

    private final long node;

    protected ObjectVersionStoreJdbc() {
        queryFactory = null;
        node = 0;
    }

    public ObjectVersionStoreJdbc(Initializer initializer) {
        this.queryFactory = initializer.getQueryFactory();
        this.node = initializer.initialize();
    }

    private NumberSubQuery<Long> maxOrdinalQuery() {
        return queryFactory.subQuery().from(qVersion).unique(qVersion.ordinal.max());
    }

    @Override
    public long getNode() {
        return node;
    }

    @Override
    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public void append(String id, Version<PropertyPath, Object, M> version) {
        append(id, singleton(version));
    }

    @Override
    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public void append(String docId, Iterable<Version<PropertyPath, Object, M>> versions) {
        String tx = null;

        SQLInsertClause versionBatch = queryFactory.insert(qVersion);
        SQLInsertClause parentBatch = queryFactory.insert(qParent);
        SQLInsertClause propertyBatch = queryFactory.insert(qProperty);

        for (Version<PropertyPath, Object, M> version : versions) {
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

    @Override
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
                .where(qRepository.key.eq(ORDINAL))
                .set(qRepository.val, maxOrdinalQuery())
                .execute();
    }

    @Override
    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public ObjectVersionGraph<M> load(Iterable<String> docIds) {
        return ObjectVersionGraph.init(loadVersions(ImmutableSet.copyOf(docIds)));
    }

    private Iterable<ObjectVersion<M>> loadVersions(Set<String> docIds) {
        SortedMap<OrdinalAndRevision, ObjectVersion<M>> versions = new TreeMap<>();
        Set<String> retrievedDocIds = new HashSet<>();
        Set<String> nextBatch = docIds;

        while (!nextBatch.isEmpty()) {
            Map<Revision, Group> versionsAndParents = getVersionsAndParents(nextBatch);
            Map<Revision, List<Tuple>> properties = getPropertiesByDocId(nextBatch);

            retrievedDocIds.addAll(nextBatch);
            nextBatch = new HashSet<>();

            for (Group versionAndParents : versionsAndParents.values()) {
                Revision rev = versionAndParents.getOne(versionRevision);
                long ordinal = versionAndParents.getOne(qVersion.ordinal);
                Map<PropertyPath, Object> changeset = toChangeSet(properties.get(rev));

                ObjectVersion<M> version = buildVersion(rev, versionAndParents, changeset);

                versions.put(new OrdinalAndRevision(ordinal, rev), version);

                for (String parentDocId : versionAndParents.getSet(qParent.parentDocId)) {
                    if (!retrievedDocIds.contains(parentDocId)) {
                        nextBatch.add(parentDocId);
                    }
                }
            }
        }
        return versions.values();
    }

    private long getLastOrdinalForUpdate() {
        Long lastOrdinal = queryFactory
                .from(qRepository)
                .where(qRepository.key.eq(ORDINAL))
                .forUpdate()
                .singleResult(qRepository.val);
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

    private void addProperties(String docId, Version<PropertyPath, Object, M> version, SQLInsertClause propertyBatch) {
        for (Entry<PropertyPath, Object> entry : version.changeset.entrySet()) {
            propertyRevision
                    .populate(version.revision, propertyBatch)
                    .set(qProperty.docId, docId)
                    .set(qProperty.path, entry.getKey().toString());
            setValue(entry.getValue(), propertyBatch);
        }
    }

    private void setValue(Object value, SQLInsertClause propertyBatch) {
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
                nbr = ((Boolean) value).booleanValue() ? 1l : 0l;
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
                .set(qProperty.nbr, nbr)
                .addBatch();
    }

    private void addParents(Version<PropertyPath, Object, M> version, SQLInsertClause parentBatch) {
        for (Revision parentRevision : version.parentRevisions) {
            parentBatch.set(qParent.parentDocId, queryFactory.subQuery()
                    .from(qParentVersion)
                    .where(qParentVersion.revision.eq(parentRevision.toString()))
                    .distinct().unique(qParentVersion.docId));
            versionChild.populate(version.revision, parentBatch);
            versionParent.populate(parentRevision, parentBatch).addBatch();
        }
    }

    private void addVersion(String docId, Version<PropertyPath, Object, M> version, String tx, SQLInsertClause versionBatch) {
        versionRevision
                .populate(version.revision, versionBatch)
                .set(qVersion.docId, docId)
                .set(qVersion.tx, tx)
                .set(qVersion.ordinal, nextOrdinal)
                .set(qVersion.type, version.type)
                .set(qVersion.branch, version.branch)
                .addBatch();
    }

    private ObjectVersion<M> buildVersion(Revision rev, Group versionAndParents, Map<PropertyPath, Object> changeset) {
        return new ObjectVersionBuilder<M>(rev)
                .branch(versionAndParents.getOne(qVersion.branch))
                .type(versionAndParents.getOne(qVersion.type))
                .parents(versionAndParents.getSet(versionParent))
                .changeset(changeset)
                .build();
    }

    private Map<Revision, List<Tuple>> getPropertiesByDocId(Set<String> nextBatch) {
        return queryFactory
                .from(qProperty)
                .where(qProperty.docId.in(nextBatch))
                .transform(groupBy(propertyRevision).as(GroupBy.list(new QTuple(qProperty.all()))));
    }

    private Map<Revision, Group> getVersionsAndParents(Set<String> nextBatch) {
        return queryFactory
                .from(qVersion)
                .leftJoin(qVersion._versionParentChildRevisionFk, qParent)
                .where(qVersion.docId.in(nextBatch), qVersion.tx.isNull())
//                .orderBy(qVersion.ordinal.asc(), qVersion.revision.asc())
                .transform(groupBy(versionRevision)
                        .as(versionRevision, qVersion.branch, qVersion.type, qVersion.ordinal,
                                GroupBy.set(qParent.parentDocId),
                                GroupBy.set(versionParent)));
    }

    private Map<PropertyPath, Object> toChangeSet(List<Tuple> properties) {
        Map<PropertyPath, Object> changeset = Maps.newHashMapWithExpectedSize(properties.size());
        if (properties != null) {
            for (Tuple tuple : properties) {
                PropertyPath path = PropertyPath.parse(tuple.get(qProperty.path));
                Object value = getPropertyValue(tuple);
                changeset.put(path, value);
            }
        }
        return changeset;
    }

    private Object getPropertyValue(Tuple tuple) {
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
}
