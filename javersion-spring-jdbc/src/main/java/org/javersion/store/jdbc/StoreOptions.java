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

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.javersion.path.PropertyPath;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableMap;
import com.querydsl.core.types.Path;
import com.querydsl.sql.SQLQueryFactory;

@Immutable
public class StoreOptions<Id, M, V extends JVersion<Id>> {

    public final String repositoryId;

    public final JRepository repository;

    public final V version;

    public final V sinceVersion;

    public final JVersionParent parent;

    public final JVersionProperty property;

    public final ImmutableMap<PropertyPath, Path<?>> versionTableProperties;

    public final GraphOptions<Id, M> graphOptions;

    public final SQLQueryFactory queryFactory;

    protected StoreOptions(AbstractBuilder<Id, M, V, ?, ?> builder) {
        this.repositoryId = Check.notNull(builder.repositoryId, "repositoryId");
        this.repository = Check.notNull(builder.repositoryTable, "repositoryTable");
        this.version = Check.notNull(builder.version, "versionTable");
        this.sinceVersion = Check.notNull(builder.versionTableSince, "versionTableSince");
        this.parent = Check.notNull(builder.parentTable, "parentTable");
        this.property = Check.notNull(builder.propertyTable, "propertyTable");
        this.versionTableProperties = builder.versionTableProperties != null
                ? ImmutableMap.copyOf(builder.versionTableProperties)
                : ImmutableMap.of();
        this.graphOptions = Check.notNull(builder.graphOptions, "graphOptions");
        this.queryFactory = Check.notNull(builder.queryFactory, "queryFactory");
    }

    public abstract static class AbstractBuilder<Id, M, V extends JVersion<Id>,
            Options extends StoreOptions<Id, M, V>,
            This extends AbstractBuilder<Id, M, V, Options,This>> {

        protected String repositoryId = "repository";

        protected JRepository repositoryTable;

        protected V version;

        protected V versionTableSince;

        protected JVersionParent parentTable;

        protected JVersionProperty propertyTable;

        protected GraphOptions<Id, M> graphOptions = new GraphOptions<>();

        @Nullable
        protected ImmutableMap<PropertyPath, Path<?>> versionTableProperties;

        protected SQLQueryFactory queryFactory;

        public This repositoryId(String repositoryId) {
            this.repositoryId = repositoryId;
            return self();
        }

        public This repositoryTable(JRepository jRepository) {
            this.repositoryTable = jRepository;
            return self();
        }

        public This versionTableSince(V sinceVersion) {
            this.versionTableSince = sinceVersion;
            return self();
        }

        public This versionTable(V version) {
            this.version = version;
            return self();
        }

        public This parentTable(JVersionParent jParent) {
            this.parentTable = jParent;
            return self();
        }

        public This propertyTable(JVersionProperty jProperty) {
            this.propertyTable = jProperty;
            return self();
        }

        public This graphOptions(GraphOptions<Id, M> graphOptions) {
            this.graphOptions = graphOptions;
            return self();
        }

        public This versionTableProperties(ImmutableMap<PropertyPath, Path<?>> versionTableProperties) {
            this.versionTableProperties = versionTableProperties;
            return self();
        }

        public This defaultsFor(String repositoryName) {
            return repositoryId(repositoryName)
                    .repositoryTable(new JRepository())
                    .parentTable(new JVersionParent(repositoryName))
                    .propertyTable(new JVersionProperty(repositoryName));
        }

        public This queryFactory(SQLQueryFactory queryFactory) {
            this.queryFactory = queryFactory;
            return self();
        }

        public abstract Options build();

        public Options build(SQLQueryFactory queryFactory) {
            return queryFactory(queryFactory).build();
        }

        @SuppressWarnings("unchecked")
        public This self() {
            return (This) this;
        }
    }
}
