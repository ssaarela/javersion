package org.javersion.object;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.object.TestUtil.properties;
import static org.javersion.reflect.TypeDescriptors.getTypeDescriptor;

import java.util.Map;

import org.javersion.core.Persistent;
import org.javersion.path.PropertyPath;
import org.junit.Test;

public class PropertyIgnoreTest {

    @Versionable
    public static class Ignoramus {
        public transient Long transientField;
        @VersionIgnore
        public Long ignoredField;
        public Long field;
    }

    private final ObjectSerializer<Ignoramus> serializer = new ObjectSerializer<>(Ignoramus.class);

    @Test
    public void Ignored_And_Transient_Fields() {
        Ignoramus ig = new Ignoramus();
        ig.transientField = 123l;
        ig.ignoredField = 567l;
        ig.field = 890l;
        Map<PropertyPath, Object> properties = serializer.toPropertyMap(ig);
        assertThat(properties).isEqualTo(properties(
                "", Persistent.object(getTypeDescriptor(Ignoramus.class).getSimpleName()),
                "field", 890l
        ));

        ig = serializer.fromPropertyMap(properties);
        assertThat(ig.transientField).isNull();
        assertThat(ig.ignoredField).isNull();
        assertThat(ig.field).isEqualTo(890l);
    }
}
