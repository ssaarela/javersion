package org.javersion.util;

import static java.lang.String.format;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.fill;

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
    public static final BinaryEncoder BASE32_CD;

    /**
     * Number encoder using Crockford's alphabet.
     */
    public static final BinaryEncoder BASE32_CD_NUMBER;

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
        BASE32_CD = builder.buildBaseEncoder();
        BASE32_CD_NUMBER = builder.buildUnsignedNumberEncoder();

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

    private static final String[] EMPTY_ARGS = new String[0];

    private static final int BYTE_MASK = 255;

    protected final int encodingBitLen;

    private final byte mask;

    private final char[] numberToChar;

    private final int[] charToNumber;

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

    public String encode(Bytes bytes) {
        int byteLen = bytes.length();
        int charLen = charLen(byteLen);

        int bitIndex = getFirstBitIndex(byteLen);
        int charIndex = getFirstCharIndex(charLen);

        char[] chars = new char[charLen];
        while (charIndex >= 0 && charIndex < charLen) {
            int num = bytes.getNumber(bitIndex, encodingBitLen);
            chars[charIndex] = numberToChar[num];
            bitIndex = getNextBitIndex(bitIndex);
            charIndex = getNextCharIndex(charIndex);
        }
        return new String(chars);
    }

    public byte[] decode(String str) {
        int charLen = str.length();
        int bitLen = charLen * encodingBitLen;
        int byteLen = bitLen / 8;

        int bitIndex = getFirstBitIndex(byteLen);
        int charIndex = getFirstCharIndex(charLen);

        byte[] bytes = new byte[byteLen];
        while (charIndex >= 0 && charIndex < charLen) {
            int charToNumberIndex = str.charAt(charIndex);
            checkWithinCharToNumberRange(str, charIndex, charToNumberIndex);

            int number = charToNumber[charToNumberIndex];
            checkNumber(str, charIndex, number);

            setNumber(number, bytes, bitIndex);
            bitIndex = getNextBitIndex(bitIndex);
            charIndex = getNextCharIndex(charIndex);
        }
        return bytes;
    }

    private void checkNumber(String str, int charIndex, int number) {
        if (number < 0) {
            throwIllegalCharacterException(str, charIndex);
        }
    }

    private void checkWithinCharToNumberRange(String str, int charIndex, int charToNumberIndex) {
        if (charToNumberIndex >= charToNumber.length) {
            throwIllegalCharacterException(str, charIndex);
        }
    }

    private void throwIllegalCharacterException(String str, int index) {
        throw new IllegalArgumentException(format("Illegal character %s at %s", str.charAt(index), index));
    }

    public String encodeLong(long l) {
        return encode(new Bytes.Long(l));
    }

    public String encodeInt(int i) {
        return encode(new Bytes.Integer(i));
    }

    public long decodeLong(String str) {
        byte[] bytes = decode(str);
        Check.that(bytes.length == 8, "Expected 8 bytes", EMPTY_ARGS);
        return append(bytes, 8, 0, 0);
    }

    public int decodeInt(String str) {
        byte[] bytes = decode(str);
        Check.that(bytes.length == 4, "Expected 4 bytes", EMPTY_ARGS);
        return (int) append(bytes, 4, 0, 0);
    }

    private int charLen(int byteLen) {
        int bitLen = byteLen * 8;
        int charLen = bitLen / encodingBitLen;
        if (bitLen % encodingBitLen != 0) {
            return charLen + 1;
        }
        return charLen;
    }

    private void setNumber(final int number, final byte[] bytes, final int index) {
        int loByte = index / 8;
        int toBit = index + encodingBitLen;
        int hiByte = (toBit - 1) / 8;
        int loShift = toBit % 8;
        int hiShift = (loShift == 0 ? 0 : 8 - loShift);

        if (hiByte < bytes.length) {
            bytes[hiByte] |= number << hiShift;
        }
        if (hiByte != loByte && index >= 0) {
            bytes[loByte] |= number >>> loShift;
        }
    }

    abstract int getFirstBitIndex(int byteLen);

    abstract int getFirstCharIndex(int charLen);

    abstract int getNextBitIndex(int currentBitIndex);

    abstract int getNextCharIndex(int currentCharIndex);


    static byte[] append(long val, int byteCount, int byOffset, byte[] intoBytes) {
        int byteOffset = 8 - byteCount;
        for (int i = (byOffset + byteCount - 1); i >= byOffset; i--) {
            intoBytes[i] = getByte(val, (i - byOffset + byteOffset));
        }
        return intoBytes;
    }

    static long append(byte[] bytes, int byteCount, int byOffset, long intoVal) {
        int byteOffset = 8 - byteCount;
        for (int i= byOffset + byteCount - 1; i >= byOffset; i--) {
            intoVal = setByte((bytes[i] & BYTE_MASK), (i - byOffset + byteOffset), intoVal);
        }
        return intoVal;
    }

    static long setByte(long b, int index, long into) {
        return into | (index == 7 ? b : ((b << (56 - 8 * index))));
    }

    static byte getByte(long l, int index) {
        return (byte) ((index == 7 ? l : ((l >>> (56 - 8 * index)))) & BYTE_MASK);
    }

    private static class NumberEncoder extends BinaryEncoder {

        public NumberEncoder(char[] numberToChar, int[] charToNumber) {
            super(numberToChar, charToNumber);
            for (int i = 1; i < numberToChar.length; i++) {
                Check.that(numberToChar[i-1] < numberToChar[i],
                        "Expected alphabet to be in lexical order! Got %s before %s", numberToChar[i-1], numberToChar[i]);
            }
        }

        @Override
        int getFirstBitIndex(int byteLen) {
            return (byteLen * 8) - encodingBitLen;
        }

        @Override
        int getFirstCharIndex(int charLen) {
            return charLen - 1;
        }

        @Override
        int getNextBitIndex(int currentBitIndex) {
            return currentBitIndex - encodingBitLen;
        }

        @Override
        int getNextCharIndex(int currentCharIndex) {
            return currentCharIndex - 1;
        }
    }

    private static class SignedNumberEncoder extends  NumberEncoder {

        public SignedNumberEncoder(char[] numberToChar, int[] charToNumber) {
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

    private static class BaseEncoder extends BinaryEncoder {

        public BaseEncoder(char[] numberToChar, int[] charToNumber) {
            super(numberToChar, charToNumber);
        }

        @Override
        int getFirstBitIndex(int byteLen) {
            return 0;
        }

        @Override
        int getFirstCharIndex(int charLen) {
            return 0;
        }

        @Override
        int getNextBitIndex(int currentBitIndex) {
            return currentBitIndex + encodingBitLen;
        }

        @Override
        int getNextCharIndex(int currentCharIndex) {
            return currentCharIndex + 1;
        }
    }

}
