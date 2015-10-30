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

public class EntityStoreOptions<Id extends Comparable> extends StoreOptions<Id, JEntityVersion<Id>> {

    public final JEntity<Id> entity;

    protected EntityStoreOptions(Builder<Id> builder) {
        super(builder);
        this.entity = builder.entity;
    }

    public static class Builder<Id extends Comparable> extends AbstractBuilder<Id, JEntityVersion<Id>, Builder<Id>> {

        private JEntity<Id> entity;

        public Builder<Id> entityTable(JEntity<Id> entity) {
            this.entity = entity;
            return this;
        }

        @Override
        public EntityStoreOptions<Id> build() {
            return new EntityStoreOptions<>(this);
        }

    }

}
