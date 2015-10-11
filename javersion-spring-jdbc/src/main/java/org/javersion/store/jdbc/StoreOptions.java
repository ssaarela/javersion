package org.javersion.store.jdbc;

import java.util.function.Function;

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

    public final JVersion<Id> sinceVersion;

    public final JVersionParent parent;

    public final JVersionProperty property;

    public final ImmutableMap<PropertyPath, Path<?>> versionTableProperties;

    final SQLQueryFactory queryFactory;

    final CacheBuilder<Object, Object> cacheBuilder;

    protected StoreOptions(AbstractBuilder<Id, ?> builder) {
        this.repositoryId = Check.notNull(builder.repositoryId, "repositoryId");
        this.repository = Check.notNull(builder.repository, "repository");
        this.version = Check.notNull(builder.version, "version");
        this.sinceVersion = Check.notNull(builder.sinceVersion, "sinceVersion");
        this.parent = Check.notNull(builder.parent, "parent");
        this.property = Check.notNull(builder.property, "property");
        this.versionTableProperties = builder.versionTableProperties != null
                ? ImmutableMap.copyOf(builder.versionTableProperties)
                : ImmutableMap.of();
        this.queryFactory = Check.notNull(builder.queryFactory, "queryFactory");
        this.cacheBuilder = builder.cacheBuilder;
    }

    public abstract static class AbstractBuilder<Id, This extends AbstractBuilder<Id, This>> {

        protected String repositoryId = "repository";

        protected JRepository repository;

        protected JVersion<Id> version;

        protected JVersion<Id> sinceVersion;

        protected Function<String, JVersion<Id>> versionFunction;

        protected JVersionParent parent;

        protected JVersionProperty property;

        @Nullable
        protected ImmutableMap<PropertyPath, Path<?>> versionTableProperties;

        protected SQLQueryFactory queryFactory;

        @Nullable
        protected CacheBuilder<Object, Object> cacheBuilder;

        public This repositoryId(String repositoryId) {
            this.repositoryId = repositoryId;
            return self();
        }

        public This repository(JRepository jRepository) {
            this.repository = jRepository;
            return self();
        }

        public This sinceVersion(JVersion<Id> sinceVersion) {
            this.sinceVersion = sinceVersion;
            return self();
        }

        public This version(JVersion<Id> version) {
            this.version = version;
            return self();
        }

        public This parent(JVersionParent jParent) {
            this.parent = jParent;
            return self();
        }

        public This property(JVersionProperty jProperty) {
            this.property = jProperty;
            return self();
        }

        public This versionTableProperties(ImmutableMap<PropertyPath, Path<?>> versionTableProperties) {
            this.versionTableProperties = versionTableProperties;
            return self();
        }

        public This queryFactory(SQLQueryFactory queryFactory) {
            this.queryFactory = queryFactory;
            return self();
        }

        public This cacheBuilder(CacheBuilder<Object, Object> cacheBuilder) {
            this.cacheBuilder = cacheBuilder;
            return self();
        }

        public abstract <T extends StoreOptions<Id>> T build();

        public abstract This self();
    }
}
