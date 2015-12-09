package org.javersion.store.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.store.sql.QEntityVersionParent.entityVersionParent;

import org.junit.Test;

public class ConfigurationTest {

    @Test
    public void JVersionParent_equality() {
        JVersionParent copyParent = new JVersionParent(entityVersionParent);
        JVersionParent parent = new JVersionParent("PUBLIC", "ENTITY_VERSION_PARENT");
        JVersionParent defaultParent = new JVersionParent("ENTITY");
        assertThat(copyParent).isEqualTo(parent);
        assertThat(copyParent.hashCode()).isEqualTo(parent.hashCode());

        assertThat(defaultParent).isEqualTo(parent);
        assertThat(defaultParent.hashCode()).isEqualTo(parent.hashCode());

        assertThat(copyParent.revision).isEqualTo(parent.revision);
        assertThat(copyParent.revision.hashCode()).isEqualTo(parent.revision.hashCode());

        assertThat(copyParent.parentRevision).isEqualTo(parent.parentRevision);
        assertThat(copyParent.parentRevision.hashCode()).isEqualTo(parent.parentRevision.hashCode());
    }

    @Test
    public void JVersionProperty_equality() {
        JVersionProperty copyProperty = new JVersionProperty(entityVersionParent);
        JVersionProperty property = new JVersionProperty("PUBLIC", "ENTITY_VERSION_PARENT");
        assertThat(copyProperty).isEqualTo(property);
        assertThat(copyProperty.hashCode()).isEqualTo(property.hashCode());

        assertThat(copyProperty.revision).isEqualTo(property.revision);
        assertThat(copyProperty.revision.hashCode()).isEqualTo(property.revision.hashCode());

        assertThat(copyProperty.path).isEqualTo(property.path);
        assertThat(copyProperty.path.hashCode()).isEqualTo(property.path.hashCode());

        assertThat(copyProperty.type).isEqualTo(property.type);
        assertThat(copyProperty.type.hashCode()).isEqualTo(property.type.hashCode());

        assertThat(copyProperty.nbr).isEqualTo(property.nbr);
        assertThat(copyProperty.nbr.hashCode()).isEqualTo(property.nbr.hashCode());

        assertThat(copyProperty.str).isEqualTo(property.str);
        assertThat(copyProperty.str.hashCode()).isEqualTo(property.str.hashCode());
    }
}
