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
    public void new_revision_should_be_less_than_max() {
        Revision revision = new Revision();
        assertThat(revision).isLessThan(MAX_VALUE);
        assertThat(revision.toString().compareTo(MAX_VALUE.toString())).isLessThan(0);
    }

    @Test
    public void not_equal_to_other_types() {
        assertThat(new Revision()).isNotEqualTo(new Object());
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

    @Test
    public void decode() {
        assertDecode("0000000000000-000000000000O", 0, 0);
        assertDecode("000000000000o-0000000000001", 0, 1);
        assertDecode("000000000000I-000000000000i", 1, 1);
        assertDecode("000000000000L-000000000000l", 1, 1);
        assertDecode("0000000000002-0000000000003", 2, 3);
        assertDecode("0000000000004-0000000000005", 4, 5);
        assertDecode("0000000000006-0000000000007", 6, 7);
        assertDecode("0000000000008-0000000000009", 8, 9);
        assertDecode("000000000000A-000000000000B", 10, 11);
        assertDecode("000000000000a-000000000000b", 10, 11);
        assertDecode("000000000000C-000000000000D", 12, 13);
        assertDecode("000000000000c-000000000000d", 12, 13);
        assertDecode("000000000000E-000000000000F", 14, 15);
        assertDecode("000000000000e-000000000000f", 14, 15);
        assertDecode("000000000000G-000000000000H", 16, 17);
        assertDecode("000000000000g-000000000000h", 16, 17);
        assertDecode("000000000000J-000000000000K", 18, 19);
        assertDecode("000000000000j-000000000000k", 18, 19);
        assertDecode("000000000000M-000000000000N", 20, 21);
        assertDecode("000000000000m-000000000000n", 20, 21);
        assertDecode("000000000000P-000000000000Q", 22, 23);
        assertDecode("000000000000p-000000000000q", 22, 23);
        assertDecode("000000000000r-000000000000s", 24, 25);
        assertDecode("000000000000R-000000000000S", 24, 25);
        assertDecode("000000000000T-000000000000V", 26, 27);
        assertDecode("000000000000t-000000000000v", 26, 27);
        assertDecode("000000000000W-000000000000X", 28, 29);
        assertDecode("000000000000w-000000000000x", 28, 29);
        assertDecode("000000000000Y-000000000000Z", 30, 31);
        assertDecode("000000000000y-000000000000z", 30, 31);
        assertDecode("0000000000010-0000000000011", 32, 33);
        assertDecode("FZZZZZZZZZZZZ-fzzzzzzzzzzzz", -1, -1);
        assertDecode("ZZZZZZZZZZZZZ-zzzzzzzzzzzzz", -1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decode_illegal_character_within_bounds() {
        new Revision("0000000000000-000000000000.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void decode_illegal_character_out_off_bounds() {
        new Revision("0000000000000-000000000000|");
    }

    @Test
    public void encode() {
        assertThat(new Revision(0, 0).toString()).isEqualTo("0000000000000-0000000000000");
        assertThat(new Revision(1, 2).toString()).isEqualTo("0000000000001-0000000000002");
        assertThat(new Revision(3, 4).toString()).isEqualTo("0000000000003-0000000000004");
        assertThat(new Revision(5, 6).toString()).isEqualTo("0000000000005-0000000000006");
        assertThat(new Revision(7, 8).toString()).isEqualTo("0000000000007-0000000000008");
        assertThat(new Revision(9, 10).toString()).isEqualTo("0000000000009-000000000000A");
        assertThat(new Revision(11, 12).toString()).isEqualTo("000000000000B-000000000000C");
        assertThat(new Revision(13, 14).toString()).isEqualTo("000000000000D-000000000000E");
        assertThat(new Revision(15, 16).toString()).isEqualTo("000000000000F-000000000000G");
        assertThat(new Revision(17, 18).toString()).isEqualTo("000000000000H-000000000000J");
        assertThat(new Revision(19, 20).toString()).isEqualTo("000000000000K-000000000000M");
        assertThat(new Revision(21, 22).toString()).isEqualTo("000000000000N-000000000000P");
        assertThat(new Revision(23, 24).toString()).isEqualTo("000000000000Q-000000000000R");
        assertThat(new Revision(25, 26).toString()).isEqualTo("000000000000S-000000000000T");
        assertThat(new Revision(27, 28).toString()).isEqualTo("000000000000V-000000000000W");
        assertThat(new Revision(29, 30).toString()).isEqualTo("000000000000X-000000000000Y");
        assertThat(new Revision(31, 32).toString()).isEqualTo("000000000000Z-0000000000010");
        assertThat(new Revision(33, 34).toString()).isEqualTo("0000000000011-0000000000012");
        assertThat(new Revision(-1, -1).toString()).isEqualTo("FZZZZZZZZZZZZ-FZZZZZZZZZZZZ");
    }

    private void assertDecode(String str, long a, long b) {
        Revision revision = new Revision(str);
        assertThat(revision.timeSeq).isEqualTo(a);
        assertThat(revision.node).isEqualTo(b);
    }
}
