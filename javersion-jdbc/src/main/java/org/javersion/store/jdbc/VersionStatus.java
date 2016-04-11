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

public enum VersionStatus {
    /**
     * Version, property or parent is squashed. It should only be loaded
     * when older version or whole history is required or when fetching
     * updates. Otherwise, squashed rows should be skipped on first load.
     */
    SQUASHED,
    /**
     * Normal version, property or parent. Active rows should be loaded
     * always.
     */
    ACTIVE,
    /**
     * A redundant property or parent copied from an ancestor for
     * current versions effective state. Redundant relations
     * should only be fetched on first load, and skipped when
     * loading whole history or updates.
     *
     * This state does not apply for versions.
     */
    REDUNDANT
}
