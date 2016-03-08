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

import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPathBase;

public class JRepository extends RelationalPathBase<JRepository> {

    public final StringPath id = createString("id");

    public JRepository(RelationalPathBase<?> table) {
        super(JRepository.class, table.getMetadata(), table.getSchemaName(), table.getTableName());
        table.getColumns().forEach(path -> addMetadata(path, table.getMetadata(path)));
    }

    public JRepository() {
        this("PUBLIC", "REPOSITORY");
    }

    public JRepository(String schema, String table) {
        super(JRepository.class, PathMetadataFactory.forVariable(table), schema, table);
        addMetadata();
    }

    protected void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}
