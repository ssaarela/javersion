package org.javersion.store;

import javax.sql.DataSource;

import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLQueryFactory;

@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement
public class PersistenceTestConfiguration {

    @Bean
    public SQLQueryFactory queryFactory(final DataSource dataSource) {
        com.mysema.query.sql.Configuration configuration = new com.mysema.query.sql.Configuration(new H2Templates());
        ObjectVersionStoreJdbc.registerTypes(configuration);
        return new SQLQueryFactory(configuration, () -> DataSourceUtils.getConnection(dataSource));
    }

    @Bean
    public ObjectVersionStoreJdbc.Initializer storeInitializer(SQLQueryFactory queryFactory) {
        return new ObjectVersionStoreJdbc.Initializer(queryFactory);
    }

    @Bean
    public VersionStore<String,
            PropertyPath, Object, Void,
            ObjectVersionGraph<Void>,
            ObjectVersionGraph.Builder<Void>> versionStore(ObjectVersionStoreJdbc.Initializer initializer) {
        return new ObjectVersionStoreJdbc<>(initializer);
    }

}
