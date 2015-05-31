package org.javersion.store.jdbc;

import com.mysema.query.types.Path;
import com.mysema.query.types.expr.SimpleExpression;

public class Column<T> {

    public final SimpleExpression<T> expr;

    public final Path<T> path;

    public <P extends SimpleExpression<T> & Path<T>> Column(P path) {
        this.path = path;
        this.expr = path;
    }

}
