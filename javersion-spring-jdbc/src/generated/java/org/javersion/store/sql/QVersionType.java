package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QVersionType is a Querydsl query type for QVersionType
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QVersionType extends com.mysema.query.sql.RelationalPathBase<QVersionType> {

    private static final long serialVersionUID = 1436464965;

    public static final QVersionType versionType = new QVersionType("VERSION_TYPE");

    public final StringPath name = createString("name");

    public final com.mysema.query.sql.PrimaryKey<QVersionType> constraint8 = createPrimaryKey(name);

    public final com.mysema.query.sql.ForeignKey<QDocumentVersion> _documentVersionTypeFk = createInvForeignKey(name, "TYPE");

    public QVersionType(String variable) {
        super(QVersionType.class, forVariable(variable), "PUBLIC", "VERSION_TYPE");
        addMetadata();
    }

    public QVersionType(String variable, String schema, String table) {
        super(QVersionType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QVersionType(Path<? extends QVersionType> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "VERSION_TYPE");
        addMetadata();
    }

    public QVersionType(PathMetadata<?> metadata) {
        super(QVersionType.class, metadata, "PUBLIC", "VERSION_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(1).ofType(Types.VARCHAR).withSize(8).notNull());
    }

}

