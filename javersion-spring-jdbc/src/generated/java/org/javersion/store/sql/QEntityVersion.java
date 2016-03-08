package org.javersion.store.sql;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;


/**
 * QEntityVersion is a Querydsl query type for QEntityVersion
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QEntityVersion extends RelationalPathBase<QEntityVersion> {

    private static final long serialVersionUID = 185370056;

    public static final QEntityVersion entityVersion = new QEntityVersion("ENTITY_VERSION");

    public final StringPath branch = createString("branch");

    public final StringPath comment = createString("comment");

    public final StringPath docId = createString("docId");

    public final NumberPath<Long> localOrdinal = createNumber("localOrdinal", Long.class);

    public final NumberPath<Long> ordinal = createNumber("ordinal", Long.class);

    public final SimplePath<org.javersion.core.Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final EnumPath<org.javersion.core.VersionType> type = createEnum("type", org.javersion.core.VersionType.class);

    public final PrimaryKey<QEntityVersion> constraint86 = createPrimaryKey(revision);

    public final ForeignKey<QEntity> entityVersionDocIdFk = createForeignKey(docId, "ID");

    public final ForeignKey<QVersionType> entityVersionTypeFk = createForeignKey(type, "NAME");

    public final ForeignKey<QEntityVersionParent> _entityVersionParentParentRevisionFk = createInvForeignKey(revision, "PARENT_REVISION");

    public final ForeignKey<QEntityVersionParent> _entityVersionParentRevisionFk = createInvForeignKey(revision, "REVISION");

    public final ForeignKey<QEntityVersionProperty> _entityVersionPropertyRevisionFk = createInvForeignKey(revision, "REVISION");

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

    public QEntityVersion(PathMetadata metadata) {
        super(QEntityVersion.class, metadata, "PUBLIC", "ENTITY_VERSION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(branch, ColumnMetadata.named("BRANCH").withIndex(5).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(comment, ColumnMetadata.named("COMMENT").withIndex(7).ofType(Types.VARCHAR).withSize(255));
        addMetadata(docId, ColumnMetadata.named("DOC_ID").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(localOrdinal, ColumnMetadata.named("LOCAL_ORDINAL").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(ordinal, ColumnMetadata.named("ORDINAL").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(8).notNull());
    }

}

