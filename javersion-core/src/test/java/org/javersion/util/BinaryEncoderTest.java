package org.javersion.util;

import static com.google.common.base.Charsets.UTF_8;
import static java.lang.Long.parseUnsignedLong;
import static java.lang.String.format;
import static java.lang.System.nanoTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.javersion.util.BinaryEncoder.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Base64;

import org.junit.Test;

public class BinaryEncoderTest {

    private static final String TEXT =
            "Base32 is a notation for encoding arbitrary byte data using a restricted set of symbols which can be conveniently " +
            "used by humans and processed by old computer systems which only recognize restricted character sets.";

    private static final byte[] TEXT_BYTES = TEXT.getBytes(UTF_8);

    private static final Builder BUILDER = new Builder("01234567");

    private static final BinaryEncoder NUMBER8 = BUILDER.buildUnsignedNumberEncoder();

    private static final BinaryEncoder SIGNED_NUMBER8 = BUILDER.buildSignedNumberEncoder();

    private static final BinaryEncoder BASE8 = BUILDER.buildBaseEncoder();

    @Test
    public void short_one() {
        assertThat(NUMBER8.encode(twoBytes("0-000-000-0  00-000-001"))).isEqualTo("000001");
        assertThat(  BASE8.encode(twoBytes("000-000-00  0-000-000-1"))).isEqualTo("000004");

        byte[] bytes = NUMBER8.decode("000001");
        assertThat(bytes).isEqualTo(twoBytes("00000000 00000001"));
        assertThat(BASE8.decode("000004")).isEqualTo(bytes);
    }

    @Test
    public void all_ones_byte() {
        assertThat(NUMBER8.encode(oneByte("11-111-111"))).isEqualTo("377");
        assertThat(  BASE8.encode(oneByte("111-111-11"))).isEqualTo("776");

        byte[] bytes = NUMBER8.decode("377");
        assertThat(bytes).isEqualTo(oneByte("11111111"));
        assertThat(BASE8.decode("776")).isEqualTo(bytes);
    }

    @Test
    public void two_byte_alternate_ones() {
        assertThat(NUMBER8.encode(twoBytes("1-010-101-0  10-101-010"))).isEqualTo("125252");
        assertThat(  BASE8.encode(twoBytes("101-010-10  1-010-101-0"))).isEqualTo("525250");

        byte[] bytes = NUMBER8.decode("125252");
        assertThat(bytes).isEqualTo(twoBytes("10101010 10101010"));
        assertThat(BASE8.decode("525250")).isEqualTo(bytes);
    }

    @Test
    public void two_byte_alternate_zeros() {
        assertThat(NUMBER8.encode(twoBytes("0-101-010-1  01-010-101"))).isEqualTo("052525");
        assertThat(  BASE8.encode(twoBytes("010-101-01  0-101-010-1"))).isEqualTo("252524");

        byte[] bytes = NUMBER8.decode("052525");
        assertThat(bytes).isEqualTo(twoBytes("01010101 01010101"));
        assertThat(BASE8.decode("252524")).isEqualTo(bytes);
    }

    @Test
    public void one_zeros_one() {
        assertThat(NUMBER8.encode(fourBytes("10-000-000  000-000-00  0-000-000-0  00-000-001")))
                .isEqualTo("20000000001");
        assertThat(  BASE8.encode(fourBytes("100-000-00  0-000-000-0  00-000-000  000-000-01")))
                .isEqualTo("40000000002");

        byte[] bytes = NUMBER8.decode("20000000001");
        assertThat(bytes).isEqualTo(fourBytes("10000000 00000000 00000000 00000001"));
        assertThat(BASE8.decode("40000000002")).isEqualTo(bytes);
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
            current = NUMBER8.encodeLong(val);
            assertOrder(prev, current, i);
            assertThat(NUMBER8.decodeLong(current)).isEqualTo(val);
            assertThat(BASE8.decodeLong(BASE8.encodeLong(val))).isEqualTo(val);
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
            current = NUMBER8.encodeLong(val);
            assertOrder(prev, current, i);
            assertThat(NUMBER8.decodeLong(current)).isEqualTo(val);
            assertThat(BASE8.decodeInt(BASE8.encodeInt(val))).isEqualTo(val);
        }
        assertOrder(prev, current, 64 - 8);
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
        final String base32 = BASE32.encode(TEXT_BYTES);

        // Checker generated with http://online-calculators.appspot.com/base32/ (padding removed)
        assertThat(base32).isEqualTo("IJQXGZJTGIQGS4ZAMEQG433UMF2GS33OEBTG64RAMVXGG33ENFXGOIDBOJRGS5DSMFZHSIDCPF2GKIDEMF2GCIDVONUW4ZZAMEQHEZLTORZGSY3UMVSCA43" +
                "FOQQG6ZRAON4W2YTPNRZSA53INFRWQIDDMFXCAYTFEBRW63TWMVXGSZLOORWHSIDVONSWIIDCPEQGQ5LNMFXHGIDBNZSCA4DSN5RWK43TMVSCAYTZEBXWYZBAMNXW24DVORSXEIDTPFZ" +
                "XIZLNOMQHO2DJMNUCA33ONR4SA4TFMNXWO3TJPJSSA4TFON2HE2LDORSWIIDDNBQXEYLDORSXEIDTMV2HGLQ");

