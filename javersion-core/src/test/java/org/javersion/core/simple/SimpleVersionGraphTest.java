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
import static org.javersion.core.Version.DEFAULT_BRANCH;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javersion.core.BranchAndRevision;
import org.javersion.core.Merge;
import org.javersion.core.VersionType;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class SimpleVersionGraphTest {
    
    private static final Set<Long> EMPTY_REVISIONS = setOf();
    
    private static final Map<String, String> EMPTY_PROPERTIES = mapOf();
    
    private static final String ALT_BRANCH = "alt-branch"; 
    /**
     * <pre>
     * default
     *    alt-branch
     * 1    firstName: "John", lastName: "Doe"
     * | \   
     * 2  |  status: "Single"
     * |  |
     * |  3  mood: "Lonely"
     * |  |
     * |  4  lastName: "Foe", status: "Just married", mood: "Ecstatic", married: "2013-10-12" 
     * |  |
     * 5 /   mood: "Ecstatic"
     * |/
     * 6    mood: null, married: null // unresolved status!
     * |
     * 7    // still unresolved status!
     * |
     * 8    status="Married" // resolve status
     * :
     * 9    type: ROOT, status: "New beginning"
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
                .expectAllHeads(setOf(1l))
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
                .branch(ALT_BRANCH)
                .parents(setOf(1l))
                .properties(mapOf(
                        "mood", "Lonely")))
                .expectAllHeads(setOf(2l, 3l))
                .mergeRevisions(setOf(3l, 2l))
                .expectProperties(mapOf(
                        "firstName", "John", // 1
                        "lastName", "Doe", // 1
                        "status", "Single", // 2
                        "mood", "Lonely")), // 3


            when(version(4l)
                .branch(ALT_BRANCH)
                .parents(setOf(3l))
                .properties(mapOf(
                        "lastName", "Foe",
                        "status", "Just married",
                        "mood", "Ecstatic",
                        "married", "2013-10-12")))
                .expectAllHeads(setOf(2l, 4l))
                .mergeRevisions(setOf(4l))
                .expectProperties(mapOf(
                        "firstName", "John", // 1
                        "lastName", "Foe", // 4
                        "status", "Just married", // 4
                        "mood", "Ecstatic", // 4
                        "married", "2013-10-12")), // 4

            then("Merge with ancestor")
                .mergeRevisions(setOf(3l, 4l))
                .expectMergeHeads(setOf(4l))
                .expectProperties(mapOf(
                        "firstName", "John", // 1
                        "lastName", "Foe", // 4
                        "status", "Just married", // 4
                        "mood", "Ecstatic", // 4
                        "married", "2013-10-12")), // 4

            then("Merge with concurrent older version")
                .mergeRevisions(setOf(2l, 4l))
                .expectMergeHeads(setOf(2l, 4l))
                .expectProperties(mapOf(
                        "firstName", "John", // 1
                        "lastName", "Foe", // 4
                        "status", "Just married", // 4
                        "mood", "Ecstatic", // 4
                        "married", "2013-10-12")) // 4
                .expectConflicts(multimapOf(
                        "status", "Single" // 2
                        )),

            then("Merge with concurrent older version, ignore order")
                .mergeRevisions(setOf(4l, 2l))
                .expectMergeHeads(setOf(2l, 4l))
                .expectProperties(mapOf(
                        "firstName", "John", // 1
                        "lastName", "Foe", // 4
                        "status", "Just married", // 4
                        "mood", "Ecstatic", // 4
                        "married", "2013-10-12")) // 4
                .expectConflicts(multimapOf(
                        "status", "Single" // 2
                        )),                


            when(version(5l)
                .parents(setOf(2l))
                .properties(mapOf(
                        "mood", "Ecstatic")))
                .expectAllHeads(setOf(5l, 4l))
                .mergeRevisions(setOf(4l, 5l))
                .expectProperties(mapOf(
                        "firstName", "John",
                        "lastName", "Foe",
                        "status", "Just married", // 4
                        "mood", "Ecstatic", // 4 and 5 - not conflicting!
                        "married", "2013-10-12")) // 4
                .expectConflicts(multimapOf(
                        "status", "Single" // 2
                        )),  
                        
            then("Merge default branch")
                .mergeBranches(setOf(DEFAULT_BRANCH))
                .expectMergeHeads(setOf(5l))
                .expectProperties(mapOf(
                        "firstName", "John", // 1
                        "lastName", "Doe", // 1
                        "status", "Single", // 2
                        "mood", "Ecstatic")), // 5
                        
            then("Merge alt-branch")
                .mergeBranches(setOf(ALT_BRANCH))
                .expectMergeHeads(setOf(4l))
                .expectProperties(mapOf(
                        "firstName", "John", // 1
                        "lastName", "Foe", // 4
                        "status", "Just married", // 4
                        "mood", "Ecstatic", // 4
                        "married", "2013-10-12")), // 4


            when(version(6l)
                .parents(setOf(5l, 4l))
                .properties(mapOf(
                        "mood", null,
                        "married", null)))
                .expectAllHeads(setOf(6l, 4l))
                .expectProperties(mapOf(
                        "firstName", "John",
                        "lastName", "Foe",
                        "status", "Just married")) // 4
                .expectConflicts(multimapOf(
                        "status", "Single" // 2 - unresolved conflict
                        )),
                        
                        
            then("Merge alt-branch - should not have changed")
                .mergeBranches(setOf(ALT_BRANCH))
                .expectMergeHeads(setOf(4l))
                .expectProperties(mapOf(
                        "firstName", "John", // 1
                        "lastName", "Foe", // 4
                        "status", "Just married", // 4
                        "mood", "Ecstatic", // 4
                        "married", "2013-10-12")), // 4
                        
            then("Merge alt-branch and default")
                .mergeBranches(setOf(ALT_BRANCH, DEFAULT_BRANCH))
                .expectMergeHeads(setOf(6l))
                .expectProperties(mapOf(
                        "firstName", "John",
                        "lastName", "Foe",
                        "status", "Just married")) // 4
                .expectConflicts(multimapOf(
                        "status", "Single" // 2 - unresolved conflict
                        )),

                        
            when(version(7l)
                .parents(setOf(6l)))
                .expectProperties(mapOf(
                        "firstName", "John",
                        "lastName", "Foe",
                        "status", "Just married"))
                .expectConflicts(multimapOf(
                        "status", "Single" // 2 - still unresolved conflict
                        )),
                        
                        
            when(version(8l)
                .parents(setOf(7l))
                .properties(mapOf(
                        "status", "Married"
                        )))
                .expectProperties(mapOf(
                        "firstName", "John",
                        "lastName", "Foe",
                        "status", "Married")),
            

            when(version(9l)
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
            assertGraphExpectations(versionGraph, revision, expectation);
            assertMergeExpectations(versionGraph, revision, expectation);
        }
    }
    
    static List<List<VersionExpectation>> getBulkExpectations() {
        List<List<VersionExpectation>> bulks = Lists.newArrayList();
        for (int i=1; i<= EXPECTATIONS.size(); i++) {
            bulks.add(EXPECTATIONS.subList(0, i));
        }
        return bulks;
    }

    @Test
    public void Bulk_Load() {
        Long revision = null;
        for (List<VersionExpectation> expectations : getBulkExpectations()) {
            VersionExpectation lastExpectation = expectations.get(expectations.size() - 1);
            if (lastExpectation.getRevision() != null) {
                revision = lastExpectation.getRevision();
            }
            SimpleVersionGraph versionGraph = SimpleVersionGraph.init(getVersions(expectations));
            VersionExpectation expectation = expectations.get(expectations.size() - 1);
            assertGraphExpectations(versionGraph, revision, expectation);
            assertMergeExpectations(versionGraph, revision, expectation);
        }
    }

    private Iterable<SimpleVersion> getVersions(
            List<VersionExpectation> expectations) {
        return filter(transform(expectations, getVersion), notNull());
    }
    
    private void assertGraphExpectations(SimpleVersionGraph versionGraph, long revision, VersionExpectation expectation) {
        if (expectation.expectedHeads != null) {
            Set<Long> heads = new HashSet<>();
            for (BranchAndRevision leaf : versionGraph.getHeads().keys()) {
                heads.add(leaf.revision);
            }
            assertThat(title("heads", revision, expectation),
                    heads,
                    equalTo(expectation.expectedHeads));
        }
    }

    private void assertMergeExpectations(SimpleVersionGraph versionGraph, long revision, VersionExpectation expectation) {
        try {
            Merge<String, String> merge;
            if (expectation.mergeBranches != null) {
                merge = versionGraph.mergeBranches(expectation.mergeBranches);
            } else {
                merge = versionGraph.mergeRevisions(expectation.mergeRevisions);
            }
            assertThat(title("mergeHeads", revision, expectation), 
                    merge.getHeads(), 
                    equalTo(expectation.expectedMergeHeads));
            
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
        public Iterable<String> mergeBranches;
        public Map<String, String> expectedProperties;
        public Multimap<String, String> expectedConflicts = ImmutableMultimap.of();
        public Set<Long> expectedMergeHeads;
        public Set<Long> expectedHeads;
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
            this.expectedMergeHeads = mergeRevisions;
        }
        public Long getRevision() {
            return version != null ? version.revision : null;
        }
        public VersionExpectation mergeRevisions(Set<Long> mergeRevisions) {
            this.mergeRevisions = mergeRevisions;
            this.expectedMergeHeads = mergeRevisions;
            return this;
        }
        public VersionExpectation mergeBranches(Iterable<String> mergeBranches) {
            this.mergeBranches = mergeBranches;
            return this;
        }
        public VersionExpectation expectProperties(Map<String, String> expectedProperties) {
            this.expectedProperties = expectedProperties;
            return this;
        }
        public VersionExpectation expectMergeHeads(Set<Long> expectedRevisions) {
            this.expectedMergeHeads = expectedRevisions;
            return this;
        }
        public VersionExpectation expectAllHeads(Set<Long> expectedHeads) {
            this.expectedHeads = expectedHeads;
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
