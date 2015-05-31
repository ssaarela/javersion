package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTestVersionParent is a Querydsl query type for QTestVersionParent
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QTestVersionParent extends com.mysema.query.sql.RelationalPathBase<QTestVersionParent> {

    private static final long serialVersionUID = 1035624707;

    public static final QTestVersionParent testVersionParent = new QTestVersionParent("TEST_VERSION_PARENT");

    public final SimplePath<org.javersion.core.Revision> parentRevision = createSimple("parentRevision", org.javersion.core.Revision.class);

    public final SimplePath<org.javersion.core.Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final com.mysema.query.sql.PrimaryKey<QTestVersionParent> constraint1 = createPrimaryKey(parentRevision, revision);

    public final com.mysema.query.sql.ForeignKey<QTestVersion> testVersionParentRevisionFk = createForeignKey(revision, "REVISION");

    public final com.mysema.query.sql.ForeignKey<QTestVersion> testVersionParentParentRevisionFk = createForeignKey(parentRevision, "REVISION");

    public QTestVersionParent(String variable) {
        super(QTestVersionParent.class, forVariable(variable), "PUBLIC", "TEST_VERSION_PARENT");
        addMetadata();
    }

    public QTestVersionParent(String variable, String schema, String table) {
        super(QTestVersionParent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTestVersionParent(Path<? extends QTestVersionParent> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "TEST_VERSION_PARENT");
        addMetadata();
    }

    public QTestVersionParent(PathMetadata<?> metadata) {
        super(QTestVersionParent.class, metadata, "PUBLIC", "TEST_VERSION_PARENT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(parentRevision, ColumnMetadata.named("PARENT_REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

