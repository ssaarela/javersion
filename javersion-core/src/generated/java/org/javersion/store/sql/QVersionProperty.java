package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;

import com.mysema.query.sql.spatial.RelationalPathSpatial;

import com.mysema.query.spatial.path.*;



/**
 * QVersionProperty is a Querydsl query type for QVersionProperty
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QVersionProperty extends RelationalPathSpatial<QVersionProperty> {

    private static final long serialVersionUID = 1518143584;

    public static final QVersionProperty versionProperty = new QVersionProperty("VERSION_PROPERTY");

    public final StringPath docId = createString("docId");

    public final NumberPath<Long> nbr = createNumber("nbr", Long.class);

    public final StringPath path = createString("path");

    public final StringPath revision = createString("revision");

    public final StringPath str = createString("str");

    public final StringPath type = createString("type");

    public final com.mysema.query.sql.PrimaryKey<QVersionProperty> constraint38 = createPrimaryKey(path, revision);

    public final com.mysema.query.sql.ForeignKey<QVersion> versionPropertyRevisionFk = createForeignKey(revision, "REVISION");

    public QVersionProperty(String variable) {
        super(QVersionProperty.class, forVariable(variable), "PUBLIC", "VERSION_PROPERTY");
        addMetadata();
    }

    public QVersionProperty(String variable, String schema, String table) {
        super(QVersionProperty.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QVersionProperty(Path<? extends QVersionProperty> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "VERSION_PROPERTY");
        addMetadata();
    }

    public QVersionProperty(PathMetadata<?> metadata) {
        super(QVersionProperty.class, metadata, "PUBLIC", "VERSION_PROPERTY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(docId, ColumnMetadata.named("DOC_ID").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(nbr, ColumnMetadata.named("NBR").withIndex(6).ofType(Types.BIGINT).withSize(19));
        addMetadata(path, ColumnMetadata.named("PATH").withIndex(3).ofType(Types.VARCHAR).withSize(512).notNull());
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(str, ColumnMetadata.named("STR").withIndex(5).ofType(Types.VARCHAR).withSize(1024));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(4).ofType(Types.CHAR).withSize(1));
    }

}

