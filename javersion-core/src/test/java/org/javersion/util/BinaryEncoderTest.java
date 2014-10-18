package org.javersion.util;

import static com.google.common.base.Charsets.UTF_8;
import static java.lang.Long.parseUnsignedLong;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.javersion.util.BinaryEncoder.BASE32;
import static org.javersion.util.BinaryEncoder.Builder;
import static org.javersion.util.BinaryEncoder.getByte;
import static org.javersion.util.BinaryEncoder.setByte;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.junit.Test;

public class BinaryEncoderTest {

    private static final Builder BASE8 = new Builder("01234567");

    private static final BinaryEncoder number8 = BASE8.buildNumberEncoder();

    private static final BinaryEncoder base8 = BASE8.buildBaseEncoder();

    @Test
    public void short_one() {
        assertThat(number8.encode(twoBytes("0-000-000-0  00-000-001"))).isEqualTo("000001");
        assertThat(  base8.encode(twoBytes("000-000-00  0-000-000-1"))).isEqualTo("000004");

        byte[] bytes = number8.decode("000001");
        assertThat(bytes).isEqualTo(twoBytes("00000000 00000001"));
        assertThat(base8.decode("000004")).isEqualTo(bytes);
    }

    @Test
    public void all_ones_byte() {
        assertThat(number8.encode(oneByte("11-111-111"))).isEqualTo("377");
        assertThat(  base8.encode(oneByte("111-111-11"))).isEqualTo("776");

        byte[] bytes = number8.decode("377");
        assertThat(bytes).isEqualTo(oneByte("11111111"));
        assertThat(base8.decode("776")).isEqualTo(bytes);
    }

    @Test
    public void two_byte_alternate_ones() {
        assertThat(number8.encode(twoBytes("1-010-101-0  10-101-010"))).isEqualTo("125252");
        assertThat(  base8.encode(twoBytes("101-010-10  1-010-101-0"))).isEqualTo("525250");

        byte[] bytes = number8.decode("125252");
        assertThat(bytes).isEqualTo(twoBytes("10101010 10101010"));
        assertThat(base8.decode("525250")).isEqualTo(bytes);
    }

    @Test
    public void two_byte_alternate_zeros() {
        assertThat(number8.encode(twoBytes("0-101-010-1  01-010-101"))).isEqualTo("052525");
        assertThat(  base8.encode(twoBytes("010-101-01  0-101-010-1"))).isEqualTo("252524");

        byte[] bytes = number8.decode("052525");
        assertThat(bytes).isEqualTo(twoBytes("01010101 01010101"));
        assertThat(base8.decode("252524")).isEqualTo(bytes);
    }

    @Test
    public void one_zeros_one() {
        assertThat(number8.encode(fourBytes("10-000-000  000-000-00  0-000-000-0  00-000-001")))
                .isEqualTo("20000000001");
        assertThat(  base8.encode(fourBytes("100-000-00  0-000-000-0  00-000-000  000-000-01")))
                .isEqualTo("40000000002");

        byte[] bytes = number8.decode("20000000001");
        assertThat(bytes).isEqualTo(fourBytes("10000000 00000000 00000000 00000001"));
        assertThat(base8.decode("40000000002")).isEqualTo(bytes);
    }

    @Test
    public void get_byte() {
        assertThat(getByte(1, 7)).isEqualTo((byte) 1);
        assertThat(getByte(1, 0)).isEqualTo((byte) 0);
        assertThat(getByte(-1, 0)).isEqualTo((byte) -1);
        assertThat(getByte(-1, 2)).isEqualTo((byte) -1);
        assertThat(getByte(-1, 4)).isEqualTo((byte) -1);
        assertThat(getByte(-1, 6)).isEqualTo((byte) -1);
    }

