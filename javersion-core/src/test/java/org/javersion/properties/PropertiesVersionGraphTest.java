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
import org.javersion.core.Revision;
import org.javersion.core.VersionType;
import org.javersion.properties.PropertiesVersionGraph;
import org.javersion.properties.PropertiesVersion;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class PropertiesVersionGraphTest {

    private static final Set<Revision> EMPTY_REVISIONS = setOf();

    private static final Map<String, String> EMPTY_PROPERTIES = mapOf();

    private static final String ALT_BRANCH = "alt-branch";

    private static final Revision[] REV = new Revision[50];

    static {
        for (int i=0; i < REV.length; i++) {
            REV[i] = new Revision();
        }
    }
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
            when(version(REV[1])
                .changeset(mapOf(
                        "firstName", "John",
                        "lastName", "Doe")))
                .expectProperties(mapOf(
                        "firstName", "John",
                        "lastName", "Doe")),

            then("Empty merge")
                .expectAllHeads(setOf(REV[1]))
                .mergeRevisions(EMPTY_REVISIONS)
                .expectProperties(EMPTY_PROPERTIES),


            when(version(REV[2])
                .parents(setOf(REV[1]))
                .changeset(mapOf(
                        "status", "Single")))
                .expectProperties(mapOf(
                        "firstName", "John", // 1
                        "lastName", "Doe", // 1
                        "status", "Single")), // 2


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
                        "mood", "Lonely")), // 3


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
                        "married", "2013-10-12")), // 4

            then("Merge with ancestor")
                .mergeRevisions(setOf(REV[3], REV[4]))
                .expectMergeHeads(setOf(REV[4]))
                .expectProperties(mapOf(
                        "firstName", "John", // 1
                        "lastName", "Foe", // 4
                        "status", "Just married", // 4
                        "mood", "Ecstatic", // 4
                        "married", "2013-10-12")), // 4

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
                        )),

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
                        )),


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
                        )),

            then("Merge default branch")
                .mergeBranches(setOf(DEFAULT_BRANCH))
                .expectMergeHeads(setOf(REV[5]))
                .expectProperties(mapOf(
                        "firstName", "John", // 1
                        "lastName", "Doe", // 1
                        "status", "Single", // 2
                        "mood", "Ecstatic")), // 5

            then("Merge alt-branch")
                .mergeBranches(setOf(ALT_BRANCH))
                .expectMergeHeads(setOf(REV[4]))
                .expectProperties(mapOf(
                        "firstName", "John", // 1
                        "lastName", "Foe", // 4
                        "status", "Just married", // 4
                        "mood", "Ecstatic", // 4
                        "married", "2013-10-12")), // 4


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
                        )),


            then("Merge alt-branch - should not have changed")
                .mergeBranches(setOf(ALT_BRANCH))
                .expectMergeHeads(setOf(REV[4]))
                .expectProperties(mapOf(
                        "firstName", "John", // 1
                        "lastName", "Foe", // 4
                        "status", "Just married", // 4
                        "mood", "Ecstatic", // 4
                        "married", "2013-10-12")), // 4

            then("Merge alt-branch and default")
                .mergeBranches(setOf(ALT_BRANCH, DEFAULT_BRANCH))
                .expectMergeHeads(setOf(REV[6]))
                .expectProperties(mapOf(
                        "firstName", "John",
                        "lastName", "Foe",
                        "status", "Just married")) // 4
                .expectConflicts(multimapOf(
                        "status", "Single" // 2 - unresolved conflict
                        )),


            when(version(REV[7])
                .parents(setOf(REV[6])))
                .expectProperties(mapOf(
                        "firstName", "John",
                        "lastName", "Foe",
                        "status", "Just married"))
                .expectConflicts(multimapOf(
                        "status", "Single" // 2 - still unresolved conflict
                        )),


            when(version(REV[8])
                .parents(setOf(REV[7]))
                .changeset(mapOf(
                        "status", "Married"
                        )))
                .expectProperties(mapOf(
                        "firstName", "John",
                        "lastName", "Foe",
                        "status", "Married")),


            when(version(REV[9])
                .changeset(mapOf(
                        "status", "New beginning"))
                .type(VersionType.ROOT))
                .expectProperties(mapOf(
                        "status", "New beginning")) // 4 and 5 - not conflicting!
            );


    @Test
    public void Sequential_Updates() {
        PropertiesVersionGraph versionGraph = PropertiesVersionGraph.init();
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
    public void Bulk_Load() {
        Revision revision = null;
        for (List<VersionExpectation> expectations : getBulkExpectations()) {
            VersionExpectation lastExpectation = expectations.get(expectations.size() - 1);
            if (lastExpectation.getRevision() != null) {
                revision = lastExpectation.getRevision();
            }
            PropertiesVersionGraph versionGraph = PropertiesVersionGraph.init(getVersions(expectations));
            VersionExpectation expectation = expectations.get(expectations.size() - 1);
            assertGraphExpectations(versionGraph, revision, expectation);
            assertMergeExpectations(versionGraph, revision, expectation);
        }
    }

    private Iterable<PropertiesVersion> getVersions(
            List<VersionExpectation> expectations) {
        return filter(transform(expectations, getVersion), notNull());
    }

    private void assertGraphExpectations(PropertiesVersionGraph versionGraph, Revision revision, VersionExpectation expectation) {
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

    private void assertMergeExpectations(PropertiesVersionGraph versionGraph, Revision revision, VersionExpectation expectation) {
        try {
            Merge<String, String> merge;
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

    public static Function<VersionExpectation, PropertiesVersion> getVersion = new Function<VersionExpectation, PropertiesVersion>() {
        @Override
        public PropertiesVersion apply(VersionExpectation input) {
            return input.version;
        }
    };

    public static class VersionExpectation {
        public final String title;
        public final PropertiesVersion version;
        public Set<Revision> mergeRevisions = ImmutableSet.of();
        public Iterable<String> mergeBranches;
        public Map<String, String> expectedProperties;
        public Multimap<String, String> expectedConflicts = ImmutableMultimap.of();
        public Set<Revision> expectedMergeHeads;
        public Set<Revision> expectedHeads;
        public VersionExpectation(String title) {
            this(null, title);
        }
        public VersionExpectation(PropertiesVersion version) {
            this(version, null);
        }
        public VersionExpectation(PropertiesVersion version, String title) {
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

    public static PropertiesVersion.Builder version(Revision rev) {
        return new PropertiesVersion.Builder(rev);
    }

    public static VersionExpectation when(PropertiesVersion.Builder builder) {
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
