package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTestVersionProperty is a Querydsl query type for QTestVersionProperty
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QTestVersionProperty extends com.mysema.query.sql.RelationalPathBase<QTestVersionProperty> {

    private static final long serialVersionUID = 929596398;

    public static final QTestVersionProperty testVersionProperty = new QTestVersionProperty("TEST_VERSION_PROPERTY");

    public final StringPath docId = createString("docId");

    public final NumberPath<Long> nbr = createNumber("nbr", Long.class);

    public final StringPath path = createString("path");

    public final SimplePath<org.javersion.core.Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final StringPath str = createString("str");

    public final StringPath type = createString("type");

    public final com.mysema.query.sql.PrimaryKey<QTestVersionProperty> constraintE = createPrimaryKey(path, revision);

    public final com.mysema.query.sql.ForeignKey<QTestVersion> testVersionPropertyRevisionFk = createForeignKey(revision, "REVISION");

    public QTestVersionProperty(String variable) {
        super(QTestVersionProperty.class, forVariable(variable), "PUBLIC", "TEST_VERSION_PROPERTY");
        addMetadata();
    }

    public QTestVersionProperty(String variable, String schema, String table) {
        super(QTestVersionProperty.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTestVersionProperty(Path<? extends QTestVersionProperty> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "TEST_VERSION_PROPERTY");
        addMetadata();
    }

    public QTestVersionProperty(PathMetadata<?> metadata) {
        super(QTestVersionProperty.class, metadata, "PUBLIC", "TEST_VERSION_PROPERTY");
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

