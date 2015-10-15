package org.javersion.store;

import static org.javersion.path.PropertyPath.ROOT;
import static org.javersion.store.sql.QRepository.repository;
import static org.javersion.store.sql.QDocumentVersion.documentVersion;
import static org.javersion.store.sql.QDocumentVersionParent.documentVersionParent;
import static org.javersion.store.sql.QDocumentVersionProperty.documentVersionProperty;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.javersion.store.jdbc.*;
import org.javersion.store.jdbc.DocumentStoreOptions.Builder;
import org.javersion.store.sql.QDocumentVersion;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.ImmutableMap;
import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLExpressions;
import com.mysema.query.sql.SQLQueryFactory;

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
        return configuration;
    }

    @Bean
    public SQLQueryFactory queryFactory(final DataSource dataSource, com.mysema.query.sql.Configuration configuration) {
        return new SQLQueryFactory(configuration, () -> DataSourceUtils.getConnection(dataSource));
    }

    @Bean
    public DocumentVersionStoreJdbc<String, Void> versionStore(SQLQueryFactory queryFactory) {

        return new DocumentVersionStoreJdbc<String, Void>(documentOptionsBuilder(queryFactory).build());
    }

    @Bean
    public DocumentVersionStoreJdbc<String, Void> mappedVersionStore(SQLQueryFactory queryFactory) {
        return new DocumentVersionStoreJdbc<String, Void>(
                documentOptionsBuilder(queryFactory)
                        .versionTableProperties(ImmutableMap.of(
                                ROOT.property("name"), documentVersion.name,
                                ROOT.property("id"), documentVersion.id))
                        .build());
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager);
    }


    private Builder<String> documentOptionsBuilder(SQLQueryFactory queryFactory) {
        QDocumentVersion sinceVersion = new QDocumentVersion("SINCE");
        return new Builder<String>()
                .repository(new JRepository(repository))
                .repositoryId("DOCUMENT_VERSION")
                .version(new JDocumentVersion<>(documentVersion, documentVersion.docId))
                .sinceVersion(new JDocumentVersion<>(sinceVersion, sinceVersion.docId))
                .nextOrdinal(SQLExpressions.nextval("DOCUMENT_VERSION_ORDINAL_SEQ"))
                .parent(new JVersionParent(documentVersionParent))
                .property(new JVersionProperty(documentVersionProperty))
                .queryFactory(queryFactory);
    }
}
