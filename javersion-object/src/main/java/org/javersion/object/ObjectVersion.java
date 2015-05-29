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
package org.javersion.object;

import org.javersion.core.Revision;
import org.javersion.core.Version;
import org.javersion.path.PropertyPath;

public class ObjectVersion<M> extends Version<PropertyPath, Object, M> {

    public ObjectVersion(Version.BuilderBase<PropertyPath, Object, M, ?> builder) {
        super(builder);
    }

    public static <M> Builder<M> builder() {
        return new Builder<>();
    }

    public static <M> Builder<M> builder(Revision revision) {
        return new Builder<>(revision);
    }

    public static class Builder<M> extends Version.BuilderBase<PropertyPath, Object, M, ObjectVersion.Builder<M>> {

        public Builder() {}

        public Builder(Revision revision) {
            super(revision);
        }

        @Override
        public ObjectVersion<M> build() {
            return new ObjectVersion<>(this);
        }

    }

}
