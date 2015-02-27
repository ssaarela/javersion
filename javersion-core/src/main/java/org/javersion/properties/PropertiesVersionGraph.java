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
import org.javersion.core.VersionGraph;
import org.javersion.core.VersionGraphBuilder;
import org.javersion.core.VersionNode;
import org.javersion.properties.PropertiesVersionGraph.Builder;
import org.javersion.util.PersistentSortedMap;

public final class PropertiesVersionGraph extends VersionGraph<String, String, Void, PropertiesVersionGraph, Builder> {

    public static PropertiesVersionGraph init() {
        return new PropertiesVersionGraph();
    }

    public static PropertiesVersionGraph init(PropertiesVersion version) {
        Builder builder = new Builder();
        builder.add(version);
        return builder.build();
    }

    public static PropertiesVersionGraph init(Iterable<PropertiesVersion> versions) {
        Builder builder = new Builder();
        for (PropertiesVersion version : versions) {
            builder.add(version);
        }
        return builder.build();
    }

    private PropertiesVersionGraph() {
        super();
    }

    private PropertiesVersionGraph(Builder builder) {
        super(builder);
    }

    private PropertiesVersionGraph(PersistentSortedMap<Revision, VersionNode<String, String, Void>> versionNodes, VersionNode<String, String, Void> at) {
        super(versionNodes, at);
    }

    @Override
    protected Builder newBuilder() {
        return new Builder(this);
    }

    @Override
    protected PropertiesVersionGraph at(PersistentSortedMap<Revision, VersionNode<String, String, Void>> versionNodes, VersionNode<String, String, Void> at) {
        return new PropertiesVersionGraph(versionNodes, at);
    }

    static class Builder extends VersionGraphBuilder<String, String, Void, PropertiesVersionGraph, Builder> {

        protected Builder() {
            super();
        }

        protected Builder(PropertiesVersionGraph parentGraph) {
            super(parentGraph);
        }

        @Override
        public PropertiesVersionGraph build() {
            return new PropertiesVersionGraph(this);
        }
    }

}
