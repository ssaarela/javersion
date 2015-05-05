package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QVersion is a Querydsl query type for QVersion
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QVersion extends com.mysema.query.sql.RelationalPathBase<QVersion> {

    private static final long serialVersionUID = 103506283;

    public static final QVersion version = new QVersion("VERSION");

    public final StringPath branch = createString("branch");

    public final StringPath docId = createString("docId");

    public final NumberPath<Long> ordinal = createNumber("ordinal", Long.class);

    public final SimplePath<org.javersion.core.Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final StringPath tx = createString("tx");

    public final EnumPath<org.javersion.core.VersionType> type = createEnum("type", org.javersion.core.VersionType.class);

    public final com.mysema.query.sql.PrimaryKey<QVersion> constraint3 = createPrimaryKey(revision);

    public final com.mysema.query.sql.ForeignKey<QVersionType> versionTypeFk = createForeignKey(type, "NAME");

    public final com.mysema.query.sql.ForeignKey<QVersionProperty> _versionPropertyRevisionFk = createInvForeignKey(revision, "REVISION");

    public final com.mysema.query.sql.ForeignKey<QVersionParent> _versionParentParentRevisionFk = createInvForeignKey(revision, "PARENT_REVISION");

    public final com.mysema.query.sql.ForeignKey<QVersionParent> _versionParentRevisionFk = createInvForeignKey(revision, "REVISION");

    public QVersion(String variable) {
        super(QVersion.class, forVariable(variable), "PUBLIC", "VERSION");
        addMetadata();
    }

    public QVersion(String variable, String schema, String table) {
        super(QVersion.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QVersion(Path<? extends QVersion> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "VERSION");
        addMetadata();
    }

    public QVersion(PathMetadata<?> metadata) {
        super(QVersion.class, metadata, "PUBLIC", "VERSION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(branch, ColumnMetadata.named("BRANCH").withIndex(5).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(docId, ColumnMetadata.named("DOC_ID").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(ordinal, ColumnMetadata.named("ORDINAL").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(tx, ColumnMetadata.named("TX").withIndex(4).ofType(Types.VARCHAR).withSize(32));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(8).notNull());
    }

}

