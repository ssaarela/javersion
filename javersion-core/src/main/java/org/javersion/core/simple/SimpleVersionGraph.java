package org.javersion.core.simple;

import org.javersion.core.VersionGraph;

public final class SimpleVersionGraph extends VersionGraph<String, String, String, SimpleVersion, SimpleVersionGraph> {
    
    public static SimpleVersionGraph init(SimpleVersion version) {
        return build(new Builder(), version);
    }
    
    public static SimpleVersionGraph init(Iterable<SimpleVersion> versions) {
        return build(new Builder(), versions);
    }

    SimpleVersionGraph(Builder builder) {
        super(builder);
    }

    @Override
    public SimpleVersionGraph commit(SimpleVersion version) {
        return build(new Builder(this), version);
    }

    @Override
    public SimpleVersionGraph commit(Iterable<SimpleVersion> versions) {
        return build(new Builder(this), versions);
    }

    
    
    public static class Builder extends VersionGraph.Builder<String, String, String, SimpleVersion, SimpleVersionGraph> {

        protected Builder() {
            super(null);
        }
        protected Builder(SimpleVersionGraph parentGraph) {
            super(parentGraph);
        }

        @Override
        protected SimpleVersionGraph build() {
            return new SimpleVersionGraph(this);
        }
    }
}
