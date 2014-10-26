package org.javersion.util;

public interface Bytes {

    public static final int BYTE_MASK = 255;

    public static class Array implements Bytes {

        private final byte[] bytes;

        public Array(byte[] bytes) {
            this.bytes = bytes;
        }

        public int getNumber(int index, int encodingBitLen) {
            int hiByte = (index + encodingBitLen - 1) / 8;
            int shift = (index + encodingBitLen) % 8;
            int number;
            if (shift == 0) {
                number = bytes[hiByte];
            } else {
                if (hiByte < bytes.length) {
                    number = (bytes[hiByte] & BYTE_MASK) >>> (8 - shift);
                } else {
                    number = 0;
                }
                if (shift < encodingBitLen && hiByte > 0) {
                    number |= bytes[hiByte - 1] << shift;
                }
            }
            return number & ((1 << encodingBitLen) - 1);
        }

        @Override
        public int length() {
            return bytes.length;
        }

    }

    public static class Integer implements Bytes {

        private final int i;

        public Integer(int i) {
            this.i = i;
        }

        @Override
        public int getNumber(int index, int encodingBitLen) {
            int number = i;
            int shift = 32 - encodingBitLen - index;
            if (shift > 0) {
                number >>>= shift;
            } else if (shift < 0) {
                number <<= -shift;
            }
            return number & ((1 << encodingBitLen) - 1);
        }

        @Override
        public int length() {
            return 4;
        }
    }

    public static class Long implements Bytes {

        private final long l;

        public Long(long l) {
            this.l = l;
        }

        public Long(int i1, int i2) {
            l = (((long) i1) << 32) | i2;
        }

        @Override
        public int getNumber(int index, int encodingBitLen) {
            long number = l;
            int shift = 64 - encodingBitLen - index;
            if (shift > 0) {
                number >>>= shift;
            } else if (shift < 0) {
                number <<= -shift;
            }
            return (int) (number & ((1 << encodingBitLen) - 1));
        }

        @Override
        public int length() {
            return 8;
        }
    }

    public int getNumber(int index, int encodingBitLen);

    public int length();
}
