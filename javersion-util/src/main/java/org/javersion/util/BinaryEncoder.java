package org.javersion.util;

import static java.lang.String.format;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.fill;

/**
 * Configurable binary encoder:
 * <ul>
 *     <li>Configurable charset of length n^2.</li>
 *     <li>Configurable aliases (e.g. 1 l i for I).</li>
 *     <li>Configurable direction: leftover zeros first as in binary format of numbers or at the end as in base encoders.
 *         Encoding numbers in reverse retains the numeric order.</li>
 *     <li>Configurable signed/unsigned encoding for numeric encoders.</li>
 *     <li>Fluent builder for defining encoders.</li>
 * </ul>
 * Reverse encoding is especially useful for text-based indexing.
 *
 * DISCLAIMER:
 * <ul>
 *     <li>Flexibility comes with a price: any specialized encoder will be faster (e.g.
 *     Java's Base64 is roughly twice as fast).</li>
 *     <li>Numbers are encoded by bytes, so the length of the result is constant for the type of number.</li>
 *     <li>Splitting result into lines of some max length is not supported.</li>
 *     <li>Padding is not supported.</li>
 * </ul>
 */
public abstract class BinaryEncoder {

    public static final BinaryEncoder HEX;

    public static final BinaryEncoder HEX_ALIASED;

    /**
     * No-padding Base32 encoder.
     */
    public static final BinaryEncoder BASE32;

    /**
     * Douglas Crockford's Base32 alternative. Result is comparable as
     * alphabet is in lexical order.
     */
    public static final BinaryEncoder BASE32_CROCKFORD;

    /**
     * Number encoder using Crockford's alphabet.
     */
    public static final BinaryEncoder BASE32_CROCKFORD_NUMBER;

    /**
     * No-padding Base64 encoder.
     */
    public static final BinaryEncoder BASE64;

    public static final BinaryEncoder BASE64_URL;

    public static final BinaryEncoder NUMBER_BASE64_URL;


    static {
        Builder builder;

        builder = new  Builder("0123456789ABCDEF")
                  .withAliases("          abcdef");
        HEX = builder.buildUnsignedNumberEncoder();

        builder.withAliasesFor('0', "oO")
               .withAliasesFor('1', "iIl");
        HEX_ALIASED = builder.buildUnsignedNumberEncoder();

        builder = new  Builder("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567")
                  .withAliases("abcdefghijklmnopqrstuvwxyz");
        BASE32 = builder.buildBaseEncoder();

        builder = new  Builder("0123456789ABCDEFGHJKMNPQRSTVWXYZ")
                  .withAliases("          abcdefghjkmnpqrstvwxyz")
                  .withAliasesFor('0', "oO")
                  .withAliasesFor('1', "iIlL");
        BASE32_CROCKFORD = builder.buildBaseEncoder();
        BASE32_CROCKFORD_NUMBER = builder.buildUnsignedNumberEncoder();

        builder = new Builder("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/");
        BASE64 = builder.buildBaseEncoder();

        builder = new Builder("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_");
        BASE64_URL = builder.buildBaseEncoder();

        builder = new Builder("-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz");
        NUMBER_BASE64_URL = builder.buildUnsignedNumberEncoder();
    }

    public static final class Builder {

        private int maxChar = -1;

        private final char[] numberToChar;

        private int[] charToNumber;

        public Builder(String chars) {
            this(chars.toCharArray());
        }

        public Builder(char... chars) {
            numberToChar = copyOf(chars, chars.length);
            setMaxChar(chars);
            charToNumber = new int[maxChar + 1];
            fill(charToNumber, -1);
            for (int i=0; i < chars.length; i++) {
                char ch = chars[i];
                verify(ch);
                numberToChar[i] = ch;
                charToNumber[ch] = i;
            }
        }

        public Builder withAliasesFor(char ch, String aliases) {
            return withAliasesFor(ch, aliases.toCharArray());
        }

        public Builder withAliasesFor(char ch, char... aliases) {
            int number = charToNumber[ch];
            setMaxChar(aliases);
            ensureCharToNumberSize();
            for (char alias : aliases) {
                verify(alias);
                charToNumber[alias] = alias;
            }
            return this;
        }

        private void ensureCharToNumberSize() {
            int oldSize = charToNumber.length;
            if (oldSize <= maxChar) {
                charToNumber = copyOf(charToNumber, maxChar + 1);
            }
            fill(charToNumber, oldSize, charToNumber.length, -1);
        }

        public Builder withAliases(String aliases) {
            Check.that(aliases.length() <= numberToChar.length,
                    "Expected positional aliases length to be same or less as main chars. Use space to skip.");
            char[] chars = aliases.toCharArray();
            setMaxChar(chars);
            ensureCharToNumberSize();
            for (int i=0; i < chars.length; i++) {
                char alias = chars[i];
                if (alias != ' ') {
                    verify(alias);
                    charToNumber[alias] = i;
                }
            }
            return this;
        }

        public NumberEncoder buildUnsignedNumberEncoder() {
            return new NumberEncoder(numberToChar, charToNumber);
        }

        public NumberEncoder buildSignedNumberEncoder() {
            return new SignedNumberEncoder(numberToChar, charToNumber);
        }

        public BaseEncoder buildBaseEncoder() {
            return new BaseEncoder(numberToChar, charToNumber);
        }

        private void verify(char ch) {
            Check.that(charToNumber[ch] == -1, "Duplicate mapping for %s", ch);
        }
        private void setMaxChar(char[] chars) {
            for (int i=0; i < chars.length; i++) {
                int ch = chars[i];
                if (maxChar < ch) {
                    maxChar = ch;
                }
            }
        }
    }


