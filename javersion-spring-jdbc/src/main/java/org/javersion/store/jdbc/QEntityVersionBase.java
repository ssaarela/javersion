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

import org.javersion.core.Revision;
import org.javersion.core.VersionType;

import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.core.types.PathMetadata;

/**
 * This class may be used as a base for custom QXYZVersion class.
 * Minimum requirement for a custom QXYZVersion is docId field.
 * You may also add custom metadata fields.
 *
 * @param <T>
 */
public abstract class QEntityVersionBase<T> extends RelationalPathBase<T> {

    public final StringPath branch = createString("branch");

    public final NumberPath<Long> localOrdinal = createNumber("localOrdinal", Long.class);

    public final NumberPath<Long> ordinal = createNumber("ordinal", Long.class);

    public final SimplePath<Revision> revision = createSimple("revision", Revision.class);

    public final EnumPath<VersionType> type = createEnum("type", VersionType.class);

    public QEntityVersionBase(Class<? extends T> type, String variable, String schema, String table) {
        super(type, variable, schema, table);
        addMetadata();
    }

    public QEntityVersionBase(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(branch, ColumnMetadata.named("BRANCH").ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(localOrdinal, ColumnMetadata.named("LOCAL_ORDINAL").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(ordinal, ColumnMetadata.named("ORDINAL").ofType(Types.BIGINT).withSize(19));
        addMetadata(revision, ColumnMetadata.named("REVISION").ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(type, ColumnMetadata.named("TYPE").ofType(Types.VARCHAR).withSize(8).notNull());
    }

}