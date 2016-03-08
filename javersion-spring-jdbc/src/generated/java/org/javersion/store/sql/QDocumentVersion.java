package org.javersion.store.sql;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import org.javersion.core.Revision;
import org.javersion.core.VersionType;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;

/**
 * QDocumentVersion is a Querydsl query type for QDocumentVersion
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QDocumentVersion extends RelationalPathBase<QDocumentVersion> {

    private static final long serialVersionUID = 2075230416;

    public static final QDocumentVersion documentVersion = new QDocumentVersion("DOCUMENT_VERSION");

    public final StringPath branch = createString("branch");

    public final StringPath docId = createString("docId");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> ordinal = createNumber("ordinal", Long.class);

    public final SimplePath<Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final NumberPath<Long> txOrdinal = createNumber("txOrdinal", Long.class);

    public final EnumPath<VersionType> type = createEnum("type", org.javersion.core.VersionType.class);

    public final PrimaryKey<QDocumentVersion> constraint4 = createPrimaryKey(revision);

    public final ForeignKey<QVersionType> documentVersionTypeFk = createForeignKey(type, "NAME");

    public final ForeignKey<QDocumentVersionParent> _documentVersionParentRevisionFk = createInvForeignKey(revision, "REVISION");

    public final ForeignKey<QDocumentVersionParent> _documentVersionParentParentRevisionFk = createInvForeignKey(revision, "PARENT_REVISION");

    public final ForeignKey<QDocumentVersionProperty> _documentVersionPropertyRevisionFk = createInvForeignKey(revision, "REVISION");

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

    public QDocumentVersion(PathMetadata metadata) {
        super(QDocumentVersion.class, metadata, "PUBLIC", "DOCUMENT_VERSION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(branch, ColumnMetadata.named("BRANCH").withIndex(5).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(docId, ColumnMetadata.named("DOC_ID").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(7).ofType(Types.BIGINT).withSize(19));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(8).ofType(Types.VARCHAR).withSize(255));
        addMetadata(ordinal, ColumnMetadata.named("ORDINAL").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(txOrdinal, ColumnMetadata.named("TX_ORDINAL").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(8).notNull());
    }

}

