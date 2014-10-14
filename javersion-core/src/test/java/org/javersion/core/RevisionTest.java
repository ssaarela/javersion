package org.javersion.core;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.core.Revision.MAX_VALUE;
import static org.javersion.core.Revision.MIN_VALUE;

public class RevisionTest {

    @Test
    public void min_should_be_less_than_max() {
        assertThat(MIN_VALUE).isLessThan(MAX_VALUE);
    }

    @Test
    public void new_revision_should_be_greater_than_min() {
        assertThat(new Revision()).isGreaterThan(MIN_VALUE);
    }

    @Test
    public void new_revision_should_be_less_than_max() {
        assertThat(new Revision()).isLessThan(MAX_VALUE);
    }
}
