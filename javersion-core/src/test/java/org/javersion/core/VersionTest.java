package org.javersion.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.core.DiffTest.map;

import java.util.Map;

import org.junit.Test;

public class VersionTest {

    @Test
    public void get_version_properties() {
        Version<String, String, String> version = new Version.Builder<String, String, String>()
                .changeset(map("key", "value"))
                .build();
        Map<String, VersionProperty<String>> props = version.getVersionProperties();
        assertThat(props).hasSize(1);
        VersionProperty<String> prop = props.get("key");
        assertThat(prop.revision).isEqualTo(version.revision);
        assertThat(prop.value).isEqualTo("value");
    }

    @Test
    public void changeset() {
        SimpleVersionGraph versionGraph = SimpleVersionGraph.init();

        Version<String, String, String> version = new Version.Builder<String, String, String>()
                .changeset(null, versionGraph)
                .build();
        assertThat(version.changeset).isEmpty();

        version = new Version.Builder<String, String, String>()
                .changeset(map("key", "value"), versionGraph)
                .build();
        assertThat(version.changeset).isEqualTo(map("key", "value"));

        versionGraph = versionGraph.commit(version);

        Revision parent = version.revision;
        version = new Version.Builder<String, String, String>()
                .parents(parent)
                .changeset(map("key", "value2"), versionGraph)
                .build();
        assertThat(version.changeset).isEqualTo(map("key", "value2"));

        version = new Version.Builder<String, String, String>()
                .parents(parent)
                .changeset(null, versionGraph)
                .build();
        assertThat(version.changeset).isEqualTo(map("key", null));
    }

    public static class FilteredChangesetTests {

        final SimpleVersion v1 = new SimpleVersion.Builder()
                .changeset(map("key1", "value1", "key2", "value2"))
                .build();

        final SimpleVersionGraph versionGraph = SimpleVersionGraph.init(v1);

        @Test
        public void all() {
            SimpleVersion version = new SimpleVersion.Builder()
                    .parents(v1.revision)
                    .changeset(map("key", "value2"), versionGraph, k -> false)
                    .build();
            assertThat(version.changeset).isEqualTo(map());
        }

        @Test
        public void changes() {
            SimpleVersion version = new SimpleVersion.Builder()
                    .parents(v1.revision)
                    .changeset(map("key1", "change", "key2", "change"), versionGraph,
                            k -> k.equals("key1"))
                    .build();
            assertThat(version.changeset).isEqualTo(map("key1", "change"));
        }

        @Test
        public void delete() {
            SimpleVersion version = new SimpleVersion.Builder()
                    .parents(v1.revision)
                    .changeset(null, versionGraph, k -> k.equals("key1"))
                    .build();
            assertThat(version.changeset).isEqualTo(map("key1",null));
        }
    }

}
