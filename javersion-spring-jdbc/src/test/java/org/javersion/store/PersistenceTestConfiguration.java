package org.javersion.store;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.javersion.store.jdbc.JVersion;
import org.javersion.store.jdbc.JVersionProperty;
import org.javersion.store.jdbc.ObjectVersionStoreJdbc;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLQueryFactory;
import com.mysema.query.types.path.StringPath;

@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement(proxyTargetClass = true)
public class PersistenceTestConfiguration {

    @Inject
    PlatformTransactionManager transactionManager;

    @Bean
    public SQLQueryFactory queryFactory(final DataSource dataSource) {
        com.mysema.query.sql.Configuration configuration = new com.mysema.query.sql.Configuration(new H2Templates());
        ObjectVersionStoreJdbc.registerTypes("", configuration);
        return new SQLQueryFactory(configuration, () -> DataSourceUtils.getConnection(dataSource));
    }

    @Bean
    public ObjectVersionStoreJdbc<String, Void> versionStore(SQLQueryFactory queryFactory) {
        return new ObjectVersionStoreJdbc<String, Void>("PUBLIC", "", queryFactory) {

            private StringPath versionDocId;

            private StringPath propertyDocId;

            @Override
            protected void initIdColumns(JVersion jVersion, JVersionProperty jProperty) {
                versionDocId = new StringPath(jVersion, "DOC_ID");
                propertyDocId = new StringPath(jProperty, "DOC_ID");
            }

            @Override
            protected StringPath versionDocId() {
                return versionDocId;
            }

            @Override
            protected StringPath propertyDocId() {
                return propertyDocId;
            }
        };
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager);
    }
}
