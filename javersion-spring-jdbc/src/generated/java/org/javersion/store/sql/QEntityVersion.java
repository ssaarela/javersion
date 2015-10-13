package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QEntityVersion is a Querydsl query type for QEntityVersion
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QEntityVersion extends com.mysema.query.sql.RelationalPathBase<QEntityVersion> {

    private static final long serialVersionUID = 185370056;

    public static final QEntityVersion entityVersion = new QEntityVersion("ENTITY_VERSION");

    public final StringPath branch = createString("branch");

    public final StringPath docId = createString("docId");

    public final NumberPath<Long> localOrdinal = createNumber("localOrdinal", Long.class);

    public final NumberPath<Long> ordinal = createNumber("ordinal", Long.class);

    public final StringPath revision = createString("revision");

    public final StringPath type = createString("type");

    public final com.mysema.query.sql.PrimaryKey<QEntityVersion> constraint86 = createPrimaryKey(revision);

    public final com.mysema.query.sql.ForeignKey<QEntity> entityVersionDocIdFk = createForeignKey(docId, "ID");

    public final com.mysema.query.sql.ForeignKey<QVersionType> entityVersionTypeFk = createForeignKey(type, "NAME");

    public final com.mysema.query.sql.ForeignKey<QEntityVersionParent> _entityVersionParentParentRevisionFk = createInvForeignKey(revision, "PARENT_REVISION");

    public final com.mysema.query.sql.ForeignKey<QEntityVersionParent> _entityVersionParentRevisionFk = createInvForeignKey(revision, "REVISION");

    public final com.mysema.query.sql.ForeignKey<QEntityVersionProperty> _entityVersionPropertyRevisionFk = createInvForeignKey(revision, "REVISION");

    public QEntityVersion(String variable) {
        super(QEntityVersion.class, forVariable(variable), "PUBLIC", "ENTITY_VERSION");
        addMetadata();
    }

    public QEntityVersion(String variable, String schema, String table) {
        super(QEntityVersion.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QEntityVersion(Path<? extends QEntityVersion> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "ENTITY_VERSION");
        addMetadata();
    }

    public QEntityVersion(PathMetadata<?> metadata) {
        super(QEntityVersion.class, metadata, "PUBLIC", "ENTITY_VERSION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(branch, ColumnMetadata.named("BRANCH").withIndex(5).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(docId, ColumnMetadata.named("DOC_ID").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(localOrdinal, ColumnMetadata.named("LOCAL_ORDINAL").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(ordinal, ColumnMetadata.named("ORDINAL").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(8).notNull());
    }

}

