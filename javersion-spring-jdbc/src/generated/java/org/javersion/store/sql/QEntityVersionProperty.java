package org.javersion.store.sql;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;


/**
 * QEntityVersionProperty is a Querydsl query type for QEntityVersionProperty
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QEntityVersionProperty extends RelationalPathBase<QEntityVersionProperty> {

    private static final long serialVersionUID = 1792442301;

    public static final QEntityVersionProperty entityVersionProperty = new QEntityVersionProperty("ENTITY_VERSION_PROPERTY");

    public final NumberPath<Long> nbr = createNumber("nbr", Long.class);

    public final StringPath path = createString("path");

    public final SimplePath<org.javersion.core.Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final StringPath str = createString("str");

    public final StringPath type = createString("type");

    public final PrimaryKey<QEntityVersionProperty> constraint2 = createPrimaryKey(path, revision);

    public final ForeignKey<QEntityVersion> entityVersionPropertyRevisionFk = createForeignKey(revision, "REVISION");

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

    public QEntityVersionProperty(PathMetadata metadata) {
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

