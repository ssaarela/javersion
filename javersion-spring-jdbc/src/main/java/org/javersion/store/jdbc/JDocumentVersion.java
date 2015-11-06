package org.javersion.store.jdbc;

import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.NumberPath;

public class JDocumentVersion<Id> extends JVersion<Id> {

    public final NumberPath<Long> txOrdinal = createNumber("txOrdinal", Long.class);

    public JDocumentVersion(RelationalPathBase<?> table, Path<Id> docId) {
        super(table, docId);
        copyMetadata(table);
    }
}