    @Test
    public void set_byte() {
        assertThat(setByte(1, 7, 0)).isEqualTo(1l);
        assertThat(setByte(Byte.MIN_VALUE, 0, 0)).isEqualTo(Long.MIN_VALUE);
        assertThat(setByte(Byte.MIN_VALUE, 0, 1)).isEqualTo(Long.MIN_VALUE + 1);
    }

    @Test
    public void long_sweep() {
        String prev = null;
        String current = null;
        long step = Long.parseLong("10100110", 2);
        for (int i=0; i < 64-8; i++) {
            prev = current;
            long val = step << i;
            current = number8.encodeLong(val);
            assertOrder(prev, current, i);
            assertThat(number8.decodeLong(current)).isEqualTo(val);
            assertThat(base8.decodeLong(base8.encodeLong(val))).isEqualTo(val);
        }
        assertOrder(prev, current, 64 - 8);
    }

    @Test
    public void int_sweep() {
        String prev = null;
        String current = null;
        int step = Integer.parseInt("10100110", 2);
        for (int i=0; i < 32-8; i++) {
            prev = current;
            int val = step << i;
            current = number8.encodeLong(val);
            assertOrder(prev, current, i);
            assertThat(number8.decodeLong(current)).isEqualTo(val);
            assertThat(base8.decodeInt(base8.encodeInt(val))).isEqualTo(val);
        }
        assertOrder(prev, current, 64-8);
    }

    private void assertOrder(String prev, String current, int i) {
        if (prev != null) {
            if (prev.compareTo(current) >= 0) {
                fail(format("Round %s: expected %s to be less than %s", i+1, current, prev));
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void detect_illegal_chars_below_range() {
        BASE32.decode("A1B");
    }

    @Test(expected = IllegalArgumentException.class)
    public void detect_illegal_chars_above_range() {
        BASE32.decode("A{B");
    }

    @Test(expected = IllegalArgumentException.class)
    public void detect_illegal_chars_within_range() {
        BASE32.decode("A=B");
    }

    @Test
    public void base32_no_padding() throws UnsupportedEncodingException {
        final String text = "Base32 is a notation for encoding arbitrary byte data using a restricted set of symbols which can be conveniently used by humans " +
                "and processed by old computer systems which only recognize restricted character sets.";

        final String base32 = BASE32.encode(text.getBytes(UTF_8));
        // Verified with http://online-calculators.appspot.com/base32/

        assertThat(base32).isEqualTo("IJQXGZJTGIQGS4ZAMEQG433UMF2GS33OEBTG64RAMVXGG33ENFXGOIDBOJRGS5DSMFZHSIDCPF2GKIDEMF2GCIDVONUW4ZZAMEQHEZLTORZGSY3UMVSCA43" +
                "FOQQG6ZRAON4W2YTPNRZSA53INFRWQIDDMFXCAYTFEBRW63TWMVXGSZLOORWHSIDVONSWIIDCPEQGQ5LNMFXHGIDBNZSCA4DSN5RWK43TMVSCAYTZEBXWYZBAMNXW24DVORSXEIDTPFZ" +
                "XIZLNOMQHO2DJMNUCA33ONR4SA4TFMNXWO3TJPJSSA4TFON2HE2LDORSWIIDDNBQXEYLDORSXEIDTMV2HGLQ");

        final byte[] bytes = BASE32.decode(base32.toLowerCase());
        assertThat(new String(bytes, UTF_8)).isEqualTo(text);
    }

    private static char[] chars(char... chars) {
        return chars;
    }

    private static byte[] oneByte(String bits) {
        return new byte[] { (byte) parseLong(bits) };
    }

    private static byte[] twoBytes(String bits) {
        return ByteBuffer.allocate(2).putShort((short) parseLong(bits)).array();
    }

    private static byte[] fourBytes(String bits) {
        return ByteBuffer.allocate(4).putInt((int) parseLong(bits)).array();
    }

    private static long parseLong(String bits) {
        return parseUnsignedLong(bits.replaceAll("\\D", ""), 2);
    }

    private static int[] ints(int... ints) {
        return ints;
    }

}
