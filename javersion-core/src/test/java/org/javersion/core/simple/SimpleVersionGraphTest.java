package org.javersion.core.simple;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javersion.core.Merge;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class SimpleVersionGraphTest {
    
    
    public static List<VersionExpectation> EXPECTATIONS = Arrays.asList(
            when(version(1l)
                .properties(mapOf(
                            "firstName", "John",
                            "lastName", "Doe")))
                .expectProperties(mapOf(
                            "firstName", "John",
                            "lastName", "Doe")),


            when(version(2l)
                    .parents(setOf(1l))
                    .properties(mapOf(
                                "status", "Single")))
                    .expectProperties(mapOf(
                                "firstName", "John",
                                "lastName", "Doe",
                                "status", "Single")),


            when(version(3l)
                    .parents(setOf(1l))
                    .properties(mapOf(
                                "mood", "Lonely")))
                    .mergeRevisions(setOf(3l, 2l))
                    .expectProperties(mapOf(
                                "firstName", "John",
                                "lastName", "Doe",
                                "status", "Single",
                                "mood", "Lonely"))

    );
    
    @Test
    public void Sequential_Updates() {
        SimpleVersionGraph versionGraph = null;
        for (VersionExpectation expectation : EXPECTATIONS) {
            if (expectation.version != null) {
                if (versionGraph == null) {
                    versionGraph = SimpleVersionGraph.init(expectation.version);
                } else {
                    versionGraph = versionGraph.commit(expectation.version);
                }
            }
            assertExpectations(versionGraph, expectation);
        }
    }

    @Test
    public void Bulk_Load() {
        SimpleVersionGraph versionGraph = 
                SimpleVersionGraph.init(filter(transform(EXPECTATIONS, getVersion), notNull()));
        
        for (VersionExpectation expectation : EXPECTATIONS) {
            assertExpectations(versionGraph, expectation);
        }
    }
    
    private void assertExpectations(SimpleVersionGraph versionGraph, VersionExpectation expectation) {
        Merge<String, String> merge = versionGraph.merge(expectation.mergeRevisions);
        assertThat(title("revisions", expectation), merge.revisions, equalTo(expectation.expectedRevisions));
        assertThat(title("properties", expectation), merge.getProperties(), equalTo(expectation.expectedProperties));
    }
    
    private static String title(String assertLabel, VersionExpectation expectation) {
        return assertLabel + " of #" + expectation.version.revision;
    }

    public static Function<VersionExpectation, SimpleVersion> getVersion = new Function<VersionExpectation, SimpleVersion>() {
        @Override
        public SimpleVersion apply(VersionExpectation input) {
            return input.version;
        }
    };
    
    public static class VersionExpectation {
        public final SimpleVersion version;
        public Set<Long> mergeRevisions;
        public Map<String, String> expectedProperties;
        public Set<Long> expectedRevisions;
        public VersionExpectation(SimpleVersion version) {
            this.version = version;
            if (version != null) {
                this.mergeRevisions = ImmutableSet.of(version.revision);
            } else {
                this.mergeRevisions = ImmutableSet.of();
            }
            this.expectedRevisions = mergeRevisions;
        }
        public VersionExpectation mergeRevisions(Set<Long> mergeRevisions) {
            this.mergeRevisions = mergeRevisions;
            this.expectedRevisions = mergeRevisions;
            return this;
        }
        public VersionExpectation expectProperties(Map<String, String> expectedProperties) {
            this.expectedProperties = expectedProperties;
            return this;
        }
        public VersionExpectation expectRevisions(Set<Long> expectedRevisions) {
            this.expectedRevisions = expectedRevisions;
            return this;
        }
    }
    
    public static SimpleVersion.Builder version(long rev) {
        return new SimpleVersion.Builder(rev);
    }
    
    public static VersionExpectation when(SimpleVersion.Builder builder) {
        return new VersionExpectation(builder.build());
    }
    public static VersionExpectation then() {
        return new VersionExpectation(null);
    }
    @SafeVarargs
    public static <T> Set<T> setOf(T... revs) {
        return ImmutableSet.copyOf(revs);
    }
    
    public static Map<String, String> mapOf(String... entries) {
        ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
        for (int i=0; i+1 < entries.length; i+=2) {
            map.put(entries[i], entries[i+1]);
        }
        return map.build();
    }
    
}
