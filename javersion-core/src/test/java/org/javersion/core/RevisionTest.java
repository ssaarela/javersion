package org.javersion.core;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.core.Revision.MAX_VALUE;
import static org.javersion.core.Revision.MIN_VALUE;

import java.util.Arrays;

import com.eaio.uuid.UUID;

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

    @Test
    public void unique_time() {
        final int len = 100000;
        long[] times = new long[len];
        for (int i=0; i < times.length; i++) {
            times[i] = Revision.newUniqueTime();
        }
        for (int i=0; i < len - 1; i++) {
            assertThat(times[i]).isLessThan(times[i+1]);
        }
    }
}
