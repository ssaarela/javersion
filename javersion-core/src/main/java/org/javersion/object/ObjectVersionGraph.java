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

import org.javersion.core.AbstractVersionGraph;
import org.javersion.core.AbstractVersionGraphBuilder;
import org.javersion.path.PropertyPath;
import org.javersion.object.ObjectVersionGraph.Builder;

public final class ObjectVersionGraph<M> extends AbstractVersionGraph<PropertyPath, Object, ObjectVersion<M>, ObjectVersionGraph<M>, Builder<M>> {
    
    public static <M> ObjectVersionGraph<M> init() {
        return new ObjectVersionGraph<M>();
    }
    
    public static <M> ObjectVersionGraph<M> init(ObjectVersion<M> version) {
        Builder<M> builder = new Builder<M>();
        builder.add(version);
        return builder.build();
    }
    
    public static <M> ObjectVersionGraph<M> init(Iterable<ObjectVersion<M>> versions) {
        Builder<M> builder = new Builder<M>();
        for (ObjectVersion<M> version : versions) {
            builder.add(version);
        }
        return builder.build();
    }

    private ObjectVersionGraph() {
        super();
    }

    private ObjectVersionGraph(Builder<M> builder) {
        super(builder);
    }

    @Override
    protected Builder<M> newBuilder() {
        return new Builder<M>(this);
    }
    
    static class Builder<M> extends AbstractVersionGraphBuilder<PropertyPath, Object, ObjectVersion<M>, ObjectVersionGraph<M>, Builder<M>> {

        protected Builder() {
            super();
        }
        
        protected Builder(ObjectVersionGraph<M> parentGraph) {
            super(parentGraph);
        }

        @Override
        protected ObjectVersionGraph<M> build() {
            return new ObjectVersionGraph<M>(this);
        }
    }

}
