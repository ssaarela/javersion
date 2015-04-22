package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.StringPath;



/**
 * QVersionParent is a Querydsl query type for QVersionParent
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QVersionParent extends RelationalPathBase<QVersionParent> {

    private static final long serialVersionUID = 1621711349;

    public static final QVersionParent versionParent = new QVersionParent("VERSION_PARENT");

    public final StringPath childRevision = createString("childRevision");

    public final StringPath parentDocId = createString("parentDocId");

    public final StringPath parentRevision = createString("parentRevision");

    public final com.mysema.query.sql.PrimaryKey<QVersionParent> constraint2 = createPrimaryKey(childRevision, parentRevision);

    public final com.mysema.query.sql.ForeignKey<QVersion> versionParentChildRevisionFk = createForeignKey(childRevision, "REVISION");

    public final com.mysema.query.sql.ForeignKey<QVersion> versionParentParentRevisionFk = createForeignKey(parentRevision, "REVISION");

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
        addMetadata(childRevision, ColumnMetadata.named("CHILD_REVISION").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(parentDocId, ColumnMetadata.named("PARENT_DOC_ID").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(parentRevision, ColumnMetadata.named("PARENT_REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

