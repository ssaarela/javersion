package org.javersion.store;

import static org.javersion.path.PropertyPath.ROOT;
import static org.javersion.store.sql.QTestRepository.testRepository;
import static org.javersion.store.sql.QTestVersion.testVersion;
import static org.javersion.store.sql.QTestVersionParent.testVersionParent;
import static org.javersion.store.sql.QTestVersionProperty.testVersionProperty;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.javersion.store.jdbc.JRepository;
import org.javersion.store.jdbc.JVersion;
import org.javersion.store.jdbc.JVersionParent;
import org.javersion.store.jdbc.JVersionProperty;
import org.javersion.store.jdbc.ObjectVersionStoreJdbc;
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
        ObjectVersionStoreJdbc.registerTypes("TEST_", configuration);
        return configuration;
    }

    @Bean
    public SQLQueryFactory queryFactory(final DataSource dataSource, com.mysema.query.sql.Configuration configuration) {
        return new SQLQueryFactory(configuration, () -> DataSourceUtils.getConnection(dataSource));
    }

    @Bean
    public ObjectVersionStoreJdbc<String, Void> versionStore(SQLQueryFactory queryFactory) {
        return new ObjectVersionStoreJdbc<String, Void>(
                new JRepository(testRepository),
                SQLExpressions.nextval("TEST_VERSION_ORDINAL_SEQ"),
                new JVersion<>(testVersion, testVersion.docId),
                new JVersionParent(testVersionParent),
                new JVersionProperty(testVersionProperty),
                queryFactory);
    }

    @Bean
    public ObjectVersionStoreJdbc<String, Void> mappedVersionStore(SQLQueryFactory queryFactory) {
        return new ObjectVersionStoreJdbc<String, Void>(
                new JRepository(testRepository),
                SQLExpressions.nextval("TEST_VERSION_ORDINAL_SEQ"),
                new JVersion<>(testVersion, testVersion.docId),
                new JVersionParent(testVersionParent),
                new JVersionProperty(testVersionProperty),
                queryFactory,
                ImmutableMap.of(
                        ROOT.property("name"), testVersion.name,
                        ROOT.property("id"), testVersion.id));
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager);
    }
}
