package org.javersion.store.jdbc;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;


public class JRepository extends com.mysema.query.sql.RelationalPathBase<JRepository> {

    public final StringPath id = createString("id");

    public final NumberPath<Long> ordinal = createNumber("ordinal", Long.class);

    public JRepository(String schema, String tablePrefix, String alias) {
        super(JRepository.class, forVariable(alias), schema, tablePrefix + "REPOSITORY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(ordinal, ColumnMetadata.named("ORDINAL").withIndex(2).ofType(Types.BIGINT).withSize(19));
    }

}
