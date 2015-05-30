package org.javersion.store.jdbc;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.path.SimplePath;

public class JVersionParent extends com.mysema.query.sql.RelationalPathBase<JVersionParent> {

    public final SimplePath<org.javersion.core.Revision> parentRevision = createSimple("parentRevision", org.javersion.core.Revision.class);

    public final SimplePath<org.javersion.core.Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final com.mysema.query.sql.ForeignKey<JVersion> versionParentParentRevisionFk = createForeignKey(parentRevision, "REVISION");

    public final com.mysema.query.sql.ForeignKey<JVersion> versionParentRevisionFk = createForeignKey(revision, "REVISION");

    public JVersionParent(String schema, String tablePrefix, String alias) {
        super(JVersionParent.class, forVariable(alias), schema, tablePrefix + "VERSION_PARENT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(parentRevision, ColumnMetadata.named("PARENT_REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

