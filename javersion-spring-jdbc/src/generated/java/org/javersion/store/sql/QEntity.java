package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QEntity is a Querydsl query type for QEntity
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QEntity extends com.mysema.query.sql.RelationalPathBase<QEntity> {

    private static final long serialVersionUID = 1880310000;

    public static final QEntity entity = new QEntity("ENTITY");

    public final StringPath id = createString("id");

    public final com.mysema.query.sql.PrimaryKey<QEntity> constraint7 = createPrimaryKey(id);

    public final com.mysema.query.sql.ForeignKey<QEntityVersion> _entityVersionDocIdFk = createInvForeignKey(id, "DOC_ID");

    public QEntity(String variable) {
        super(QEntity.class, forVariable(variable), "PUBLIC", "ENTITY");
        addMetadata();
    }

    public QEntity(String variable, String schema, String table) {
        super(QEntity.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QEntity(Path<? extends QEntity> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "ENTITY");
        addMetadata();
    }

    public QEntity(PathMetadata<?> metadata) {
        super(QEntity.class, metadata, "PUBLIC", "ENTITY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

