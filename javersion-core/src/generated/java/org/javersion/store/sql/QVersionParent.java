package org.javersion.store.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import java.util.*;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;

import com.mysema.query.sql.spatial.RelationalPathSpatial;

import com.mysema.query.spatial.path.*;



/**
 * QVersionParent is a Querydsl query type for QVersionParent
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QVersionParent extends RelationalPathSpatial<QVersionParent> {

    private static final long serialVersionUID = 1621711349;

    public static final QVersionParent versionParent = new QVersionParent("VERSION_PARENT");

    public final NumberPath<Long> childRevisionNode = createNumber("childRevisionNode", Long.class);

    public final NumberPath<Long> childRevisionSeq = createNumber("childRevisionSeq", Long.class);

    public final NumberPath<Long> parentRevisionNode = createNumber("parentRevisionNode", Long.class);

    public final NumberPath<Long> parentRevisionSeq = createNumber("parentRevisionSeq", Long.class);

    public final com.mysema.query.sql.PrimaryKey<QVersionParent> constraint2 = createPrimaryKey(childRevisionNode, childRevisionSeq, parentRevisionNode, parentRevisionSeq);

    public final com.mysema.query.sql.ForeignKey<QVersion> versionParentChildRevisionFk = createForeignKey(Arrays.asList(childRevisionSeq, childRevisionNode), Arrays.asList("REVISION_SEQ", "REVISION_NODE"));

    public final com.mysema.query.sql.ForeignKey<QVersion> versionParentParentRevisionFk = createForeignKey(Arrays.asList(parentRevisionSeq, parentRevisionNode), Arrays.asList("REVISION_SEQ", "REVISION_NODE"));

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
        addMetadata(childRevisionNode, ColumnMetadata.named("CHILD_REVISION_NODE").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(childRevisionSeq, ColumnMetadata.named("CHILD_REVISION_SEQ").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(parentRevisionNode, ColumnMetadata.named("PARENT_REVISION_NODE").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(parentRevisionSeq, ColumnMetadata.named("PARENT_REVISION_SEQ").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

