package org.javersion.util;

abstract class Bytes {

    static final int BYTE_MASK = 255;

    static class Array extends Bytes {

        private final byte[] bytes;

        Array(int length) {
            this(new byte[length]);
        }

        Array(byte[] bytes) {
            this.bytes = bytes;
        }

        int getNumber(int index, int encodingBitLen) {
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
        void setNumber(int number, int index, int encodingBitLen) {
            int hiByte = (index + encodingBitLen - 1) / 8;
            int shift = (index + encodingBitLen) % 8;
            if (shift == 0) {
                bytes[hiByte] |= number;
            } else {
                if (hiByte < bytes.length) {
                    bytes[hiByte] |= number << (8 - shift);
                }
                if (shift < encodingBitLen && hiByte > 0) {
                    bytes[hiByte - 1] |= number >>> shift;
                }
            }
        }

        @Override
        int length() {
            return bytes.length;
        }

        byte[] getBytes() {
            return bytes;
        }
    }

    static class Integer extends Bytes {

        private int i;

        Integer() {
            this(0);
        }

        Integer(int i) {
            this.i = i;
        }

        @Override
        int getNumber(int index, int encodingBitLen) {
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
        void setNumber(int number, int index, int encodingBitLen) {
            int shift = 32 - encodingBitLen - index;
            if (shift > 0) {
                i |= number << shift;
            } else {
                i |= number >>> -shift;
            }
        }

        @Override
        int length() {
            return 4;
        }

        int getInt() {
            return i;
        }
    }

    static class Long extends Bytes {

        private long l;

        Long(long l) {
            this.l = l;
        }

        Long(int i1, int i2) {
            l = (((long) i1) << 32) | i2;
        }

        @Override
        int getNumber(int index, int encodingBitLen) {
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
        void setNumber(int number, int index, int encodingBitLen) {
            int shift = 64 - encodingBitLen - index;
            if (shift > 0) {
                l |= ((long) number) << shift;
            } else {
                l |= number >>> -shift;
            }
        }

        @Override
        int length() {
            return 8;
        }

        long getLong() {
            return l;
        }
    }

    abstract int getNumber(int index, int encodingBitLen);

    abstract void setNumber(int number, int index, int encodingBitLen);

    abstract int length();
}
