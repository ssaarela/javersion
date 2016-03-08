package org.javersion.store.sql;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;


/**
 * QEntity is a Querydsl query type for QEntity
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QEntity extends RelationalPathBase<QEntity> {

    private static final long serialVersionUID = 1880310000;

    public static final QEntity entity = new QEntity("ENTITY");

    public final StringPath id = createString("id");

    public final StringPath name = createString("name");

    public final PrimaryKey<QEntity> constraint7 = createPrimaryKey(id);

    public final ForeignKey<QEntityVersion> _entityVersionDocIdFk = createInvForeignKey(id, "DOC_ID");

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

    public QEntity(PathMetadata metadata) {
        super(QEntity.class, metadata, "PUBLIC", "ENTITY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(255));
    }

}