        final byte[] bytes = BASE32.decode(base32.toLowerCase());
        assertThat(new String(bytes, UTF_8)).isEqualTo(TEXT);
    }

    @Test
    public void performance() {
        final int rounds = 100000;
        long start, time;

        runBase64(rounds);
        start = nanoTime();
        runBase64(rounds);
        time = nanoTime() - start;
        System.out.println("Encode/decode bytes, nanos per round: " + (time/rounds));
        // 5782 - 6000 - 6180

        runLongBase32(rounds);
        start = nanoTime();
        runLongBase32(rounds);
        time = nanoTime() - start;
        System.out.println("Encode/decode long, nanos per round: " + (time/rounds));
        // 350 - 375 - 405

        runIntBase32(rounds);
        start = nanoTime();
        runIntBase32(rounds);
        time = nanoTime() - start;
        System.out.println("Encode/decode int, nanos per round: " + (time/rounds));
        // 252 - 265 - 291
    }

    private void runBase64(int rounds) {
        for (int i=0; i < rounds; i++) {
            BASE64.decode(BASE64.encode(TEXT_BYTES));
            NUMBER_BASE64_URL.decode(NUMBER_BASE64_URL.encode(TEXT_BYTES));
        }
    }

    private void runLongBase32(int rounds) {
        for (int i=0; i < rounds; i++) {
            long val = 123 * (456 + i);
            BASE64.decodeLong(BASE64.encodeLong(val));
            NUMBER_BASE64_URL.decodeLong(NUMBER_BASE64_URL.encodeLong(val));
        }
    }

    private void runIntBase32(int rounds) {
        for (int i=0; i < rounds; i++) {
            int val = 123 * (456 + i);
            BASE64.decodeInt(BASE64.encodeInt(val));
            NUMBER_BASE64_URL.decodeInt(NUMBER_BASE64_URL.encodeInt(val));
        }
    }

    @Test
    public void base64_no_padding() {
        final String base64 = BASE64.encode(TEXT_BYTES);
        final String javaBase64 = Base64.getEncoder().withoutPadding().encodeToString(TEXT_BYTES);
        assertThat(base64).isEqualTo(javaBase64);
    }

    @Test
    public void base64_number() {
        assertThat(NUMBER_BASE64_URL.encodeLong(-1)).isEqualTo("Ezzzzzzzzzz");
        assertThat(NUMBER_BASE64_URL.decodeLong("Ezzzzzzzzzz")).isEqualTo(-1);
    }

    @Test
    public void unsinged_comparison() {
        assertOrder(NUMBER8, 0, 1);
        assertOrder(NUMBER8, 1, Long.MAX_VALUE);
        assertOrder(NUMBER8, Long.MAX_VALUE, Long.MIN_VALUE);
        assertOrder(NUMBER8, Long.MIN_VALUE, -1);
    }

    @Test
    public void singed_comparison() {
        assertOrder(SIGNED_NUMBER8, Long.MIN_VALUE, -1);
        assertOrder(SIGNED_NUMBER8, -1, 0);
        assertOrder(SIGNED_NUMBER8, 0, 1);
        assertOrder(SIGNED_NUMBER8, 1, Long.MAX_VALUE);
    }

    @Test
    public void get_number() {
        int val = parseInt("11101111 00011111 00100011 01000101");
        assertThat(BASE8.getNumber(val, 0)).isEqualTo(7);
        assertThat(BASE8.getNumber(val, 8)).isEqualTo(0);
        assertThat(BASE8.getNumber(val, 16)).isEqualTo(1);
        assertThat(BASE8.getNumber(val, 24)).isEqualTo(2);
        assertThat(BASE8.getNumber(val, 29)).isEqualTo(5);
    }

    @Test(expected = AssertionError.class)
    public void get_number_underflow() {
        int val = parseInt("11101111 00011111 00100011 01000101");
        BASE8.getNumber(val, -1);
    }

    @Test(expected = AssertionError.class)
    public void get_number_overflow() {
        int val = parseInt("11101111 00011111 00100011 01000101");
        BASE8.getNumber(val, 30);
    }

    private static int parseInt(String bits) {
        bits = bits.replaceAll("[^\\-01]", "");
        return Integer.parseUnsignedInt(bits, 2);
    }

    private void assertOrder(BinaryEncoder encoder, long first, long second) {
        assertOrder(encoder.encodeLong(first), encoder.encodeLong(second));
    }

    private void assertOrder(String first, String second) {
        assertThat(first.compareTo(second)).isLessThan(0);
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
