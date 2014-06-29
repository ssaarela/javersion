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

import org.javersion.core.VersionGraph;
import org.javersion.core.VersionGraphBuilder;
import org.javersion.core.simple.SimpleVersionGraph.Builder;

public final class SimpleVersionGraph extends VersionGraph<String, String, SimpleVersion, SimpleVersionGraph, Builder> {
    
    public static SimpleVersionGraph init() {
        return build(new Builder());
    }
    
    public static SimpleVersionGraph init(SimpleVersion version) {
        return build(new Builder(), version);
    }
    
    public static SimpleVersionGraph init(Iterable<SimpleVersion> versions) {
        return build(new Builder(), versions);
    }
    

    @Override
    public SimpleVersionGraph commit(SimpleVersion version) {
        return build(new Builder(this), version);
    }

    @Override
    public SimpleVersionGraph commit(Iterable<SimpleVersion> versions) {
        return build(new Builder(this), versions);
    }


    SimpleVersionGraph(Builder builder) {
        super(builder);
    }
    
    
    static class Builder extends VersionGraphBuilder<String, String, SimpleVersion, SimpleVersionGraph, Builder> {

        protected Builder() {
            super();
        }
        protected Builder(SimpleVersionGraph parentGraph) {
            super(parentGraph);
        }

        @Override
        protected SimpleVersionGraph build() {
            return new SimpleVersionGraph(this);
        }
        
        @Override
        protected Builder newBuilder() {
            return new Builder();
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
