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
package org.javersion.core;

public class SimpleVersion extends Version<String, String, String> {

    public SimpleVersion(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Revision revision) {
        return new Builder(revision);
    }

    public static class Builder extends BuilderBase<String, String, String, Builder> {

        public Builder() {
            super();
        }

        public Builder(Revision revision) {
            super(revision);
        }

        @Override
        public SimpleVersion build() {
            return new SimpleVersion(this);
        }

    }
}
