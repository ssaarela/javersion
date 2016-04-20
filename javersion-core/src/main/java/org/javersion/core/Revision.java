package org.javersion.core;

import static java.lang.System.currentTimeMillis;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.concurrent.Immutable;

import com.eaio.uuid.UUIDGen;

/**
 * Globally unique unsigned 128-bit number:
 *
 * <pre>
 * revision (128-bit): timeSeq node
 * timeSeq (64-bit): currentTimeMillis sequence
 * currentTimeMillis (48-bit): System.currentTimeMillis
 * sequence (16-bit): running sequence number within same millisecond
 * node (64-bit): "The current clock and node value" from com.eaio.uuid.UUIDGen.clockSeqAndNode
 * </pre>
 *
 * String representation of a Revision uses Base32 Crockford characters and is
 * in it's canonical form (all upper case base characters) lexically comparable.
 * Creating a Revision from a String utilizes Crockford's alias characters.
 */
@Immutable
public final class Revision implements Comparable<Revision> {

    public static final Revision MIN_VALUE = new Revision(0, 0);

    public static final Revision MAX_VALUE = new Revision(-1, -1);

    public static final long NODE = UUIDGen.getClockSeqAndNode();

    public final long timeSeq;

    public final long node;

    public Revision(String rev) {
        if (rev.length() != 27) {
            throw new IllegalArgumentException("Expected string of length 27");
        }
        this.timeSeq = toLong(rev.substring(0, 13));
        this.node = toLong(rev.substring(14, 27));
    }

    public Revision() {
        this(newUniqueTime(), NODE);
    }

    public Revision(long node) {
        this(newUniqueTime(), node);
    }

    public Revision(long timeSeq, long node) {
        this.timeSeq = timeSeq;
        this.node = node;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Revision) {
            Revision other = (Revision) o;
            return this.timeSeq == other.timeSeq && this.node == other.node;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int) ((timeSeq >> 32) ^ timeSeq ^ (node >> 32) ^ node);
    }

    @Override
    public int compareTo(Revision other) {
        int compare = compareUnsigned(this.timeSeq, other.timeSeq);
        return (compare == 0 ? compareUnsigned(this.node, other.node) : compare);
    }

    @Override
    public String toString() {
        return toString(timeSeq) + "-" + toString(node);
    }

    public static int compareUnsigned(long x, long y) {
        return Long.compare(x + Long.MIN_VALUE, y + Long.MIN_VALUE);
    }

    private static final AtomicLong atomicLastTime = new AtomicLong(Long.MIN_VALUE);

    public static long newUniqueTime() {
        return newUniqueTime(currentTimeMillis());
    }

    public static long newUniqueTime(final long currentTimeMillis) {
        final long timeSeq = currentTimeMillis << 16;
        while (true) {
            long lastTime = atomicLastTime.get();
            if (lastTime < timeSeq) {
                if (atomicLastTime.compareAndSet(lastTime, timeSeq)) {
                    return timeSeq;
                }
            } else {
                if (atomicLastTime.compareAndSet(lastTime, lastTime + 1)) {
                    return lastTime + 1;
                }
            }
        }
    }

    public static long toLong(String str) {
        try {
            return toNumber(str.charAt(12))
                    | toNumber(str.charAt(11)) << 5
                    | toNumber(str.charAt(10)) << 10
                    | toNumber(str.charAt(9)) << 15
                    | toNumber(str.charAt(8)) << 20
                    | toNumber(str.charAt(7)) << 25
                    | toNumber(str.charAt(6)) << 30
                    | toNumber(str.charAt(5)) << 35
                    | toNumber(str.charAt(4)) << 40
                    | toNumber(str.charAt(3)) << 45
                    | toNumber(str.charAt(2)) << 50
                    | toNumber(str.charAt(1)) << 55
                    | toNumber(str.charAt(0)) << 60;
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Expected 13 character String, got " + str);
        }
    }

    public static String toString(long val) {
        return new String(new char[] {
                CHARS[MASK & (int) (val >>> 60)],
                CHARS[MASK & (int) (val >>> 55)],
                CHARS[MASK & (int) (val >>> 50)],
                CHARS[MASK & (int) (val >>> 45)],
                CHARS[MASK & (int) (val >>> 40)],
                CHARS[MASK & (int) (val >>> 35)],
                CHARS[MASK & (int) (val >>> 30)],
                CHARS[MASK & (int) (val >>> 25)],
                CHARS[MASK & (int) (val >>> 20)],
                CHARS[MASK & (int) (val >>> 15)],
                CHARS[MASK & (int) (val >>> 10)],
                CHARS[MASK & (int) (val >>> 5)],
                CHARS[MASK & (int) val]
        });
    }

    private static long toNumber(char ch) {
        try {
            long number = NUMBERS[ch];
            if (number < 0) {
                throw new IllegalArgumentException("Unrecognized character: " + ch);
            }
            return number;
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Unrecognized character: " + ch);
        }
    }

    private static final int MASK = 0b11111;

    private static final char[] CHARS = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    private static final int[] NUMBERS = new int['z' + 1];

    static {
        Arrays.fill(NUMBERS, -1);
        NUMBERS['0'] = NUMBERS['O'] = NUMBERS['o'] = 0;
        NUMBERS['1'] = NUMBERS['I'] = NUMBERS['i'] = NUMBERS['L'] = NUMBERS['l'] = 1;
        NUMBERS['2'] = 2;
        NUMBERS['3'] = 3;
        NUMBERS['4'] = 4;
        NUMBERS['5'] = 5;
        NUMBERS['6'] = 6;
        NUMBERS['7'] = 7;
        NUMBERS['8'] = 8;
        NUMBERS['9'] = 9;
        NUMBERS['A'] = NUMBERS['a'] = 10;
        NUMBERS['b'] = NUMBERS['B'] = 11;
        NUMBERS['C'] = NUMBERS['c'] = 12;
        NUMBERS['D'] = NUMBERS['d'] = 13;
        NUMBERS['E'] = NUMBERS['e'] = 14;
        NUMBERS['F'] = NUMBERS['f'] = 15;
        NUMBERS['G'] = NUMBERS['g'] = 16;
        NUMBERS['H'] = NUMBERS['h'] = 17;
        NUMBERS['J'] = NUMBERS['j'] = 18;
        NUMBERS['K'] = NUMBERS['k'] = 19;
        NUMBERS['M'] = NUMBERS['m'] = 20;
        NUMBERS['N'] = NUMBERS['n'] = 21;
        NUMBERS['P'] = NUMBERS['p'] = 22;
        NUMBERS['Q'] = NUMBERS['q'] = 23;
        NUMBERS['R'] = NUMBERS['r'] = 24;
        NUMBERS['S'] = NUMBERS['s'] = 25;
        NUMBERS['T'] = NUMBERS['t'] = 26;
        NUMBERS['V'] = NUMBERS['v'] = 27;
        NUMBERS['W'] = NUMBERS['w'] = 28;
        NUMBERS['X'] = NUMBERS['x'] = 29;
        NUMBERS['Y'] = NUMBERS['y'] = 30;
        NUMBERS['Z'] = NUMBERS['z'] = 31;
    }

}
