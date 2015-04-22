/*
 * Copyright 2014 Samppa Saarela
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
package org.javersion.json.web;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.javersion.store.ObjectVersionStoreJdbc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLQueryFactory;

@SpringBootApplication
public class JsonStoreApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JsonStoreApplication.class);
        app.run(args);
    }

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
    public ObjectVersionStoreJdbc<Void> versionStore(ObjectVersionStoreJdbc.Initializer storeInitializer) {
        return new ObjectVersionStoreJdbc<>(storeInitializer);
    }

}
