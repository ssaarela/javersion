package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QEntityVersionParent is a Querydsl query type for QEntityVersionParent
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QEntityVersionParent extends com.mysema.query.sql.RelationalPathBase<QEntityVersionParent> {

    private static final long serialVersionUID = 281216146;

    public static final QEntityVersionParent entityVersionParent = new QEntityVersionParent("ENTITY_VERSION_PARENT");

    public final StringPath parentRevision = createString("parentRevision");

    public final StringPath revision = createString("revision");

    public final com.mysema.query.sql.PrimaryKey<QEntityVersionParent> constraintE = createPrimaryKey(parentRevision, revision);

    public final com.mysema.query.sql.ForeignKey<QEntityVersion> entityVersionParentParentRevisionFk = createForeignKey(parentRevision, "REVISION");

    public final com.mysema.query.sql.ForeignKey<QEntityVersion> entityVersionParentRevisionFk = createForeignKey(revision, "REVISION");

    public QEntityVersionParent(String variable) {
        super(QEntityVersionParent.class, forVariable(variable), "PUBLIC", "ENTITY_VERSION_PARENT");
        addMetadata();
    }

    public QEntityVersionParent(String variable, String schema, String table) {
        super(QEntityVersionParent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QEntityVersionParent(Path<? extends QEntityVersionParent> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "ENTITY_VERSION_PARENT");
        addMetadata();
    }

    public QEntityVersionParent(PathMetadata<?> metadata) {
        super(QEntityVersionParent.class, metadata, "PUBLIC", "ENTITY_VERSION_PARENT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(parentRevision, ColumnMetadata.named("PARENT_REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

