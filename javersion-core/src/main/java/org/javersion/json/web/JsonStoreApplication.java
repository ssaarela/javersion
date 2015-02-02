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

import com.mysema.query.sql.H2Templates;

@SpringBootApplication
public class JsonStoreApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JsonStoreApplication.class);
        app.run(args);
    }

    @Inject
    DataSource dataSource;

    @Bean
    public ObjectVersionStoreJdbc<Void> versionStore() {
        return new ObjectVersionStoreJdbc<>(dataSource, new H2Templates());
    }

}
