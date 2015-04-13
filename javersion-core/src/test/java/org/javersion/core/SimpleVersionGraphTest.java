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

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.javersion.core.Version.DEFAULT_BRANCH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.*;

public class SimpleVersionGraphTest {

    private static final Set<Revision> EMPTY_REVISIONS = setOf();

    private static final Map<String, String> EMPTY_PROPERTIES = mapOf();

    private static final String ALT_BRANCH = "alt-branch";

    private static final Revision[] REV = new Revision[50];

    static {
        for (int i=0; i < REV.length; i++) {
            REV[i] = new Revision(0, i);
        }
    }
    /**
     * <pre>
     *     default
     *        alt-branch
     *     1    firstName: "John", lastName: "Doe"
     *     | \
     *     2  |  status: "Single"
     *     |  |
     *     |  3  mood: "Lonely"
     *     |  |
     *     |  4  lastName: "Foe", status: "Just married", mood: "Ecstatic", married: "2013-10-12"
     *     |  |
     *     5 /   mood: "Ecstatic"
     *   / |/
     *  |  6    mood: null, married: null // unresolved status!
     *  |  |
     *  |  7    // still unresolved status!
     *  |  |
     *  |  8    status="Married" // resolve status
     *  |  |
     *  |  9    type: RESET, status: "New beginning"
     *  |  |
     *  |  |  10  status: Starts with conflict
     *  |  |  |
     *  |  |  11  purpose: "Reset alt-branch"
     *  |  | /
     *   \ 12   Full reset
     *    \|
     *     13  status: "Revert to #5", firstName: "John", lastName: "Doe", mood: "Ecstatic"
     * </pre>
     */
    public static List<VersionExpectation> EXPECTATIONS;

