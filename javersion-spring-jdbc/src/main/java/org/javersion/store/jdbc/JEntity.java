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

import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.types.Path;

public class JEntity<Id> extends RelationalPathBase<JEntity>  {

    public final Path<Id> id;

    public JEntity(RelationalPathBase<?> table, Path<Id> id) {
        super(JEntity.class, table.getMetadata(), table.getSchemaName(), table.getTableName());
        this.id = id;
        table.getColumns().forEach(path -> addMetadata(path, table.getMetadata(path)));
    }

}
