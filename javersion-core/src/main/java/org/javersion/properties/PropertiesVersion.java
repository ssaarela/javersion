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
package org.javersion.properties;

import org.javersion.core.Revision;
import org.javersion.core.Version;

public class PropertiesVersion extends Version<String, String> {

    public PropertiesVersion(Builder builder) {
        super(builder);
    }

    public static class Builder extends Version.Builder<String, String, Builder> {

        public Builder() {
            super();
        }

        public Builder(Revision revision) {
            super(revision);
        }

        @Override
        public PropertiesVersion build() {
            return new PropertiesVersion(this);
        }

    }
}
