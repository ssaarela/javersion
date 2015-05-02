package org.javersion.core;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import static org.assertj.core.api.Assertions.*;

import javafx.scene.shape.SVGPath;

public class VersionTest {

    @Test
    public void get_version_properties() {
        Version<String, String, String> version = new Version.Builder<String, String, String>()
                .changeset(ImmutableMap.of("key", "value"))
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
                .changeset(ImmutableMap.of("key", "value"), versionGraph)
                .build();
        assertThat(version.changeset).isEqualTo(ImmutableMap.of("key", "value"));

        versionGraph = versionGraph.commit(version);

        Revision parent = version.revision;
        version = new Version.Builder<String, String, String>()
                .parents(parent)
                .changeset(ImmutableMap.of("key", "value2"), versionGraph)
                .build();
        assertThat(version.changeset).isEqualTo(ImmutableMap.of("key", "value2"));

        version = new Version.Builder<String, String, String>()
                .parents(parent)
                .changeset(null, versionGraph)
                .build();
        assertThat(version.changeset).isEqualTo(DiffTest.map("key", null));
    }
}
