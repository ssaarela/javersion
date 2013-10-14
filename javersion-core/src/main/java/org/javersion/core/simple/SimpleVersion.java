package org.javersion.core.simple;

import java.util.Map;
import java.util.Set;

import org.javersion.core.Version;

public class SimpleVersion extends Version<String, String> {

    
    public SimpleVersion(Builder builder) {
        super(builder);
    }

    public static class Builder extends Version.Builder<String, String> {

        public Builder(long revision) {
            super(revision);
        }

        @Override
        public Builder branch(String branch) {
            super.branch(branch);
            return this;
        }

        @Override
        public Builder parents(Set<Long> parentRevisions) {
            super.parents(parentRevisions);
            return this;
        }

        @Override
        public Builder properties(Map<String, String> properties) {
            super.properties(properties);
            return this;
        }

        @Override
        public SimpleVersion build() {
            return new SimpleVersion(this);
        }

    }
}
