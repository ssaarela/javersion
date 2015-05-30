package org.javersion.object;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.core.Version.DEFAULT_BRANCH;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.javersion.core.Version;
import org.javersion.core.VersionGraph;
import org.javersion.path.PropertyPath;
import org.junit.Test;

public class PartialProjectionTest {

    @Target({ FIELD })
    @Retention(RUNTIME)
    @Documented
    public @interface Info {}

    public static class Projection {
        @Info
        public String label;
        public String detail;
    }

    private ObjectSerializer<Projection> infoSerializer = new ObjectSerializer<Projection>(Projection.class,
            TypeMappings.builder().withClass(Projection.class).withFilter(field -> field.hasAnnotation(Info.class)).build());

    private ObjectSerializer<Projection> fullSerializer = new ObjectSerializer<Projection>(Projection.class,
            TypeMappings.builder().withClass(Projection.class).build());

    private VersionGraph<PropertyPath, Object, Void, ?, ?> versionGraph = ObjectVersionGraph.init();

    @Test
    public void different_projections_from_same_data() {
        Projection projection = new Projection();
        projection.label = "Label";
        projection.detail = "Details";

        fullManager().versionBuilder(projection).build();

        ObjectVersionManager<Projection, Void> manager = infoManager();
        projection = manager.mergeBranches(DEFAULT_BRANCH).object;
        assertThat(projection.label).isEqualTo("Label");
        assertThat(projection.detail).isNull();

        projection.label = "Changed label";
        manager.versionBuilder(projection).build();

        projection = fullManager().mergeBranches(DEFAULT_BRANCH).object;
        assertThat(projection.label).isEqualTo("Changed label");
        assertThat(projection.detail).isEqualTo("Details");
    }

    private ObjectVersionManager<Projection, Void> fullManager() {
        return manager(fullSerializer);
    }

    private ObjectVersionManager<Projection, Void> infoManager() {
        return manager(infoSerializer);
    }

    private ObjectVersionManager<Projection, Void> manager(ObjectSerializer<Projection> serializer) {
        return new ObjectVersionManager<Projection, Void>(serializer, true) {
            @Override
            public void commit(Version<PropertyPath, Object, Void> version) {
                super.commit(version);
                PartialProjectionTest.this.versionGraph = getVersionGraph();
            }
        }.init(versionGraph);
    }

}
