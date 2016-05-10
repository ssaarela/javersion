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

import com.google.common.collect.ImmutableMap;
import com.querydsl.core.types.Path;
import com.querydsl.sql.SQLQueryFactory;
import org.javersion.core.VersionNode;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.javersion.util.Check;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Immutable
public abstract class StoreOptions<Id, M, V extends JVersion<Id>> extends GraphOptions<Id, M> {

    public final V version;

    public final V sinceVersion;

    public final JVersionParent parent;

    public final JVersionProperty property;

    public final ImmutableMap<PropertyPath, Path<?>> versionTableProperties;

    public final Transactions transactions;

    public final Executor optimizer;

    public final Executor publisher;

    public final Function<VersionStore<Id, M>, GraphCache<Id, M>> cacheBuilder;

    public final SQLQueryFactory queryFactory;

    protected StoreOptions(AbstractBuilder<Id, M, V, ?, ?> builder) {
        super(builder.optimizeWhen, builder.optimizeKeep);
        this.version = Check.notNull(builder.version, "versionTable");
        this.sinceVersion = Check.notNull(builder.versionTableSince, "versionTableSince");
        this.parent = Check.notNull(builder.parentTable, "parentTable");
        this.property = Check.notNull(builder.propertyTable, "propertyTable");
        this.versionTableProperties = builder.versionTableProperties != null
                ? ImmutableMap.copyOf(builder.versionTableProperties)
                : ImmutableMap.of();
        this.transactions = Check.notNull(builder.transactions, "transactions");
        this.optimizer = builder.optimizer;
        this.publisher = builder.publisher;
        this.cacheBuilder = firstNonNull(builder.cacheBuilder, store -> null);
        this.queryFactory = Check.notNull(builder.queryFactory, "queryFactory");
    }

    public abstract AbstractBuilder<Id, M, V, ?, ?> toBuilder();

    public abstract static class AbstractBuilder<Id, M, V extends JVersion<Id>,
            Options extends StoreOptions<Id, M, V>,
            This extends AbstractBuilder<Id, M, V, Options,This>> {

        private static final Executor SYNCHRONOUS_EXECUTOR = Runnable::run;

        protected V version;

        protected V versionTableSince;

        protected JVersionParent parentTable;

        protected JVersionProperty propertyTable;

        protected Predicate<ObjectVersionGraph<M>> optimizeWhen;

        protected Function<ObjectVersionGraph<M>, Predicate<VersionNode<PropertyPath, Object, M>>> optimizeKeep;

        protected Transactions transactions;

        protected Executor optimizer;

        protected Executor publisher;

        @Nullable
        protected ImmutableMap<PropertyPath, Path<?>> versionTableProperties;

        protected Function<VersionStore<Id, M>, GraphCache<Id, M>> cacheBuilder;

        protected SQLQueryFactory queryFactory;

        public AbstractBuilder() {}

        public AbstractBuilder(StoreOptions<Id, M, V> options) {
            this.version = options.version;
            this.versionTableSince = options.sinceVersion;
            this.parentTable = options.parent;
            this.propertyTable = options.property;
            this.optimizeWhen = options.optimizeWhen;
            this.optimizeKeep = options.optimizeKeep;
            this.transactions = options.transactions;
            this.optimizer = options.optimizer;
            this.publisher = options.publisher;
            this.versionTableProperties = options.versionTableProperties;
            this.queryFactory = options.queryFactory;
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
            return optimizeWhen(graphOptions.optimizeWhen).optimizeKeep(graphOptions.optimizeKeep);
        }

        public This optimizeWhen(Predicate<ObjectVersionGraph<M>> optimizeWhen) {
            this.optimizeWhen = optimizeWhen;
            return self();
        }

        public This optimizeKeep(Function<ObjectVersionGraph<M>, Predicate<VersionNode<PropertyPath, Object, M>>> optimizeKeep) {
            this.optimizeKeep = optimizeKeep;
            return self();
        }

        public This publisherType(ExecutorType type) {
            switch (type) {
                case ASYNC:
                    return publisher(new ThreadPoolExecutor(1, 1,
                            0L, MILLISECONDS,
                            new ArrayBlockingQueue<>(2),
                            new ThreadPoolExecutor.DiscardPolicy()));
                case SYNC:
                    return publisher(SYNCHRONOUS_EXECUTOR);
                default:
                    return publisher(null);
            }
        }

        public This optimizerType(ExecutorType type) {
            switch (type) {
                case ASYNC:
                    return optimizer(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
                case SYNC:
                    return optimizer(SYNCHRONOUS_EXECUTOR);
                default:
                    return optimizer(null);
            }
        }

        public This transactions(Transactions transactions) {
            this.transactions = transactions;
            return self();
        }

        public This optimizer(Executor optimizer) {
            this.optimizer = optimizer;
            return self();
        }

        public This publisher(Executor publisher) {
            this.publisher = publisher;
            return self();
        }

        public This versionTableProperties(ImmutableMap<PropertyPath, Path<?>> versionTableProperties) {
            this.versionTableProperties = versionTableProperties;
            return self();
        }

        public This cacheBuilder(Function<VersionStore<Id, M>, GraphCache<Id, M>> cacheBuilder) {
            this.cacheBuilder = cacheBuilder;
            return self();
        }

        public This defaultsFor(String repositoryName) {
            return parentTable(new JVersionParent(repositoryName))
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
