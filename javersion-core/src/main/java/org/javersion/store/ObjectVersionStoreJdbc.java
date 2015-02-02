package org.javersion.store;

import static com.mysema.query.group.GroupBy.groupBy;
import static java.sql.Connection.TRANSACTION_READ_COMMITTED;
import static java.util.Collections.singleton;
import static java.util.Map.Entry;
import static org.javersion.store.ObjectVersionStoreJdbc.ConfigProp.NODE;
import static org.javersion.store.ObjectVersionStoreJdbc.ConfigProp.ORDINAL;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.sql.DataSource;

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

import com.eaio.uuid.UUIDGen;
import com.mysema.query.Tuple;
import com.mysema.query.dml.StoreClause;
import com.mysema.query.group.Group;
import com.mysema.query.group.GroupBy;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.SQLExpressions;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLQueryFactory;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.types.EnumByNameType;
import com.mysema.query.types.Expression;
import com.mysema.query.types.MappingProjection;
import com.mysema.query.types.Path;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.query.NumberSubQuery;

public class ObjectVersionStoreJdbc<M> implements VersionStore<String,
        PropertyPath, Object, M,
        ObjectVersionGraph<M>,
        ObjectVersionGraph.Builder<M>> {

    public static enum ConfigProp {
        NODE, ORDINAL
    }

    public static Configuration configuration(SQLTemplates templates) {
        Configuration configuration = new Configuration(templates);
        configuration.register("VERSION", "TYPE", new EnumByNameType<VersionType>(VersionType.class));
        configuration.register("REPOSITORY", "KEY", new EnumByNameType<ConfigProp>(ConfigProp.class));
        return configuration;
    }

    private static final class RevisionMapping extends MappingProjection<Revision> {

        private final Path<Long> revisionSeq;
        private final Path<Long> revisionNode;

        public RevisionMapping(Path<Long> revisionSeq, Path<Long> revisionNode) {
            super(Revision.class, revisionSeq, revisionNode);
            this.revisionSeq = revisionSeq;
            this.revisionNode = revisionNode;
        }

        @Override
        protected Revision map(Tuple row) {
            Long seq = row.get(revisionSeq);
            Long node = row.get(revisionNode);
            return seq != null && node != null ? new Revision(node, seq) : null;
        }

        public <T extends StoreClause<T>> T populate(Revision revision, T store) {
            return store.set(revisionSeq, revision.timeSeq).set(revisionNode, revision.node);
        }
    }

    private static final Expression<Long> nextOrdinal = SQLExpressions.nextval("version_ordinal_seq");

    private static final QVersion qVersion = QVersion.version;

    private static final QVersionParent qParent = QVersionParent.versionParent;

    private static final QVersionProperty qProperty = QVersionProperty.versionProperty;

    private static final QRepository qRepository = QRepository.repository;

    private static final RevisionMapping versionRevision = new RevisionMapping(qVersion.revisionSeq, qVersion.revisionNode);

    private static final RevisionMapping versionChild = new RevisionMapping(qParent.childRevisionSeq, qParent.childRevisionNode);

    private static final RevisionMapping versionParent = new RevisionMapping(qParent.parentRevisionSeq, qParent.parentRevisionNode);

    private static final RevisionMapping propertyRevision = new RevisionMapping(qProperty.revisionSeq, qProperty.revisionNode);

    private final SQLQueryFactory queryFactory;

    private final DataSource dataSource;

    private final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    private final long node;

    public ObjectVersionStoreJdbc(DataSource dataSource, SQLTemplates templates) {
        this.dataSource = dataSource;
        this.queryFactory = new SQLQueryFactory(configuration(templates), () -> connectionHolder.get());

        try (Connection connection = txBegin()) {
            Long node = queryFactory.from(qRepository).where(qRepository.key.eq(NODE)).singleResult(qRepository.val);
            if (node == null) {
                node = UUIDGen.getClockSeqAndNode();
                queryFactory
                        .insert(qRepository)
                        .set(qRepository.key, NODE)
                        .set(qRepository.val, node)
                        .addBatch()
                        .set(qRepository.key, ORDINAL)
                        .set(qRepository.val, maxOrdinalQuery())
                        .execute();
            }
            this.node = node;
            txCommit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private NumberSubQuery<Long> maxOrdinalQuery() {
        return queryFactory.subQuery().from(qVersion).unique(qVersion.ordinal.max());
    }

    public long getNode() {
        return node;
    }

    private Connection txBegin() throws SQLException {
        Connection connection = connectionHolder.get();
        if (connection != null) {
            throw new Error("connection already open");
        }
        connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(TRANSACTION_READ_COMMITTED);
        connectionHolder.set(connection);
        return connection;
    }

    private void txCommit() throws SQLException {
        Connection connection = connectionHolder.get();
        if (connection == null) {
            throw new Error("no connection");
        }
        connectionHolder.set(null);
        connection.commit();
    }

    @Override
    public void append(String id, Version<PropertyPath, Object, M> version) {
        append(id, singleton(version));
    }

    @Override
    public void append(String docId, Iterable<Version<PropertyPath, Object, M>> versions) {
        try (Connection connection = txBegin()) {
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
                addProperties(version, propertyBatch);
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
            txCommit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private long getLastOrdinalForUpdate() {
        Long lastOrdinal = queryFactory
                .from(qRepository)
                .where(qRepository.key.eq(ORDINAL))
                .forUpdate()
                .singleResult(qRepository.val);
        return lastOrdinal != null ? lastOrdinal : 0;
    }

    @Override
    public void commit() {
        try (Connection connection = txBegin()) {
            long repositoryOrdinal = getLastOrdinalForUpdate();

            List<String> txs = new ArrayList<>();
            for (Map.Entry<String, Long> entry : findUncommittedTransactions().entrySet()) {
                String tx = entry.getKey();
                long versionOrdinal = entry.getValue();
                if (versionOrdinal <= repositoryOrdinal) {
                    shiftOrdinals(tx, repositoryOrdinal - versionOrdinal + 1);
                }
                txs.add(tx);
            }
            queryFactory
                    .update(qVersion)
                    .setNull(qVersion.tx)
                    .where(qVersion.tx.in(txs))
                    .execute();

            queryFactory
                    .update(qRepository)
                    .where(qRepository.key.eq(ORDINAL))
                    .set(qRepository.val, maxOrdinalQuery())
                    .execute();
            txCommit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void shiftOrdinals(String tx, long shift) {
        queryFactory
                .update(qVersion)
                .set(qVersion.ordinal, qVersion.ordinal.add(shift))
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

    private void addProperties(Version<PropertyPath, Object, M> version, SQLInsertClause propertyBatch) {
        for (Entry<PropertyPath, Object> entry : version.changeset.entrySet()) {
            propertyRevision
                    .populate(version.revision, propertyBatch)
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

    @Override
    public ObjectVersionGraph<M> load(String id) {
        return load(id, null);
    }

    @Override
    public ObjectVersionGraph<M> load(String docId, @Nullable Revision revision) {
        try (Connection connection = txBegin()) {
            Map<Revision, Group> versionsAndParents = queryFactory
                    .from(qVersion)
                    .leftJoin(qVersion._versionParentChildRevisionFk, qParent)
                    .where(qVersion.docId.eq(docId), qVersion.tx.isNull())
                    .orderBy(qVersion.ordinal.asc(), qVersion.revisionSeq.asc())
                    .transform(groupBy(versionRevision).as(versionRevision, qVersion.branch, qVersion.type, GroupBy.set(versionParent)));

            Map<Revision, List<Tuple>> properties = queryFactory
                    .from(qProperty)
                    .innerJoin(qProperty.versionPropertyRevisionFk, qVersion)
                    .where(qVersion.docId.eq(docId), qVersion.tx.isNull())
                    .transform(groupBy(versionRevision).as(GroupBy.list(new QTuple(qProperty.all()))));

            ObjectVersionGraph.Builder<M> graphBuilder = new ObjectVersionGraph.Builder<>();
            for (Group versionAndParents : versionsAndParents.values()) {
                Revision rev = versionAndParents.getOne(versionRevision);

                Map<PropertyPath, Object> changeset = toChangeSet(properties.get(rev));

                ObjectVersion<M> version = new ObjectVersionBuilder<M>(rev)
                        .branch(versionAndParents.getOne(qVersion.branch))
                        .type(versionAndParents.getOne(qVersion.type))
                        .parents(versionAndParents.getSet(versionParent))
                        .changeset(changeset)
                        .build();

                graphBuilder.add(version);
            }
            txCommit();
            return graphBuilder.build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<PropertyPath, Object> toChangeSet(List<Tuple> properties) {
        Map<PropertyPath, Object> changeset = new HashMap<>();
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
