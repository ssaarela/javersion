package org.javersion.store.sql;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.sql.ColumnMetadata;




/**
 * QEntityVersionParent is a Querydsl query type for QEntityVersionParent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QEntityVersionParent extends com.querydsl.sql.RelationalPathBase<QEntityVersionParent> {

    private static final long serialVersionUID = 281216146;

    public static final QEntityVersionParent entityVersionParent = new QEntityVersionParent("ENTITY_VERSION_PARENT");

    public final SimplePath<org.javersion.core.Revision> parentRevision = createSimple("parentRevision", org.javersion.core.Revision.class);

    public final SimplePath<org.javersion.core.Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final EnumPath<org.javersion.store.jdbc.VersionStatus> status = createEnum("status", org.javersion.store.jdbc.VersionStatus.class);

    public final com.querydsl.sql.PrimaryKey<QEntityVersionParent> constraintE = createPrimaryKey(parentRevision, revision);

    public final com.querydsl.sql.ForeignKey<QEntityVersion> entityVersionParentParentRevisionFk = createForeignKey(parentRevision, "REVISION");

    public final com.querydsl.sql.ForeignKey<QEntityVersion> entityVersionParentRevisionFk = createForeignKey(revision, "REVISION");

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

    public QEntityVersionParent(PathMetadata metadata) {
        super(QEntityVersionParent.class, metadata, "PUBLIC", "ENTITY_VERSION_PARENT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(parentRevision, ColumnMetadata.named("PARENT_REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(3).ofType(Types.DECIMAL).withSize(1).notNull());
    }

}

