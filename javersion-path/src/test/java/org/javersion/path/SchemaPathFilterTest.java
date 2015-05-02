package org.javersion.path;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import static org.javersion.path.PropertyPath.ROOT;

public class SchemaPathFilterTest {

    @Test
    public void allow_only_root() {
        Schema<String> schema = Schema.<String>builder().build();
        SchemaPathFilter filter = new SchemaPathFilter(schema);
        assertThat(filter.apply(ROOT)).isTrue();
        assertThat(filter.apply(ROOT.property("a"))).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_schema_not_allowed() {
        new SchemaPathFilter(null);
    }
}
