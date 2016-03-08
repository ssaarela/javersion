package org.javersion.store.jdbc;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.RelationalPathBase;

public class JDocumentVersion<Id> extends JVersion<Id> {

    public final NumberPath<Long> txOrdinal = createNumber("txOrdinal", Long.class);

    public JDocumentVersion(RelationalPathBase<?> table, Path<Id> docId) {
        super(table, docId);
    }
}
