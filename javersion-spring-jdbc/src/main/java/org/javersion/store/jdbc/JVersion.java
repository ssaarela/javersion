package org.javersion.store.jdbc;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import org.javersion.core.Revision;
import org.javersion.core.VersionType;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.path.EnumPath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.SimplePath;
import com.mysema.query.types.path.StringPath;

public class JVersion extends com.mysema.query.sql.RelationalPathBase<JVersion>  {

    public final StringPath branch = createString("branch");

    public final StringPath docId = createString("docId");

    public final NumberPath<Long> ordinal = createNumber("ordinal", Long.class);

    public final SimplePath<Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final StringPath tx = createString("tx");

    public final EnumPath<VersionType> type = createEnum("type", org.javersion.core.VersionType.class);

    public final com.mysema.query.sql.ForeignKey<JVersionProperty> _versionPropertyRevisionFk = createInvForeignKey(revision, "REVISION");

    public final com.mysema.query.sql.ForeignKey<JVersionParent> _versionParentParentRevisionFk = createInvForeignKey(revision, "PARENT_REVISION");

    public final com.mysema.query.sql.ForeignKey<JVersionParent> _versionParentRevisionFk = createInvForeignKey(revision, "REVISION");

    public JVersion(String schema, String tablePrefix, String alias) {
        super(JVersion.class, forVariable(alias), schema, tablePrefix + "VERSION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(branch, ColumnMetadata.named("BRANCH").withIndex(5).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(docId, ColumnMetadata.named("DOC_ID").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(ordinal, ColumnMetadata.named("ORDINAL").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(tx, ColumnMetadata.named("TX").withIndex(4).ofType(Types.VARCHAR).withSize(32));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(8).notNull());
    }

}
