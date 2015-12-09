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

import java.sql.Types;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.types.PathMetadataFactory;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.SimplePath;
import com.mysema.query.types.path.StringPath;

public class JVersionProperty extends com.mysema.query.sql.RelationalPathBase<JVersionProperty> {

    public final NumberPath<Long> nbr = createNumber("nbr", Long.class);

    public final StringPath path = createString("path");

    public final SimplePath<org.javersion.core.Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final StringPath str = createString("str");

    public final StringPath type = createString("type");

    public JVersionProperty(RelationalPathBase<?> table) {
        super(JVersionProperty.class, table.getMetadata(), table.getSchemaName(), table.getTableName());
        table.getColumns().forEach(path -> addMetadata(path, table.getMetadata(path)));
    }

    public JVersionProperty(String repositoryName) {
        this("PUBLIC", repositoryName + "_VERSION_PROPERTY");
    }

    public JVersionProperty(String schema, String table) {
        super(JVersionProperty.class, PathMetadataFactory.forVariable(table), schema, table);
        addMetadata();
    }

    protected void addMetadata() {
        addMetadata(nbr, ColumnMetadata.named("NBR").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(path, ColumnMetadata.named("PATH").withIndex(2).ofType(Types.VARCHAR).withSize(512).notNull());
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(str, ColumnMetadata.named("STR").withIndex(4).ofType(Types.VARCHAR).withSize(1024));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(3).ofType(Types.CHAR).withSize(1));
    }

}

