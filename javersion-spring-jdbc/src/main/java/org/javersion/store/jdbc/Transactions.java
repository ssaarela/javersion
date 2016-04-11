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

import java.util.function.Supplier;

public interface Transactions {

    /**
     * Execute callback in a read-committed, readOnly transaction supporting current transaction or creating a new one if none exists.
     */
    <T> T readOnly(Supplier<T> callback);

    /**
     * Execute callback in a read-committed, read/write transaction supporting current transaction or creating a new one if none exists.
     */
    <T> T writeRequired(Supplier<T> callback);

    /**
     * Execute callback in a read-committed, read/write transaction supporting current transaction and throwing an exception if none exists.
     */
    <T> T writeMandatory(Supplier<T> callback);

}
