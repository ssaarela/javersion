package org.javersion.store.jdbc;

import org.javersion.util.Check;

import com.mysema.query.types.Expression;

public class DocumentStoreOptions<Id> extends StoreOptions<Id> {

    public final Expression<Long> nextOrdinal;

    protected DocumentStoreOptions(Builder<Id> builder) {
        super(builder);
        this.nextOrdinal = Check.notNull(builder.nextOrdinal, "nextOrdinal");
    }

    public static class Builder<Id> extends StoreOptions.Builder<Id> {

        protected Expression<Long> nextOrdinal;

        public Builder<Id> nextOrdinal(Expression<Long> nextOrdinal) {
            this.nextOrdinal = nextOrdinal;
            return this;
        }

    }

}
