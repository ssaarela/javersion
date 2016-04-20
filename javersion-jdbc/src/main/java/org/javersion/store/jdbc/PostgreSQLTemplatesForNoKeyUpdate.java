/*
 * Copyright 2016 Samppa Saarela
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
package org.javersion.store.jdbc;

import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLOps;

/**
 * Use "for no key update" instead of "for update" locking.
 * This allows non-blocking insertion of new versions (parents) while
 * running maintenance updates (publish, optimize).
 */
public class PostgreSQLTemplatesForNoKeyUpdate extends PostgreSQLTemplates {

    public PostgreSQLTemplatesForNoKeyUpdate() {
        super();
        this.add(SQLOps.FOR_UPDATE, "\nfor no key update");
    }
}
