package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDocumentRepository is a Querydsl query type for QDocumentRepository
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QDocumentRepository extends com.mysema.query.sql.RelationalPathBase<QDocumentRepository> {

    private static final long serialVersionUID = -243510190;

    public static final QDocumentRepository documentRepository = new QDocumentRepository("DOCUMENT_REPOSITORY");

    public final StringPath id = createString("id");

    public final NumberPath<Long> ordinal = createNumber("ordinal", Long.class);

    public QDocumentRepository(String variable) {
        super(QDocumentRepository.class, forVariable(variable), "PUBLIC", "DOCUMENT_REPOSITORY");
        addMetadata();
    }

    public QDocumentRepository(String variable, String schema, String table) {
        super(QDocumentRepository.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDocumentRepository(Path<? extends QDocumentRepository> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "DOCUMENT_REPOSITORY");
        addMetadata();
    }

    public QDocumentRepository(PathMetadata<?> metadata) {
        super(QDocumentRepository.class, metadata, "PUBLIC", "DOCUMENT_REPOSITORY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(ordinal, ColumnMetadata.named("ORDINAL").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

