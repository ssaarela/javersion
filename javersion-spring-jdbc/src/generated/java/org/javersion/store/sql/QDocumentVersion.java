package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDocumentVersion is a Querydsl query type for QDocumentVersion
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QDocumentVersion extends com.mysema.query.sql.RelationalPathBase<QDocumentVersion> {

    private static final long serialVersionUID = 2075230416;

    public static final QDocumentVersion documentVersion = new QDocumentVersion("DOCUMENT_VERSION");

    public final StringPath branch = createString("branch");

    public final StringPath docId = createString("docId");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> localOrdinal = createNumber("localOrdinal", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> ordinal = createNumber("ordinal", Long.class);

    public final SimplePath<org.javersion.core.Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final EnumPath<org.javersion.core.VersionType> type = createEnum("type", org.javersion.core.VersionType.class);

    public final com.mysema.query.sql.PrimaryKey<QDocumentVersion> constraint4 = createPrimaryKey(revision);

    public final com.mysema.query.sql.ForeignKey<QVersionType> documentVersionTypeFk = createForeignKey(type, "NAME");

    public final com.mysema.query.sql.ForeignKey<QDocumentVersionParent> _documentVersionParentRevisionFk = createInvForeignKey(revision, "REVISION");

    public final com.mysema.query.sql.ForeignKey<QDocumentVersionParent> _documentVersionParentParentRevisionFk = createInvForeignKey(revision, "PARENT_REVISION");

    public final com.mysema.query.sql.ForeignKey<QDocumentVersionProperty> _documentVersionPropertyRevisionFk = createInvForeignKey(revision, "REVISION");

    public QDocumentVersion(String variable) {
        super(QDocumentVersion.class, forVariable(variable), "PUBLIC", "DOCUMENT_VERSION");
        addMetadata();
    }

    public QDocumentVersion(String variable, String schema, String table) {
        super(QDocumentVersion.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDocumentVersion(Path<? extends QDocumentVersion> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "DOCUMENT_VERSION");
        addMetadata();
    }

    public QDocumentVersion(PathMetadata<?> metadata) {
        super(QDocumentVersion.class, metadata, "PUBLIC", "DOCUMENT_VERSION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(branch, ColumnMetadata.named("BRANCH").withIndex(5).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(docId, ColumnMetadata.named("DOC_ID").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(7).ofType(Types.BIGINT).withSize(19));
        addMetadata(localOrdinal, ColumnMetadata.named("LOCAL_ORDINAL").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(8).ofType(Types.VARCHAR).withSize(255));
        addMetadata(ordinal, ColumnMetadata.named("ORDINAL").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(8).notNull());
    }

}

