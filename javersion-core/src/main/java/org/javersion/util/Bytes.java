package org.javersion.util;

public interface Bytes {

    public static final int BYTE_MASK = 255;

    public static class Array implements Bytes {

        private final byte[] bytes;

        public Array(byte[] bytes) {
            this.bytes = bytes;
        }

        public int getNumber(int index, int encodingBitLen) {
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
            this.l = i1 << 32 | i2;
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
