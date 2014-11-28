package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import java.util.*;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;

import com.mysema.query.sql.spatial.RelationalPathSpatial;

import com.mysema.query.spatial.path.*;



/**
 * QVersion is a Querydsl query type for QVersion
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QVersion extends RelationalPathSpatial<QVersion> {

    private static final long serialVersionUID = 103506283;

    public static final QVersion version = new QVersion("VERSION");

    public final StringPath branch = createString("branch");

    public final StringPath docId = createString("docId");

    public final NumberPath<Long> ordinal = createNumber("ordinal", Long.class);

    public final NumberPath<Long> revisionNode = createNumber("revisionNode", Long.class);

    public final NumberPath<Long> revisionSeq = createNumber("revisionSeq", Long.class);

    public final StringPath tx = createString("tx");

    public final EnumPath<org.javersion.core.VersionType> type = createEnum("type", org.javersion.core.VersionType.class);

    public final com.mysema.query.sql.PrimaryKey<QVersion> constraint3 = createPrimaryKey(revisionNode, revisionSeq);

    public final com.mysema.query.sql.ForeignKey<QVersionType> versionTypeFk = createForeignKey(type, "NAME");

    public final com.mysema.query.sql.ForeignKey<QVersionProperty> _versionPropertyRevisionFk = createInvForeignKey(Arrays.asList(revisionSeq, revisionNode), Arrays.asList("REVISION_SEQ", "REVISION_NODE"));

    public final com.mysema.query.sql.ForeignKey<QVersionParent> _versionParentChildRevisionFk = createInvForeignKey(Arrays.asList(revisionSeq, revisionNode), Arrays.asList("CHILD_REVISION_SEQ", "CHILD_REVISION_NODE"));

    public final com.mysema.query.sql.ForeignKey<QVersionParent> _versionParentParentRevisionFk = createInvForeignKey(Arrays.asList(revisionSeq, revisionNode), Arrays.asList("PARENT_REVISION_SEQ", "PARENT_REVISION_NODE"));

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
        addMetadata(branch, ColumnMetadata.named("BRANCH").withIndex(6).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(docId, ColumnMetadata.named("DOC_ID").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(ordinal, ColumnMetadata.named("ORDINAL").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(revisionNode, ColumnMetadata.named("REVISION_NODE").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(revisionSeq, ColumnMetadata.named("REVISION_SEQ").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(tx, ColumnMetadata.named("TX").withIndex(5).ofType(Types.VARCHAR).withSize(32));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(7).ofType(Types.VARCHAR).withSize(8).notNull());
    }

}

