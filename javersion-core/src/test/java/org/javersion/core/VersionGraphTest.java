package org.javersion.core;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class VersionGraphTest {
    
    
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
        VersionGraph<String, String, String> versionGraph = null;
        for (VersionExpectation expectation : EXPECTATIONS) {
            if (versionGraph == null) {
                versionGraph = VersionGraph.init(expectation.version);
            } else {
                versionGraph = versionGraph.commit(expectation.version);
            }
            
            assertExpectations(versionGraph, expectation);
        }
    }

    @Test
    public void Bulk_Load() {
        VersionGraph<String, String, String> versionGraph = 
                VersionGraph.init(Iterables.transform(EXPECTATIONS, getVersion));
        
        for (VersionExpectation expectation : EXPECTATIONS) {
            assertExpectations(versionGraph, expectation);
        }
    }
    
    private void assertExpectations(VersionGraph<String, String, String> versionGraph, VersionExpectation expectation) {
        Merge<String, String> merge = versionGraph.merge(expectation.mergeRevisions);
        assertThat(title("revisions", expectation), merge.revisions, equalTo(expectation.expectedRevisions));
        assertThat(title("properties", expectation), merge.getProperties(), equalTo(expectation.expectedProperties));
    }
    
    private static String title(String assertLabel, VersionExpectation expectation) {
        return assertLabel + " of #" + expectation.version.revision;
    }

    public static Function<VersionExpectation, Version<String, String, String>> getVersion = new Function<VersionGraphTest.VersionExpectation, Version<String,String,String>>() {
        @Override
        public Version<String, String, String> apply(VersionExpectation input) {
            return input.version;
        }
    };
    
    public static class VersionExpectation {
        public final Version<String, String, String> version;
        public Set<Long> mergeRevisions;
        public Map<String, String> expectedProperties;
        public Set<Long> expectedRevisions;
        public VersionExpectation(Version<String, String, String> version) {
            this.version = version;
            this.mergeRevisions = ImmutableSet.of(version.revision);
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
    
    public static Version.Builder<String, String, String> version(long rev) {
        return new Version.Builder<String, String, String>(rev);
    }
    
    public static VersionExpectation when(Version.Builder<String, String, String> builder) {
        return new VersionExpectation(builder.build());
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
