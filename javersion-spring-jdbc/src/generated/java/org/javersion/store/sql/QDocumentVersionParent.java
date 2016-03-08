package org.javersion.store.sql;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;




/**
 * QDocumentVersionParent is a Querydsl query type for QDocumentVersionParent
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QDocumentVersionParent extends RelationalPathBase<QDocumentVersionParent> {

    private static final long serialVersionUID = -642672230;

    public static final QDocumentVersionParent documentVersionParent = new QDocumentVersionParent("DOCUMENT_VERSION_PARENT");

    public final SimplePath<org.javersion.core.Revision> parentRevision = createSimple("parentRevision", org.javersion.core.Revision.class);

    public final SimplePath<org.javersion.core.Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final PrimaryKey<QDocumentVersionParent> constraint5 = createPrimaryKey(parentRevision, revision);

    public final ForeignKey<QDocumentVersion> documentVersionParentRevisionFk = createForeignKey(revision, "REVISION");

    public final ForeignKey<QDocumentVersion> documentVersionParentParentRevisionFk = createForeignKey(parentRevision, "REVISION");

    public QDocumentVersionParent(String variable) {
        super(QDocumentVersionParent.class, forVariable(variable), "PUBLIC", "DOCUMENT_VERSION_PARENT");
        addMetadata();
    }

    public QDocumentVersionParent(String variable, String schema, String table) {
        super(QDocumentVersionParent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDocumentVersionParent(Path<? extends QDocumentVersionParent> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "DOCUMENT_VERSION_PARENT");
        addMetadata();
    }

    public QDocumentVersionParent(PathMetadata metadata) {
        super(QDocumentVersionParent.class, metadata, "PUBLIC", "DOCUMENT_VERSION_PARENT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(parentRevision, ColumnMetadata.named("PARENT_REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

