package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QEntityVersionProperty is a Querydsl query type for QEntityVersionProperty
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QEntityVersionProperty extends com.mysema.query.sql.RelationalPathBase<QEntityVersionProperty> {

    private static final long serialVersionUID = 1792442301;

    public static final QEntityVersionProperty entityVersionProperty = new QEntityVersionProperty("ENTITY_VERSION_PROPERTY");

    public final NumberPath<Long> nbr = createNumber("nbr", Long.class);

    public final StringPath path = createString("path");

    public final StringPath revision = createString("revision");

    public final StringPath str = createString("str");

    public final StringPath type = createString("type");

    public final com.mysema.query.sql.PrimaryKey<QEntityVersionProperty> constraint2 = createPrimaryKey(path, revision);

    public final com.mysema.query.sql.ForeignKey<QEntityVersion> entityVersionPropertyRevisionFk = createForeignKey(revision, "REVISION");

    public QEntityVersionProperty(String variable) {
        super(QEntityVersionProperty.class, forVariable(variable), "PUBLIC", "ENTITY_VERSION_PROPERTY");
        addMetadata();
    }

    public QEntityVersionProperty(String variable, String schema, String table) {
        super(QEntityVersionProperty.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QEntityVersionProperty(Path<? extends QEntityVersionProperty> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "ENTITY_VERSION_PROPERTY");
        addMetadata();
    }

    public QEntityVersionProperty(PathMetadata<?> metadata) {
        super(QEntityVersionProperty.class, metadata, "PUBLIC", "ENTITY_VERSION_PROPERTY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(nbr, ColumnMetadata.named("NBR").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(path, ColumnMetadata.named("PATH").withIndex(2).ofType(Types.VARCHAR).withSize(512).notNull());
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(str, ColumnMetadata.named("STR").withIndex(4).ofType(Types.VARCHAR).withSize(1024));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(3).ofType(Types.CHAR).withSize(1));
    }

}