    protected final int encodingBitLen;

    final byte mask;

    final char[] numberToChar;

    final int[] charToNumber;

    private BinaryEncoder(char[] numberToChar, int[] charToNumber) {
        Check.notNull(numberToChar, "toChar");
        Check.notNull(charToNumber, "charToNumber");

        this.numberToChar = copyOf(numberToChar, numberToChar.length);
        this.charToNumber = copyOf(charToNumber, charToNumber.length);

        int radix = numberToChar.length;
        Check.that(Integer.bitCount(radix) == 1, "radix should be ^2");
        Check.that(radix >= 2, "radix should be > 2");
        Check.that(radix <= 256, "radix should be <= 256");

        this.encodingBitLen = Integer.bitCount(radix-1);
        this.mask = (byte) (radix - 1);
    }

    public String encode(byte[] bytes) {
        return encode(new Bytes.Array(bytes));
    }

    public byte[] decode(String str) {
        return decode(str, new Bytes.Array(str.length() * encodingBitLen / 8)).getBytes();
    }

    public String encodeLong(long l) {
        return encode(new Bytes.Long(l));
    }

    public String encodeInt(int i) {
        return encode(new Bytes.Integer(i));
    }

    public long decodeLong(String str) {
        return decode(str, new Bytes.Long(0)).getLong();
    }

    public int decodeInt(String str) {
        return decode(str, new Bytes.Integer(0)).getInt();
    }

    abstract String encode(Bytes bytes);

    abstract <T extends Bytes> T decode(String str, T bytes);

    void throwIllegalCharacterException(String str, int index) {
        throw new IllegalArgumentException(format("Illegal character %s at %s", str.charAt(index), index));
    }

    int charLen(int byteLen) {
        // ceil
        return 1 + ((byteLen * 8 - 1) / encodingBitLen);
    }


    public static class NumberEncoder extends BinaryEncoder {

        private NumberEncoder(char[] numberToChar, int[] charToNumber) {
            super(numberToChar, charToNumber);
            for (int i = 1; i < numberToChar.length; i++) {
                Check.that(numberToChar[i-1] < numberToChar[i],
                        "Expected alphabet to be in lexical order! Got %s before %s", numberToChar[i-1], numberToChar[i]);
            }
        }

        @Override
        String encode(Bytes bytes) {
            int charLen = charLen(bytes.length());

            char[] chars = new char[charLen];
            for (int bitIndex = (bytes.length() * 8) - encodingBitLen, charIndex = charLen - 1;
                 charIndex >= 0;
                 bitIndex -= encodingBitLen, charIndex--) {
                int num = bytes.getNumber(bitIndex, encodingBitLen);

                chars[charIndex] = numberToChar[num];
            }
            return new String(chars);
        }

        @Override
        <T extends Bytes> T decode(String str, T bytes) {
            int charLen = str.length();

            for (int bitIndex = (bytes.length() * 8) - encodingBitLen, charIndex = charLen - 1;
                 charIndex >= 0;
                 bitIndex -= encodingBitLen, charIndex--) {
                int charToNumberIndex = str.charAt(charIndex);
                if (charToNumberIndex >= charToNumber.length) {
                    throwIllegalCharacterException(str, charIndex);
                }

                int number = charToNumber[charToNumberIndex];
                if (number < 0) {
                    throwIllegalCharacterException(str, charIndex);
                }

                bytes.setNumber(number, bitIndex, encodingBitLen);
            }
            return bytes;
        }
    }

    public static class SignedNumberEncoder extends  NumberEncoder {

        private SignedNumberEncoder(char[] numberToChar, int[] charToNumber) {
            super(numberToChar, charToNumber);
        }

        @Override
        public String encodeLong(long l) {
            return super.encodeLong(l - Long.MIN_VALUE);
        }

        @Override
        public String encodeInt(int i) {
            return super.encodeInt(i - Integer.MIN_VALUE);
        }

        @Override
        public long decodeLong(String str) {
            return super.decodeLong(str) + Long.MIN_VALUE;
        }

        @Override
        public int decodeInt(String str) {
            return super.decodeInt(str) + Integer.MIN_VALUE;
        }
    }

    public static class BaseEncoder extends BinaryEncoder {

        private BaseEncoder(char[] numberToChar, int[] charToNumber) {
            super(numberToChar, charToNumber);
        }

        @Override
        String encode(Bytes bytes) {
            int charLen = charLen(bytes.length());

            char[] chars = new char[charLen];
            for (int bitIndex = 0, charIndex = 0;
                    charIndex < charLen;
                    bitIndex += encodingBitLen, charIndex++) {
                int num = bytes.getNumber(bitIndex, encodingBitLen);

                chars[charIndex] = numberToChar[num];
            }
            return new String(chars);
        }

        @Override
        <T extends Bytes> T decode(String str, T bytes) {
            int charLen = str.length();

            for (int bitIndex = 0, charIndex = 0;
                 charIndex < charLen;
                 bitIndex += encodingBitLen, charIndex++) {
                int charToNumberIndex = str.charAt(charIndex);
                if (charToNumberIndex >= charToNumber.length) {
                    throwIllegalCharacterException(str, charIndex);
                }

                int number = charToNumber[charToNumberIndex];
                if (number < 0) {
                    throwIllegalCharacterException(str, charIndex);
                }

                bytes.setNumber(number, bitIndex, encodingBitLen);
            }
            return bytes;
        }
    }

}
