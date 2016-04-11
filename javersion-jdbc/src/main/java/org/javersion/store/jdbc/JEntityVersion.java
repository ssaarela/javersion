package org.javersion.store.jdbc;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.RelationalPathBase;

public class JEntityVersion<Id> extends JVersion<Id> {

    public final NumberPath<Long> localOrdinal = createNumber("localOrdinal", Long.class);

    public JEntityVersion(RelationalPathBase<?> table, Path<Id> docId) {
        super(table, docId);
    }

}
