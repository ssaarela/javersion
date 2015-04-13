package org.javersion.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.core.Revision.MAX_VALUE;
import static org.javersion.core.Revision.MIN_VALUE;

import org.junit.Test;

public class RevisionTest {

    @Test
    public void min_should_be_less_than_max() {
        assertThat(MIN_VALUE).isLessThan(MAX_VALUE);
        assertThat(MIN_VALUE.toString().compareTo(MAX_VALUE.toString())).isLessThan(0);
    }

    @Test
    public void new_revision_should_be_greater_than_min() {
        Revision revision = new Revision();
        assertThat(revision).isGreaterThan(MIN_VALUE);
        assertThat(revision.toString().compareTo(MIN_VALUE.toString())).isGreaterThan(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void bad_revision_format() {
        new Revision("foo-bar");
    }

    @Test
    public void same_given_node() {
        Revision r1 = new Revision(123l);
        Revision r2 = new Revision(123l);
        assertThat(r1.node).isEqualTo(r2.node);
        assertThat(r1.timeSeq).isLessThan(r2.timeSeq);
    }

    @Test
    public void not_equal_to_other_types() {
        assertThat(new Revision()).isNotEqualTo(new Object());
    }

    @Test
    public void new_revision_should_be_less_than_max() {
        Revision revision = new Revision();
        assertThat(revision).isLessThan(MAX_VALUE);
        assertThat(revision.toString().compareTo(MAX_VALUE.toString())).isLessThan(0);
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

    @Test
    public void to_string() {
        Revision revision = new Revision();
        String rev = revision.toString();
        assertThat(new Revision(rev)).isEqualTo(revision);
    }
}
