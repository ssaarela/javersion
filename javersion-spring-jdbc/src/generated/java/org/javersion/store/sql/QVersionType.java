package org.javersion.store.sql;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;




/**
 * QVersionType is a Querydsl query type for QVersionType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QVersionType extends com.querydsl.sql.RelationalPathBase<QVersionType> {

    private static final long serialVersionUID = 1436464965;

    public static final QVersionType versionType = new QVersionType("VERSION_TYPE");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QVersionType> constraint8 = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<QDocumentVersion> _documentVersionTypeFk = createInvForeignKey(name, "TYPE");

    public final com.querydsl.sql.ForeignKey<QEntityVersion> _entityVersionTypeFk = createInvForeignKey(name, "TYPE");

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

    public QVersionType(PathMetadata metadata) {
        super(QVersionType.class, metadata, "PUBLIC", "VERSION_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(1).ofType(Types.VARCHAR).withSize(8).notNull());
    }

}

