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


public class ContextReference<T> {
    private UpdateContext<T> context;
    ContextReference(UpdateContext<T> context) {
        this.context = context;
    }
    void commit() {
        this.context = null;
    }
    UpdateContext<T> get() {
        return context;
    }
    boolean isCommitted() {
        return context == null;
    }
    boolean isSameAs(ContextReference<T> other) {
        return this.context != null && this.context == other.context;
    }
    void validate() {
        if (isCommitted()) {
            throw new IllegalStateException("This update is already committed");
        }
    }
}