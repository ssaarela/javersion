package org.javersion.store.jdbc;

import org.javersion.core.VersionNode;
import org.javersion.path.PropertyPath;

import com.mysema.query.sql.dml.SQLInsertClause;

public class DocumentUpdateBatch<Id, M> extends AbstractUpdateBatch<Id, M, DocumentStoreOptions<Id>> {

    public DocumentUpdateBatch(DocumentStoreOptions<Id> options) {
        super(options);
    }

    protected void insertVersion(Id docId, VersionNode<PropertyPath, Object, M> version, SQLInsertClause versionBatch) {
        versionBatch.set(options.version.localOrdinal, options.nextOrdinal);
        super.insertVersion(docId, version);
    }

}
