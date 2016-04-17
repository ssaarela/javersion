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

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.util.function.Supplier;

import org.springframework.transaction.annotation.Transactional;

public class SpringTransactions implements Transactions {

    @Override
    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public <T> T readOnly(Supplier<T> callback) {
        return callback.get();
    }

    @Override
    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public <T> T writeRequired(Supplier<T> callback) {
        return callback.get();
    }

    @Override
    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
    public <T> T writeNewRequired(Supplier<T> callback) {
        return callback.get();
    }

    @Override
    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = MANDATORY)
    public <T> T writeMandatory(Supplier<T> callback) {
        return callback.get();
    }

}
