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
