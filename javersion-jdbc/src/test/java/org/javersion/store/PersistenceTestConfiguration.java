package org.javersion.store;

import static org.javersion.path.PropertyPath.ROOT;
import static org.javersion.store.sql.QDocumentVersion.documentVersion;
import static org.javersion.store.sql.QEntity.entity;
import static org.javersion.store.sql.QRepository.repository;

import java.sql.Types;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.javersion.store.jdbc.*;
import org.javersion.store.jdbc.DocumentStoreOptions.Builder;
import org.javersion.store.sql.QDocumentVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.ImmutableMap;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;

@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement(proxyTargetClass = true)
public class PersistenceTestConfiguration {

    @Inject
    PlatformTransactionManager transactionManager;

    @Value("${querydsl.sqlTemplates}")
    private Class<? extends SQLTemplates> sqlTemplatesClass;

    @Bean
    public SQLTemplates sqlTemplates() {
        try {
            return sqlTemplatesClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public com.querydsl.sql.Configuration configuration(SQLTemplates sqlTemplates) {
        com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(sqlTemplates);
        AbstractVersionStoreJdbc.registerTypes("DOCUMENT_", configuration);
        AbstractVersionStoreJdbc.registerTypes("ENTITY_", configuration);
        return configuration;
    }

    @Bean
    public SQLQueryFactory queryFactory(final DataSource dataSource, com.querydsl.sql.Configuration configuration) {
        return new SQLQueryFactory(configuration, () -> DataSourceUtils.getConnection(dataSource));
    }

    @Bean
    public Transactions transactions() {
        return new SpringTransactions();
    }

    @Bean
    public Executor executor() {
        return Runnable::run;
    }

    @Bean
    public DocumentVersionStoreJdbc<String, String, JDocumentVersion<String>> documentStore(Transactions transactions, Executor executor, SQLQueryFactory
                                                                                            queryFactory) {
        return new DocumentVersionStoreJdbc<>(documentOptionsBuilder(transactions, executor).build(queryFactory));
    }

    @Bean
    public DocumentVersionStoreJdbc<String, String, JDocumentVersion<String>> mappedDocumentStore(Transactions transactions, Executor executor, SQLQueryFactory queryFactory) {
        return new DocumentVersionStoreJdbc<>(
                documentOptionsBuilder(transactions, executor)
                        .versionTableProperties(ImmutableMap.of(
                                ROOT.property("name"), documentVersion.name,
                                ROOT.property("id"), documentVersion.id))
                        .transactions(transactions)
                        .queryFactory(queryFactory)
                        .build());
    }

    @Bean
    public CustomEntityVersionStore entityStore(Transactions transactions, Executor executor, SQLQueryFactory queryFactory) {
        MyQDocumentVersion version = new MyQDocumentVersion("ENTITY_VERSION", "ENTITY_VERSION");
        MyQDocumentVersion since = new MyQDocumentVersion("SINCE", "ENTITY_VERSION");

        return new CustomEntityVersionStore(
                new EntityStoreOptions.Builder<String, String, JEntityVersion<String>>()
                        .defaultsFor("ENTITY")
                        .entityTable(new JEntity<>(entity, entity.id))
                        .versionTable(new JEntityVersion<>(version, version.docId))
                        .versionTableSince(new JEntityVersion<>(since, since.docId))
                        .transactions(transactions)
                        .executor(executor)
                        .queryFactory(queryFactory)
                        .build());
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager);
    }


    private Builder<String, String, JDocumentVersion<String>> documentOptionsBuilder(Transactions transactions, Executor executor) {
        QDocumentVersion sinceVersion = new QDocumentVersion("SINCE");
        return new Builder<String, String, JDocumentVersion<String>>()
                .defaultsFor("DOCUMENT")
                .repositoryTable(new JRepository(repository))
                .versionTable(new JDocumentVersion<>(documentVersion, documentVersion.docId))
                .versionTableSince(new JDocumentVersion<>(sinceVersion, sinceVersion.docId))
                .nextOrdinal(SQLExpressions.nextval("DOCUMENT_VERSION_ORDINAL_SEQ"))
                .transactions(transactions)
                .executor(executor);
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
