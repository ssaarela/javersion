package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QVersionParent is a Querydsl query type for QVersionParent
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QVersionParent extends com.mysema.query.sql.RelationalPathBase<QVersionParent> {

    private static final long serialVersionUID = 1621711349;

    public static final QVersionParent versionParent = new QVersionParent("VERSION_PARENT");

    public final SimplePath<org.javersion.core.Revision> parentRevision = createSimple("parentRevision", org.javersion.core.Revision.class);

    public final SimplePath<org.javersion.core.Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final com.mysema.query.sql.PrimaryKey<QVersionParent> constraint2 = createPrimaryKey(parentRevision, revision);

    public final com.mysema.query.sql.ForeignKey<QVersion> versionParentParentRevisionFk = createForeignKey(parentRevision, "REVISION");

    public final com.mysema.query.sql.ForeignKey<QVersion> versionParentRevisionFk = createForeignKey(revision, "REVISION");

    public QVersionParent(String variable) {
        super(QVersionParent.class, forVariable(variable), "PUBLIC", "VERSION_PARENT");
        addMetadata();
    }

    public QVersionParent(String variable, String schema, String table) {
        super(QVersionParent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QVersionParent(Path<? extends QVersionParent> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "VERSION_PARENT");
        addMetadata();
    }

    public QVersionParent(PathMetadata<?> metadata) {
        super(QVersionParent.class, metadata, "PUBLIC", "VERSION_PARENT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(parentRevision, ColumnMetadata.named("PARENT_REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

