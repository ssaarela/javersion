package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.StringPath;




/**
 * QRepository is a Querydsl query type for QRepository
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QRepository extends com.mysema.query.sql.RelationalPathBase<QRepository> {

    private static final long serialVersionUID = -1904416297;

    public static final QRepository repository = new QRepository("REPOSITORY");

    public final StringPath id = createString("id");

    public QRepository(String variable) {
        super(QRepository.class, forVariable(variable), "PUBLIC", "REPOSITORY");
        addMetadata();
    }

    public QRepository(String variable, String schema, String table) {
        super(QRepository.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRepository(Path<? extends QRepository> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "REPOSITORY");
        addMetadata();
    }

    public QRepository(PathMetadata<?> metadata) {
        super(QRepository.class, metadata, "PUBLIC", "REPOSITORY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

