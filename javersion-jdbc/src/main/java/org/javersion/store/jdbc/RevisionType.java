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

import static java.sql.Types.VARCHAR;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nullable;

import org.javersion.core.Revision;

import com.querydsl.sql.types.AbstractType;

public class RevisionType extends AbstractType<Revision> {

    public final static RevisionType REVISION_TYPE = new RevisionType();

    public RevisionType() {
        super(VARCHAR);
    }

    @Override
    public Class<Revision> getReturnedClass() {
        return Revision.class;
    }

    @Nullable
    @Override
    public Revision getValue(ResultSet rs, int startIndex) throws SQLException {
        String str = rs.getString(startIndex);
        return str != null ? new Revision(str) : null;
    }

    @Override
    public void setValue(PreparedStatement st, int startIndex, Revision value) throws SQLException {
        if (value != null) {
            st.setString(startIndex, value.toString());
        } else {
            st.setNull(startIndex, VARCHAR);
        }
    }
}
