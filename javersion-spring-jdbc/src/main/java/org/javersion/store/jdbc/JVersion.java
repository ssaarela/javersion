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

import org.javersion.core.Revision;
import org.javersion.core.VersionType;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.path.EnumPath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.SimplePath;
import com.mysema.query.types.path.StringPath;

public class JVersion extends com.mysema.query.sql.RelationalPathBase<JVersion>  {

    public final StringPath branch = createString("branch");

    public final NumberPath<Long> ordinal = createNumber("ordinal", Long.class);

    public final SimplePath<Revision> revision = createSimple("revision", org.javersion.core.Revision.class);

    public final NumberPath<Long> txOrdinal = createNumber("txOrdinal", Long.class);

    public final EnumPath<VersionType> type = createEnum("type", org.javersion.core.VersionType.class);

    public final com.mysema.query.sql.ForeignKey<JVersionProperty> _versionPropertyRevisionFk = createInvForeignKey(revision, "REVISION");

    public final com.mysema.query.sql.ForeignKey<JVersionParent> _versionParentParentRevisionFk = createInvForeignKey(revision, "PARENT_REVISION");

    public final com.mysema.query.sql.ForeignKey<JVersionParent> _versionParentRevisionFk = createInvForeignKey(revision, "REVISION");

    public JVersion(String schema, String tablePrefix, String alias) {
        super(JVersion.class, forVariable(alias), schema, tablePrefix + "VERSION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(revision, ColumnMetadata.named("REVISION").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(ordinal, ColumnMetadata.named("ORDINAL").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(txOrdinal, ColumnMetadata.named("TX_ORDINAL").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(branch, ColumnMetadata.named("BRANCH").withIndex(5).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(8).notNull());
    }

}
