package org.javersion.store;

import static org.javersion.path.PropertyPath.ROOT;
import static org.javersion.store.sql.QDocumentVersion.documentVersion;
import static org.javersion.store.sql.QEntity.entity;
import static org.javersion.store.sql.QRepository.repository;

import java.sql.Types;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.javersion.store.jdbc.*;
import org.javersion.store.jdbc.DocumentStoreOptions.Builder;
import org.javersion.store.sql.QDocumentVersion;
import org.javersion.store.sql.QRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.ImmutableMap;
import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLExpressions;
import com.mysema.query.sql.SQLQueryFactory;
import com.mysema.query.types.path.StringPath;

@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement(proxyTargetClass = true)
public class PersistenceTestConfiguration {

    @Inject
    PlatformTransactionManager transactionManager;

    @Bean
    public com.mysema.query.sql.Configuration configuration() {
        com.mysema.query.sql.Configuration configuration = new com.mysema.query.sql.Configuration(new H2Templates());
        AbstractVersionStoreJdbc.registerTypes("DOCUMENT_", configuration);
        AbstractVersionStoreJdbc.registerTypes("ENTITY_", configuration);
        return configuration;
    }

    @Bean
    public SQLQueryFactory queryFactory(final DataSource dataSource, com.mysema.query.sql.Configuration configuration) {
        return new SQLQueryFactory(configuration, () -> DataSourceUtils.getConnection(dataSource));
    }

    @Bean
    public DocumentVersionStoreJdbc<String, Void, JDocumentVersion<String>> documentStore(SQLQueryFactory queryFactory) {

        return new DocumentVersionStoreJdbc<>(documentOptionsBuilder().build(queryFactory));
    }

    @Bean
    public DocumentVersionStoreJdbc<String, Void, JDocumentVersion<String>> mappedDocumentStore(SQLQueryFactory queryFactory) {
        return new DocumentVersionStoreJdbc<>(
                documentOptionsBuilder()
                        .versionTableProperties(ImmutableMap.of(
                                ROOT.property("name"), documentVersion.name,
                                ROOT.property("id"), documentVersion.id))
                        .queryFactory(queryFactory)
                        .build());
    }

    @Bean
    public CustomEntityVersionStore entityVersionStore(SQLQueryFactory queryFactory) {
        MyQDocumentVersion version = new MyQDocumentVersion("ENTITY_VERSION", "ENTITY_VERSION");
        MyQDocumentVersion since = new MyQDocumentVersion("SINCE", "ENTITY_VERSION");

        return new CustomEntityVersionStore(
                new EntityStoreOptions.Builder<String, JEntityVersion<String>>()
                        .defaultsFor("ENTITY")
                        .entityTable(new JEntity<>(entity, entity.id))
                        .versionTable(new JEntityVersion<>(version, version.docId))
                        .versionTableSince(new JEntityVersion<>(since, since.docId))
                        .queryFactory(queryFactory)
                        .build());
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager);
    }


    private Builder<String, JDocumentVersion<String>> documentOptionsBuilder() {
        QDocumentVersion sinceVersion = new QDocumentVersion("SINCE");
        return new Builder<String, JDocumentVersion<String>>()
                .defaultsFor("DOCUMENT")
                .repositoryTable(new JRepository(repository))
                .versionTable(new JDocumentVersion<>(documentVersion, documentVersion.docId))
                .versionTableSince(new JDocumentVersion<>(sinceVersion, sinceVersion.docId))
                .nextOrdinal(SQLExpressions.nextval("DOCUMENT_VERSION_ORDINAL_SEQ"));
    }

    private class MyQDocumentVersion extends QEntityVersionBase<MyQDocumentVersion> {

        public final StringPath docId = createString("docId");

        public final StringPath comment = createString("comment");

        public MyQDocumentVersion(String variable, String table) {
            super(MyQDocumentVersion.class, variable, table, table);
            addMetadata(docId, ColumnMetadata.named("DOC_ID").ofType(Types.VARCHAR).withSize(255).notNull());
            addMetadata(comment, ColumnMetadata.named("COMMENT").ofType(Types.VARCHAR).withSize(255));
        }
    }
}
