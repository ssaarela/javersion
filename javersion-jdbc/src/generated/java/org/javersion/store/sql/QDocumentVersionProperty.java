package org.javersion.store.sql;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;




/**
 * QDocumentVersionProperty is a Querydsl query type for QDocumentVersionProperty
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDocumentVersionProperty extends com.querydsl.sql.RelationalPathBase<QDocumentVersionProperty> {

    private static final long serialVersionUID = -1301024059;

    public static final QDocumentVersionProperty documentVersionProperty = new QDocumentVersionProperty("DOCUMENT_VERSION_PROPERTY");

    public final NumberPath<Long> nbr = createNumber("nbr", Long.class);

    public final StringPath path = createString("path");

    public final SimplePath<org.javersion.core.Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final EnumPath<org.javersion.store.jdbc.VersionStatus> status = createEnum("status", org.javersion.store.jdbc.VersionStatus.class);

    public final StringPath str = createString("str");

    public final StringPath type = createString("type");

    public final com.querydsl.sql.PrimaryKey<QDocumentVersionProperty> constraint80 = createPrimaryKey(path, revision);

    public final com.querydsl.sql.ForeignKey<QDocumentVersion> documentVersionPropertyRevisionFk = createForeignKey(revision, "REVISION");

    public QDocumentVersionProperty(String variable) {
        super(QDocumentVersionProperty.class, forVariable(variable), "PUBLIC", "DOCUMENT_VERSION_PROPERTY");
        addMetadata();
    }

    public QDocumentVersionProperty(String variable, String schema, String table) {
        super(QDocumentVersionProperty.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDocumentVersionProperty(Path<? extends QDocumentVersionProperty> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "DOCUMENT_VERSION_PROPERTY");
        addMetadata();
    }

    public QDocumentVersionProperty(PathMetadata metadata) {
        super(QDocumentVersionProperty.class, metadata, "PUBLIC", "DOCUMENT_VERSION_PROPERTY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(nbr, ColumnMetadata.named("NBR").withIndex(6).ofType(Types.BIGINT).withSize(19));
        addMetadata(path, ColumnMetadata.named("PATH").withIndex(3).ofType(Types.VARCHAR).withSize(1024).notNull());
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(2).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(str, ColumnMetadata.named("STR").withIndex(5).ofType(Types.VARCHAR).withSize(1024));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(4).ofType(Types.CHAR).withSize(1).notNull());
    }

}

