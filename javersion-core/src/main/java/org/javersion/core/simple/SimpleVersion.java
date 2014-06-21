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
package org.javersion.core.simple;

import java.util.Map;
import java.util.Set;

import org.javersion.core.Version;
import org.javersion.core.VersionType;

public class SimpleVersion extends Version<String, String> {

    
    public SimpleVersion(Builder builder) {
        super(builder);
    }

    public static class Builder extends Version.Builder<String, String> {

        public Builder(long revision) {
            super(revision);
        }

        @Override
        public Builder withBranch(String branch) {
            super.withBranch(branch);
            return this;
        }

        @Override
        public Builder withParents(Set<Long> parentRevisions) {
            super.withParents(parentRevisions);
            return this;
        }

        @Override
        public Builder withProperties(Map<String, String> properties) {
            super.withProperties(properties);
            return this;
        }

        @Override
        public SimpleVersion build() {
            return new SimpleVersion(this);
        }

        @Override
        public Builder withType(VersionType versionType) {
            super.withType(versionType);
            return this;
        }

    }
}
