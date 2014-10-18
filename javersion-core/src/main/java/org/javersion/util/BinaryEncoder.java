package org.javersion.util;

import static java.util.Arrays.copyOfRange;

import java.util.Arrays;

public abstract class BinaryEncoder {

    /**
     * No-padding Base32 encoder.
     */
    public static final BinaryEncoder BASE32;

    public static final BinaryEncoder BASE32_NUMBER;

    /**
     * Douglas Crockford's Base32 alternative.
     */
    public static final BinaryEncoder BASE32_CD;

    /**
     * Number encoder using Crockford's alphabet.
     */
    public static final BinaryEncoder BASE32_CD_NUMBER;

    static {
        Builder builder;

        builder = new  Builder("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567")
                  .withAliases("abcdefghijklmnopqrstuvwxyz");
        BASE32 = builder.buildBaseEncoder();
        BASE32_NUMBER = builder.buildNumberEncoder();

        builder = new  Builder("0123456789ABCDEFGHJKMNPQRSTVWXYZ")
                  .withAliases("          abcdefghjkmnpqrstvwxyz")
                 .withAliasesFor('0', "oO")
                 .withAliasesFor('1', "iIlL");
        BASE32_CD = builder.buildBaseEncoder();
        BASE32_CD_NUMBER = builder.buildNumberEncoder();
    }

    public static final class Builder {

        private int maxChar = -1;

        private int minChar = Integer.MAX_VALUE;

        private final char[] numberToChar;

        private int[] charToNumber;

        public Builder(String chars) {
            this(chars.toCharArray());
        }

        public Builder(char... chars) {
            numberToChar = Arrays.copyOf(chars, chars.length);
            minMax(chars);
            charToNumber = new int[maxChar + 1];
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
            minMax(aliases);
            ensureCharToNumberSize();
            for (char alias : aliases) {
                verify(alias);
                charToNumber[alias] = alias;
            }
            return this;
        }

        private void ensureCharToNumberSize() {
            if (charToNumber.length <= maxChar) {
                charToNumber = Arrays.copyOf(charToNumber, maxChar + 1);
            }
        }

        public Builder withAliases(String aliases) {
            Check.that(aliases.length() <= numberToChar.length,
                    "Expected positional aliases length to be same or less as main chars. Use space to skip.");
            char[] chars = aliases.toCharArray();
            minMax(chars);
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

        public NumberEncoder buildNumberEncoder() {
            return new NumberEncoder(numberToChar, getOptimizedCharToNumber(), minChar);
        }

        public BaseEncoder buildBaseEncoder() {
            return new BaseEncoder(numberToChar, getOptimizedCharToNumber(), minChar);
        }

        private int[] getOptimizedCharToNumber() {
            return copyOfRange(charToNumber, minChar, maxChar + 1);
        }

        private void verify(char ch) {
            Check.that(charToNumber[ch] == 0, "Duplicate mapping for %s", ch);
        }
        private void minMax(char[] chars) {
            for (int i=0; i < chars.length; i++) {
                int ch = chars[i];
                if (maxChar < ch) {
                    maxChar = ch;
                }
                if (minChar > ch) {
                    minChar = ch;
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

    private final int charToNumberOffset;

    private BinaryEncoder(char[] numberToChar, int[] charToNumber, int charToNumberOffset) {
        Check.notNull(numberToChar, "toChar");
        Check.notNull(charToNumber, "charToNumber");

        this.numberToChar = numberToChar;
        this.charToNumber = charToNumber;
        this.charToNumberOffset = charToNumberOffset;

        int radix = numberToChar.length;
        Check.that(Integer.bitCount(radix) == 1, "radix should be ^2");
        Check.that(radix >= 2, "radix should be > 2");
        Check.that(radix <= 256, "radix should be <= 256");

        this.encodingBitLen = Integer.bitCount(radix-1);
        this.mask = (byte) (radix - 1);
    }

    public String encode(byte[] bytes) {
        int byteLen = bytes.length;
        int charLen = charLen(byteLen);

        int bitIndex = getFirstBitIndex(byteLen);
        int charIndex = getFirstCharIndex(charLen);

        char[] chars = new char[charLen];
        while (charIndex >= 0 && charIndex < charLen) {
            int num = getNumber(bytes, bitIndex);
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
            int number = charToNumber[str.charAt(charIndex) - charToNumberOffset];
            setNumber(number, bytes, bitIndex);
            bitIndex = getNextBitIndex(bitIndex);
            charIndex = getNextCharIndex(charIndex);
        }
        return bytes;
    }

    public String encodeLong(long l) {
        return encode(append(l, 8, 0, new byte[8]));
    }

    public String encodeInt(int i) {
        return encode(append(i, 4, 0, new byte[4]));
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
        if (byteLen % encodingBitLen != 0) {
            return charLen + 1;
        }
        return charLen;
    }

    private int getNumber(final byte[] bytes, final int index) {
        int loByte = index / 8;
        int toBit = index + encodingBitLen;
        int hiByte = (toBit - 1) / 8;
        int loShift = toBit % 8;
        int hiShift = (loShift == 0 ? 0 : 8 - loShift);
        int number = 0;

        if (hiByte < bytes.length) {
            // NOTE >>> doesn't work with bytes
            number = bytes[hiByte] & BYTE_MASK;
            number >>>= hiShift;
        }
        if (hiByte != loByte && index >= 0) {
            number |= (bytes[loByte] << loShift);
        }
        return number & mask;
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

    private static final class NumberEncoder extends BinaryEncoder {

        public NumberEncoder(char[] numberToChar, int[] charToNumber, int numberToCharOffset) {
            super(numberToChar, charToNumber, numberToCharOffset);
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

    private static final class BaseEncoder extends BinaryEncoder {

        public BaseEncoder(char[] numberToChar, int[] charToNumber, int numberToCharOffset) {
            super(numberToChar, charToNumber, numberToCharOffset);
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
