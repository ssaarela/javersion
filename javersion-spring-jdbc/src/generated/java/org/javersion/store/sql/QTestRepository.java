package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTestRepository is a Querydsl query type for QTestRepository
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QTestRepository extends com.mysema.query.sql.RelationalPathBase<QTestRepository> {

    private static final long serialVersionUID = 637136265;

    public static final QTestRepository testRepository = new QTestRepository("TEST_REPOSITORY");

    public final StringPath id = createString("id");

    public final NumberPath<Long> ordinal = createNumber("ordinal", Long.class);

    public QTestRepository(String variable) {
        super(QTestRepository.class, forVariable(variable), "PUBLIC", "TEST_REPOSITORY");
        addMetadata();
    }

    public QTestRepository(String variable, String schema, String table) {
        super(QTestRepository.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTestRepository(Path<? extends QTestRepository> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "TEST_REPOSITORY");
        addMetadata();
    }

    public QTestRepository(PathMetadata<?> metadata) {
        super(QTestRepository.class, metadata, "PUBLIC", "TEST_REPOSITORY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(ordinal, ColumnMetadata.named("ORDINAL").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

