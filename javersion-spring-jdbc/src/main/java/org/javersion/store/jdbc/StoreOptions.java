package org.javersion.store.jdbc;

import javax.annotation.Nullable;

import org.javersion.path.PropertyPath;
import org.javersion.util.Check;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.mysema.query.sql.SQLQueryFactory;
import com.mysema.query.types.Path;

public class StoreOptions<Id> {

    public final String repositoryId;

    public final JRepository repository;

    public final JVersion<Id> version;

    public final JVersionParent parent;

    public final JVersionProperty property;

    public final ImmutableMap<PropertyPath, Path<?>> versionTableProperties;

    final SQLQueryFactory queryFactory;

    final CacheBuilder<Object, Object> cacheBuilder;

    protected StoreOptions(Builder<Id> builder) {
        this.repositoryId = Check.notNull(builder.repositoryId, "repositoryId");
        this.repository = Check.notNull(builder.repository, "jRepository");
        this.version = Check.notNull(builder.version, "jVersion");
        this.parent = Check.notNull(builder.parent, "jParent");
        this.property = Check.notNull(builder.property, "jProperty");
        this.versionTableProperties = builder.versionTableProperties != null
                ? ImmutableMap.copyOf(builder.versionTableProperties)
                : ImmutableMap.of();
        this.queryFactory = Check.notNull(builder.queryFactory, "queryFactory");
        this.cacheBuilder = builder.cacheBuilder;
    }

    public static class Builder<Id> {

        protected String repositoryId = "repository";

        protected JRepository repository;

        protected JVersion<Id> version;

        protected JVersionParent parent;

        protected JVersionProperty property;

        @Nullable
        protected ImmutableMap<PropertyPath, Path<?>> versionTableProperties;

        protected SQLQueryFactory queryFactory;

        @Nullable
        protected CacheBuilder<Object, Object> cacheBuilder;

        public Builder<Id> repositoryId(String repositoryId) {
            this.repositoryId = repositoryId;
            return this;
        }

        public Builder<Id> repository(JRepository jRepository) {
            this.repository = jRepository;
            return this;
        }

        public Builder<Id> version(JVersion<Id> jVersion) {
            this.version = jVersion;
            return this;
        }

        public Builder<Id> parent(JVersionParent jParent) {
            this.parent = jParent;
            return this;
        }

        public Builder<Id> property(JVersionProperty jProperty) {
            this.property = jProperty;
            return this;
        }

        public Builder<Id> versionTableProperties(ImmutableMap<PropertyPath, Path<?>> versionTableProperties) {
            this.versionTableProperties = versionTableProperties;
            return this;
        }

        public Builder<Id> queryFactory(SQLQueryFactory queryFactory) {
            this.queryFactory = queryFactory;
            return this;
        }

        public Builder<Id> cacheBuilder(CacheBuilder<Object, Object> cacheBuilder) {
            this.cacheBuilder = cacheBuilder;
            return this;
        }

        public StoreOptions<Id> build() {
            return new StoreOptions<>(this);
        }
    }
}
