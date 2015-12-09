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

import org.javersion.util.Check;

import com.mysema.query.sql.SQLQueryFactory;

public class EntityStoreOptions<Id extends Comparable, V extends JEntityVersion<Id>> extends StoreOptions<Id, V> {

    public static <Id extends Comparable, V extends JEntityVersion<Id>> Builder<Id, V> builder() {
        return new Builder<>();
    }

    public final JEntity<Id> entity;

    protected EntityStoreOptions(Builder<Id, V> builder) {
        super(builder);
        this.entity = Check.notNull(builder.entity, "entity");
    }

    public static class Builder<Id extends Comparable, V extends JEntityVersion<Id>> extends AbstractBuilder<Id, V, EntityStoreOptions<Id, V>, Builder<Id, V>> {

        private JEntity<Id> entity;

        public Builder<Id, V> entityTable(JEntity<Id> entity) {
            this.entity = entity;
            return this;
        }

        @Override
        public EntityStoreOptions<Id, V> build() {
            return new EntityStoreOptions<>(this);
        }

    }

}
