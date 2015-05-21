package org.javersion.store;

import javax.sql.DataSource;

import org.javersion.store.jdbc.ObjectVersionStoreJdbc;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLQueryFactory;

@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement(proxyTargetClass = true)
public class PersistenceTestConfiguration {

    @Bean
    public SQLQueryFactory queryFactory(final DataSource dataSource) {
        com.mysema.query.sql.Configuration configuration = new com.mysema.query.sql.Configuration(new H2Templates());
        ObjectVersionStoreJdbc.registerTypes("", configuration);
        return new SQLQueryFactory(configuration, () -> DataSourceUtils.getConnection(dataSource));
    }

    @Bean
    public ObjectVersionStoreJdbc<Void> versionStore(SQLQueryFactory queryFactory) {
        return new ObjectVersionStoreJdbc<>("PUBLIC", "", queryFactory);
    }

}
