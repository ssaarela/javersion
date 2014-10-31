package org.javersion.core;

import static java.lang.System.currentTimeMillis;

import java.util.concurrent.atomic.AtomicLong;

import org.javersion.util.BinaryEncoder;

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
 */
public final class Revision implements Comparable<Revision> {

    private static final BinaryEncoder ENCODER = BinaryEncoder.BASE32_CROCKFORD_NUMBER;

    public static final Revision MIN_VALUE = new Revision(0, 0);

    public static final Revision MAX_VALUE = new Revision(-1, -1);

    private static final long NODE = UUIDGen.getClockSeqAndNode();

    private final long timeSeq;

    private final long node;

    public Revision(String rev) {
        if (rev.length() != 27) {
            throw new IllegalArgumentException("Expected string of length 27");
        }
        this.timeSeq = ENCODER.decodeLong(rev.substring(0, 13));
        this.node = ENCODER.decodeLong(rev.substring(14, 27));
    }

    public Revision() {
        this(newUniqueTime(), NODE);
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
        return new StringBuilder(27)
                .append(ENCODER.encodeLong(timeSeq))
                .append('-')
                .append(ENCODER.encodeLong(node)).toString();
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
}
