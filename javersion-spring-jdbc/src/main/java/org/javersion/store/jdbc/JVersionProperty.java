package org.javersion.store.jdbc;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.SimplePath;
import com.mysema.query.types.path.StringPath;

public class JVersionProperty extends com.mysema.query.sql.RelationalPathBase<JVersionProperty> {

    public final StringPath docId = createString("docId");

    public final NumberPath<Long> nbr = createNumber("nbr", Long.class);

    public final StringPath path = createString("path");

    public final SimplePath<org.javersion.core.Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final StringPath str = createString("str");

    public final StringPath type = createString("type");

    public final com.mysema.query.sql.PrimaryKey<JVersionProperty> constraint38 = createPrimaryKey(path, revision);

    public final com.mysema.query.sql.ForeignKey<JVersion> versionPropertyRevisionFk = createForeignKey(revision, "REVISION");

    public JVersionProperty(String schema, String tablePrefix, String alias) {
        super(JVersionProperty.class, forVariable(alias), schema, tablePrefix + "VERSION_PROPERTY");
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

