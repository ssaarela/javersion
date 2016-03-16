/*
 * Copyright 2013 Samppa Saarela
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
package org.javersion.util;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public interface Merger<T> {

    default boolean insert(T newEntry) {
        return true;
    }

    /**
     * Return true if newEntry should replace oldEntry, otherwise false.
     *
     * @param oldEntry
     * @param newEntry
     * @return true if newEntry replaces oldEntry.
     */
    default boolean merge(T oldEntry, T newEntry) {
        return true;
    }

    default boolean delete(T oldEntry) {
        return true;
    }

}