    static {
        ImmutableList.Builder b = ImmutableList.builder();
        b.add(
                when(version(REV[1])
                        .changeset(mapOf(
                                "firstName", "John",
                                "lastName", "Doe")))
                        .expectProperties(mapOf(
                                "firstName", "John",
                                "lastName", "Doe"))
        );
        b.add(
                then("Empty merge")
                        .expectAllHeads(setOf(REV[1]))
                        .mergeRevisions(EMPTY_REVISIONS)
                        .expectProperties(EMPTY_PROPERTIES)
        );
        b.add(
                when(version(REV[2])
                        .parents(setOf(REV[1]))
                        .changeset(mapOf(
                                "status", "Single")))
                        .expectProperties(mapOf(
                                "firstName", "John", // 1
                                "lastName", "Doe", // 1
                                "status", "Single")) // 2
        );
        b.add(
                when(version(REV[3])
                        .branch(ALT_BRANCH)
                        .parents(setOf(REV[1]))
                        .changeset(mapOf(
                                "mood", "Lonely")))
                        .expectAllHeads(setOf(REV[2], REV[3]))
                        .mergeRevisions(setOf(REV[3], REV[2]))
                        .expectProperties(mapOf(
                                "firstName", "John", // 1
                                "lastName", "Doe", // 1
                                "status", "Single", // 2
                                "mood", "Lonely")) // 3
        );
        b.add(
                when(version(REV[4])
                        .branch(ALT_BRANCH)
                        .parents(setOf(REV[3]))
                        .changeset(mapOf(
                                "lastName", "Foe",
                                "status", "Just married",
                                "mood", "Ecstatic",
                                "married", "2013-10-12")))
                        .expectAllHeads(setOf(REV[2], REV[4]))
                        .mergeRevisions(setOf(REV[4]))
                        .expectProperties(mapOf(
                                "firstName", "John", // 1
                                "lastName", "Foe", // 4
                                "status", "Just married", // 4
                                "mood", "Ecstatic", // 4
                                "married", "2013-10-12")) // 4
        );
        b.add(
                then("Merge with ancestor")
                        .mergeRevisions(setOf(REV[3], REV[4]))
                        .expectMergeHeads(setOf(REV[4]))
                        .expectProperties(mapOf(
                                "firstName", "John", // 1
                                "lastName", "Foe", // 4
                                "status", "Just married", // 4
                                "mood", "Ecstatic", // 4
                                "married", "2013-10-12")) // 4
        );
        b.add(
                then("Merge with concurrent older version")
                        .mergeRevisions(setOf(REV[2], REV[4]))
                        .expectMergeHeads(setOf(REV[2], REV[4]))
                        .expectProperties(mapOf(
                                "firstName", "John", // 1
                                "lastName", "Foe", // 4
                                "status", "Just married", // 4
                                "mood", "Ecstatic", // 4
                                "married", "2013-10-12")) // 4
                        .expectConflicts(multimapOf(
                                "status", "Single" // 2
                        ))
        );
        b.add(
                then("Merge with concurrent older version, ignore order")
                        .mergeRevisions(setOf(REV[4], REV[2]))
                        .expectMergeHeads(setOf(REV[2], REV[4]))
                        .expectProperties(mapOf(
                                "firstName", "John", // 1
                                "lastName", "Foe", // 4
                                "status", "Just married", // 4
                                "mood", "Ecstatic", // 4
                                "married", "2013-10-12")) // 4
                        .expectConflicts(multimapOf(
                                "status", "Single" // 2
                        ))
        );
        b.add(
                when(version(REV[5])
                        .parents(setOf(REV[2]))
                        .changeset(mapOf(
                                "mood", "Ecstatic")))
                        .expectAllHeads(setOf(REV[5], REV[4]))
                        .mergeRevisions(setOf(REV[4], REV[5]))
                        .expectProperties(mapOf(
                                "firstName", "John",
                                "lastName", "Foe",
                                "status", "Just married", // 4
                                "mood", "Ecstatic", // 4 and 5 - not conflicting!
                                "married", "2013-10-12")) // 4
                        .expectConflicts(multimapOf(
                                "status", "Single" // 2
                        ))
        );
        b.add(
                then("Merge default branch")
                        .mergeBranches(setOf(DEFAULT_BRANCH))
                        .expectMergeHeads(setOf(REV[5]))
                        .expectProperties(mapOf(
                                "firstName", "John", // 1
                                "lastName", "Doe", // 1
                                "status", "Single", // 2
                                "mood", "Ecstatic")) // 5
        );
        b.add(
                then("Merge alt-branch")
                        .mergeBranches(setOf(ALT_BRANCH))
                        .expectMergeHeads(setOf(REV[4]))
                        .expectProperties(mapOf(
                                "firstName", "John", // 1
                                "lastName", "Foe", // 4
                                "status", "Just married", // 4
                                "mood", "Ecstatic", // 5
                                "married", "2013-10-12")) // 4
        );
        b.add(
                when(version(REV[6])
                        .parents(setOf(REV[5], REV[4]))
                        .changeset(mapOf(
                                "mood", null,
                                "married", null)))
                        .expectAllHeads(setOf(REV[6], REV[4]))
                        .expectProperties(mapOf(
                                "firstName", "John",
                                "lastName", "Foe",
                                "status", "Just married")) // 4
                        .expectConflicts(multimapOf(
                                "status", "Single" // 2 - unresolved conflict
                        ))
        );
        b.add(
                then("Merge alt-branch - should not have changed")
                        .mergeBranches(setOf(ALT_BRANCH))
                        .expectMergeHeads(setOf(REV[4]))
                        .expectProperties(mapOf(
                                "firstName", "John", // 1
                                "lastName", "Foe", // 4
                                "status", "Just married", // 4
                                "mood", "Ecstatic", // 4
                                "married", "2013-10-12")) // 4
        );
        b.add(
                then("Merge alt-branch and default")
                        .mergeBranches(setOf(ALT_BRANCH, DEFAULT_BRANCH))
                        .expectMergeHeads(setOf(REV[6]))
                        .expectProperties(mapOf(
                                "firstName", "John",
                                "lastName", "Foe",
                                "status", "Just married")) // 4
                        .expectConflicts(multimapOf(
                                "status", "Single" // 2 - unresolved conflict
                        ))
        );
        b.add(
                when(version(REV[7])
                        .parents(setOf(REV[6])))
                        .expectProperties(mapOf(
                                "firstName", "John",
                                "lastName", "Foe",
                                "status", "Just married"))
                        .expectConflicts(multimapOf(
                                "status", "Single" // 2 - still unresolved conflict
                        ))
        );
        b.add(
                when(version(REV[8])
                        .parents(setOf(REV[7]))
                        .changeset(mapOf(
                                "status", "Married"
                        )))
                        .expectProperties(mapOf(
                                "firstName", "John",
                                "lastName", "Foe",
                                "status", "Married"))
        );
        b.add(
                when(version(REV[9])
                        .parents(setOf(REV[8]))
                        .changeset(mapOf(
                                "status", "New beginning"))
                        .type(VersionType.RESET))
                        .expectAllHeads(setOf(REV[9]))
                        .expectMergeHeads(setOf(REV[9]))
                        .expectProperties(mapOf(
                                "status", "New beginning"))
        );
        b.add(
                when(version(REV[10])
                        .branch(ALT_BRANCH)
                        .changeset(mapOf("status", "Starts with conflict")))
                        .mergeBranches(setOf(DEFAULT_BRANCH, ALT_BRANCH))
                        .expectAllHeads(setOf(REV[9], REV[10]))
                        .expectMergeHeads(setOf(REV[9], REV[10]))
                        .expectProperties(mapOf(
                                "status", "New beginning"
                        ))
                        .expectConflicts(multimapOf(
                                "status", "Starts with conflict"
                        ))
        );
        b.add(
                when(version(REV[11])
                        .parents(setOf(REV[10]))
                        .changeset(mapOf(
                                "purpose", "Reset alt-branch"))
                        .type(VersionType.RESET))
                        .mergeBranches(setOf(DEFAULT_BRANCH))
                        .expectAllHeads(setOf(REV[9], REV[11]))
                        .expectMergeHeads(setOf(REV[9], REV[11]))
                        .expectProperties(mapOf(
                                "status", "New beginning",
                                "purpose", "Reset alt-branch"))
        );
        b.add(
                when(version(REV[12]) // Full reset
                        .parents(setOf(REV[11], REV[9]))
                        .type(VersionType.RESET))
                        .expectAllHeads(setOf(REV[12]))
                        .expectProperties(mapOf())
        );
        b.add(
                when(version(REV[13])
                        .parents(setOf(REV[12], REV[5]))
                        .changeset(mapOf(
                                "status", "Revert to #5")))
                        .mergeBranches(setOf(DEFAULT_BRANCH))
                        .expectAllHeads(setOf(REV[13]))
                        .expectMergeHeads(setOf(REV[13]))
                        .expectProperties(mapOf(
                                "status", "Revert to #5",
                                "firstName", "John",
                                "lastName", "Doe",
                                "mood", "Ecstatic"))
        );

        EXPECTATIONS = b.build();
    }

