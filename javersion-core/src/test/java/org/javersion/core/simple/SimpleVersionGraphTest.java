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

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static java.util.Collections.unmodifiableMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javersion.core.Merge;
import org.javersion.core.VersionType;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.*;

public class SimpleVersionGraphTest {
    
    private static Set<Long> EMPTY_REVISIONS = setOf();
    
    private static Map<String, String> EMPTY_PROPERTIES = mapOf();
    
    /**
     * <pre>
     * 1    firstName: "John", lastName: "Doe"
     * | \   
     * 2  |  status: "Single"
     * |  |
     * |  3  mood: "Lonely"
     * |  |
     * |  4  lastName: "Foe", status: "Just married", mood: "Ecstatic", married: "2013-10-12" 
     * |  |
     * 5 /   status: "Just married"
     * |/
     * 6    status: "Married", mood: null, married: null
     * :
     * 7    type: ROOT, status: "New beginning"
     * </pre>
     */
    public static List<VersionExpectation> EXPECTATIONS = Arrays.asList(
            when(version(1l)
                .properties(mapOf(
                            "firstName", "John",
                            "lastName", "Doe")))
                .expectProperties(mapOf(
                            "firstName", "John",
                            "lastName", "Doe")),

            then("Empty merge")
                .mergeRevisions(EMPTY_REVISIONS)
                .expectProperties(EMPTY_PROPERTIES),
                

            when(version(2l)
                    .parents(setOf(1l))
                    .properties(mapOf(
                                "status", "Single")))
                    .expectProperties(mapOf(
                                "firstName", "John", // 1
                                "lastName", "Doe", // 1
                                "status", "Single")), // 2


            when(version(3l)
                    .parents(setOf(1l))
                    .properties(mapOf(
                                "mood", "Lonely")))
                    .mergeRevisions(setOf(3l, 2l))
                    .expectProperties(mapOf(
                                "firstName", "John", // 1
                                "lastName", "Doe", // 1
                                "status", "Single", // 2
                                "mood", "Lonely")), // 3


            when(version(4l)
                    .parents(setOf(3l))
                    .properties(mapOf(
                                "lastName", "Foe",
                                "status", "Just married",
                                "mood", "Ecstatic",
                                "married", "2013-10-12")))
                    .mergeRevisions(setOf(4l))
                    .expectProperties(mapOf(
                                "firstName", "John", // 1
                                "lastName", "Foe", // 4
                                "status", "Just married", // 4
                                "mood", "Ecstatic", // 4
                                "married", "2013-10-12")), // 4

            then("Merge with ancestor")
                    .mergeRevisions(setOf(3l, 4l))
                    .expectRevisions(setOf(4l))
                    .expectProperties(mapOf(
                            "firstName", "John", // 1
                            "lastName", "Doe", // 1
                            "status", "Just married", // 4 - "Single" from version 2 is not merged here!
                            "mood", "Lonely", // 3
                            "married", "2013-10-12")) // 4
                    .expectConflicts(multimapOf(
                            "lastName", "Foe", // 4
                            "mood", "Ecstatic" // 4
                            )),


            when(version(5l)
                    .parents(setOf(2l))
                    .properties(mapOf(
                                "status", "Just married")))
                    .mergeRevisions(setOf(4l, 5l))
                    .expectProperties(mapOf(
                                "firstName", "John",
                                "lastName", "Foe",
                                "status", "Just married", // 4 and 5 - not conflicting!
                                "mood", "Ecstatic", // 4
                                "married", "2013-10-12")), // 4

            then("Conflicting merge")
                    .mergeRevisions(setOf(5l, 4l))
                    .expectProperties(mapOf(
                            "firstName", "John", // 1
                            "lastName", "Doe", // 1
                            "status", "Just married", // 4 and 5 - not conflicting!
                            "mood", "Ecstatic", // 4
                            "married", "2013-10-12")) // 4.
                    .expectConflicts(multimapOf(
                            "lastName", "Foe" // 4
                            )),


            when(version(6l)
                    .parents(setOf(5l, 4l))
                    .properties(mapOf(
                                "status", "Married",
                                "mood", null,
                                "married", null)))
                    .expectProperties(mapOf(
                                "firstName", "John",
                                "lastName", "Foe",
                                "status", "Married")), // 4 and 5 - not conflicting!

            when(version(7l)
                    .properties(mapOf(
                                "status", "New beginning"))
                    .type(VersionType.ROOT))
                    .expectProperties(mapOf(
                                "status", "New beginning")) // 4 and 5 - not conflicting!
            );
    
    
    @Test
    public void Sequential_Updates() {
        SimpleVersionGraph versionGraph = SimpleVersionGraph.init();
        long revision = -1;
        for (VersionExpectation expectation : EXPECTATIONS) {
            if (expectation.version != null) {
                revision = expectation.version.revision;
                versionGraph = versionGraph.commit(expectation.version);
            }
            assertExpectations(versionGraph, revision, expectation);
        }
    }
    
