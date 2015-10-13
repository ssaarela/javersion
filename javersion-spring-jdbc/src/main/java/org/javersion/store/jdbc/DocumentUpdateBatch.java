/*
 * Copyright 2015 Samppa Saarela
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