    @Test
    public void Sequential_Updates() {
        SimpleVersionGraph versionGraph = SimpleVersionGraph.init();
        Revision revision = null;
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
    public void Bulk_Init() {
        Revision revision = null;
        for (List<VersionExpectation> expectations : getBulkExpectations()) {
            VersionExpectation expectation = expectations.get(expectations.size() - 1);
            if (expectation.getRevision() != null) {
                revision = expectation.getRevision();
            }
            SimpleVersionGraph versionGraph = SimpleVersionGraph.init(getVersions(expectations));
            assertGraphExpectations(versionGraph, revision, expectation);
            assertMergeExpectations(versionGraph, revision, expectation);
        }
    }

    @Test
    public void Visit_Older_Versions() {
        SimpleVersionGraph versionGraph = SimpleVersionGraph.init(getVersions(EXPECTATIONS));
        runExpectations(versionGraph, EXPECTATIONS);
    }

    @Test
    public void Bulk_Commit() {
        SimpleVersionGraph versionGraph = SimpleVersionGraph.init(EXPECTATIONS.get(0).version);
        versionGraph = versionGraph.commit(getVersions(EXPECTATIONS.subList(1, EXPECTATIONS.size())));
        runExpectations(versionGraph, EXPECTATIONS);
    }

    @Test
    public void Tip_of_an_Empty_Graph() {
        SimpleVersionGraph versionGraph = SimpleVersionGraph.init();
        assertNull(versionGraph.getTip());
        assertTrue(versionGraph.getVersions().isEmpty());
    }

    @Test(expected = VersionNotFoundException.class)
    public void Version_Not_Found() {
        SimpleVersionGraph.init().getVersionNode(new Revision());
    }

    @Test
    public void VersionNode_Should_Analyze_Actual_Changeset() {
        SimpleVersion v1 = new SimpleVersion.Builder()
                .changeset(ImmutableMap.of("id", "id1", "name", "name1"))
                .build();

        // v2.parents == v3.parents, non-conflicting changes from not-a-diff-based versions

        SimpleVersion v2 = new SimpleVersion.Builder()
                .changeset(ImmutableMap.of("id", "id2", "name", "name1"))
                .parents(v1.revision)
                .build();

        SimpleVersion v3 = new SimpleVersion.Builder()
                .changeset(ImmutableMap.of("id", "id1", "name", "name2"))
                .parents(v1.revision)
                .build();

        SimpleVersionGraph versionGraph = SimpleVersionGraph.init(asList(v1, v2, v3));

        VersionNode versionNode = versionGraph.getVersionNode(v2.revision);
        assertThat(versionNode.getChangeset(), equalTo(ImmutableMap.of("id", "id2")));
        Version<String, String, String> version = versionNode.getVersion();
        assertNotSame(version, v2);
        assertThat(version.revision, equalTo(v2.revision));
        assertThat(version.type, equalTo(v2.type));
        assertThat(version.branch, equalTo(v2.branch));
        assertThat(version.parentRevisions, equalTo(v2.parentRevisions));
        assertThat(version.meta, equalTo(v2.meta));
        // VersionNode.getVersion() reflects actual changes
        assertThat(version.changeset, equalTo(ImmutableMap.of("id", "id2")));

        versionNode = versionGraph.getVersionNode(v3.revision);
        assertThat(versionNode.getChangeset(), equalTo(ImmutableMap.of("name", "name2")));

        Merge<String, String, String> merge = versionGraph.mergeBranches(DEFAULT_BRANCH);
        assertTrue(merge.getConflicts().isEmpty());
        assertThat(merge.getProperties(), equalTo(ImmutableMap.of("id", "id2", "name", "name2")));
    }

    private int findIndex(List<VersionExpectation> expectations, int versionNumber) {
        for (int i=0; i < expectations.size(); i++) {
            Revision revision = expectations.get(i).getRevision();
            if (revision != null && revision.equals(REV[versionNumber])) {
                return i;
            }
        }
        throw new VersionNotFoundException(REV[versionNumber]);
    }

    private void runExpectations(SimpleVersionGraph versionGraph, List<VersionExpectation> expectations) {
        for (VersionExpectation expectation : expectations) {
            Revision revision = expectation.getRevision();
            if (revision != null) {
                versionGraph = versionGraph.at(revision);
            }
            assertGraphExpectations(versionGraph, revision, expectation);
            assertMergeExpectations(versionGraph, revision, expectation);
        }
    }

    private Iterable<SimpleVersion> getVersions(
            List<VersionExpectation> expectations) {
        return filter(transform(expectations, getVersion), notNull());
    }

    private void assertGraphExpectations(SimpleVersionGraph versionGraph, Revision revision, VersionExpectation expectation) {
        if (expectation.expectedHeads != null) {
            Set<Revision> heads = new HashSet<>();
            for (BranchAndRevision leaf : versionGraph.getHeads().keys()) {
                heads.add(leaf.revision);
            }
            assertThat(title("heads", revision, expectation),
                    heads,
                    equalTo(expectation.expectedHeads));
        }
    }

    private void assertMergeExpectations(SimpleVersionGraph versionGraph, Revision revision, VersionExpectation expectation) {
        try {
            Merge<String, String, String> merge;
            if (expectation.mergeBranches != null) {
                merge = versionGraph.mergeBranches(expectation.mergeBranches);
            } else {
                merge = versionGraph.mergeRevisions(expectation.mergeRevisions);
            }
            assertThat(title("mergeHeads", revision, expectation),
                    merge.getMergeHeads(),
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

    private static String title(String assertLabel, Revision revision, VersionExpectation expectation) {
        return assertLabel + " of " + revision + (expectation.title != null ? ": " + expectation.title : "");
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
        public Set<Revision> mergeRevisions = ImmutableSet.of();
        public Iterable<String> mergeBranches;
        public Map<String, String> expectedProperties;
        public Multimap<String, String> expectedConflicts = ImmutableMultimap.of();
        public Set<Revision> expectedMergeHeads;
        public Set<Revision> expectedHeads;
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
        public Revision getRevision() {
            return version != null ? version.revision : null;
        }
        public VersionExpectation mergeRevisions(Set<Revision> mergeRevisions) {
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
        public VersionExpectation expectMergeHeads(Set<Revision> expectedRevisions) {
            this.expectedMergeHeads = expectedRevisions;
            return this;
        }
        public VersionExpectation expectAllHeads(Set<Revision> expectedHeads) {
            this.expectedHeads = expectedHeads;
            return this;
        }
        public VersionExpectation expectConflicts(Multimap<String, String> expectedConflicts) {
            this.expectedConflicts = expectedConflicts;
            return this;
        }
    }

    public static SimpleVersion.Builder version(Revision rev) {
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
