package org.javersion.store.jdbc;

import static java.sql.Types.VARCHAR;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nullable;

import org.javersion.core.Revision;

import com.mysema.query.sql.types.AbstractType;
import com.sun.org.apache.bcel.internal.generic.RET;

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
