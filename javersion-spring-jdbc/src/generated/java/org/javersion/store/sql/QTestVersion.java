package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTestVersion is a Querydsl query type for QTestVersion
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QTestVersion extends com.mysema.query.sql.RelationalPathBase<QTestVersion> {

    private static final long serialVersionUID = 706366201;

    public static final QTestVersion testVersion = new QTestVersion("TEST_VERSION");

    public final StringPath branch = createString("branch");

    public final StringPath docId = createString("docId");

    public final NumberPath<Long> ordinal = createNumber("ordinal", Long.class);

    public final SimplePath<org.javersion.core.Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final NumberPath<Long> txOrdinal = createNumber("txOrdinal", Long.class);

    public final EnumPath<org.javersion.core.VersionType> type = createEnum("type", org.javersion.core.VersionType.class);

    public final com.mysema.query.sql.PrimaryKey<QTestVersion> constraint4 = createPrimaryKey(revision);

    public final com.mysema.query.sql.ForeignKey<QVersionType> testVersionTypeFk = createForeignKey(type, "NAME");

    public final com.mysema.query.sql.ForeignKey<QTestVersionParent> _testVersionParentRevisionFk = createInvForeignKey(revision, "REVISION");

    public final com.mysema.query.sql.ForeignKey<QTestVersionProperty> _testVersionPropertyRevisionFk = createInvForeignKey(revision, "REVISION");

    public final com.mysema.query.sql.ForeignKey<QTestVersionParent> _testVersionParentParentRevisionFk = createInvForeignKey(revision, "PARENT_REVISION");

    public QTestVersion(String variable) {
        super(QTestVersion.class, forVariable(variable), "PUBLIC", "TEST_VERSION");
        addMetadata();
    }

    public QTestVersion(String variable, String schema, String table) {
        super(QTestVersion.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTestVersion(Path<? extends QTestVersion> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "TEST_VERSION");
        addMetadata();
    }

    public QTestVersion(PathMetadata<?> metadata) {
        super(QTestVersion.class, metadata, "PUBLIC", "TEST_VERSION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(branch, ColumnMetadata.named("BRANCH").withIndex(5).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(docId, ColumnMetadata.named("DOC_ID").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(ordinal, ColumnMetadata.named("ORDINAL").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(txOrdinal, ColumnMetadata.named("TX_ORDINAL").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(8).notNull());
    }

}