    static List<List<VersionExpectation>> getBulkExpectations() {
        List<List<VersionExpectation>> bulks = Lists.newArrayList();
        List<VersionExpectation> bulk = Lists.newArrayList();
        for (VersionExpectation expectation : EXPECTATIONS) {
            if (expectation.version != null && expectation.version.type == VersionType.ROOT) {
                bulks.add(bulk);
                bulk = Lists.newArrayList();
            }
            bulk.add(expectation);
        }
        bulks.add(bulk);
        return bulks;
    }

    @Test
    public void Bulk_Load() {
        long revision = -1;
        for (List<VersionExpectation> expectations : getBulkExpectations()) {
            SimpleVersionGraph versionGraph = 
                    SimpleVersionGraph.init(filter(transform(expectations, getVersion), notNull()));
            for (VersionExpectation expectation : expectations) {
                if (expectation.version != null) {
                    revision = expectation.version.revision;
                }
                assertExpectations(versionGraph, revision, expectation);
            }
        }
    }
    
    private void assertExpectations(SimpleVersionGraph versionGraph, long revision, VersionExpectation expectation) {
        try {
            Merge<String, String> merge = versionGraph.merge(expectation.mergeRevisions);
            assertThat(title("revisions", revision, expectation), 
                    merge.revisions, 
                    equalTo(expectation.expectedRevisions));
            
            assertThat(title("properties", revision, expectation), 
                    merge.getProperties(), 
                    equalTo(expectation.expectedProperties));
            
            assertThat(title("conflicts", revision, expectation), 
                    Multimaps.transformValues(merge.conflicts, merge.getVersionPropertyValue), 
                    equalTo(expectation.expectedConflicts));
        } catch (RuntimeException e) {
            throw new AssertionError(title("merge", revision, expectation), e);
        }
    }
    
    private static String title(String assertLabel, long revision, VersionExpectation expectation) {
        return assertLabel + " of #" + revision + (expectation.title != null ? ": " + expectation.title : "");
    }

    public static Function<VersionExpectation, SimpleVersion> getVersion = new Function<VersionExpectation, SimpleVersion>() {
        @Override
        public SimpleVersion apply(VersionExpectation input) {
            return input.version;
        }
    };
    
    public static class VersionExpectation {
        public final String title;
        public final SimpleVersion version;
        public Set<Long> mergeRevisions = ImmutableSet.of();
        public Map<String, String> expectedProperties;
        public Multimap<String, String> expectedConflicts = ImmutableMultimap.of();
        public Set<Long> expectedRevisions;
        public VersionExpectation(String title) {
            this(null, title);
        }
        public VersionExpectation(SimpleVersion version) {
            this(version, null);
        }
        public VersionExpectation(SimpleVersion version, String title) {
            this.version = version;
            this.title = title;
            if (version != null) {
                this.mergeRevisions = ImmutableSet.of(version.revision);
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
        public VersionExpectation expectConflicts(Multimap<String, String> expectedConflicts) {
            this.expectedConflicts = expectedConflicts;
            return this;
        }
    }
    
    public static SimpleVersion.Builder version(long rev) {
        return new SimpleVersion.Builder(rev);
    }
    
    public static VersionExpectation when(SimpleVersion.Builder builder) {
        return new VersionExpectation(builder.build());
    }
    public static VersionExpectation then(String title) {
        return new VersionExpectation(title);
    }

    @SafeVarargs
    public static <T> Set<T> setOf(T... revs) {
        return ImmutableSet.copyOf(revs);
    }
    
    public static Map<String, String> mapOf(String... entries) {
        Map<String, String> map = Maps.newHashMap();
        for (int i=0; i+1 < entries.length; i+=2) {
            map.put(entries[i], entries[i+1]);
        }
        return unmodifiableMap(map);
    }

    public static Multimap<String, String> multimapOf(String... entries) {
        Multimap<String, String> map = ArrayListMultimap.create();
        for (int i=0; i+1 < entries.length; i+=2) {
            map.put(entries[i], entries[i+1]);
        }
        return map;
    }
    
}
