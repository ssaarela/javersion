package org.javersion.store.jdbc;

import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.NumberPath;

public class JEntityVersion<Id> extends JVersion<Id> {

    public final NumberPath<Long> localOrdinal = createNumber("localOrdinal", Long.class);

    public JEntityVersion(RelationalPathBase<?> table, Path<Id> docId) {
        super(table, docId);
        copyMetadata(table);
    }
}
