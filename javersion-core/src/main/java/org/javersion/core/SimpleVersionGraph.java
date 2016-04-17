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

import static java.util.Arrays.asList;

public final class SimpleVersionGraph extends AbstractVersionGraph<String, String, String, SimpleVersionGraph, SimpleVersionGraph.Builder> {

    public static SimpleVersionGraph init() {
        return new SimpleVersionGraph();
    }

    public static SimpleVersionGraph init(Version<String, String, String>... versions) {
        return init(asList(versions));
    }

    public static SimpleVersionGraph init(Iterable<? extends Version<String, String, String>> versions) {
        Builder builder = new Builder();
        for (Version<String, String, String> version : versions) {
            builder.add(version);
        }
        return builder.build();
    }

    private SimpleVersionGraph() {
        super();
    }

    private SimpleVersionGraph(Builder builder) {
        super(builder);
    }

    @Override
    protected Builder newBuilder() {
        return new Builder(this);
    }

    @Override
    protected Builder newEmptyBuilder() {
        return new Builder();
    }

    static class Builder extends VersionGraphBuilder<String, String, String, SimpleVersionGraph, Builder> {

        protected Builder() {
            super();
        }

        protected Builder(SimpleVersionGraph parentGraph) {
            super(parentGraph);
        }

        @Override
        public SimpleVersionGraph build() {
            return new SimpleVersionGraph(this);
        }
    }

}
