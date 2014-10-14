package org.javersion.core;

import com.eaio.uuid.UUID;

public final class Revision implements Comparable<Revision> {

    public static final Revision MIN_VALUE = new Revision(new UUID(Long.MIN_VALUE, Long.MIN_VALUE));

    public static final Revision MAX_VALUE = new Revision(new UUID(Long.MAX_VALUE, Long.MAX_VALUE));

    private final UUID uuid;

    public Revision() {
        this(new UUID());
    }

    public Revision(String vid) {
        this(new UUID(vid));
    }

    private Revision(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Revision) {
            Revision other = (Revision) o;
            return this.uuid.equals(other.uuid);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public int compareTo(Revision other) {
        return this.uuid.compareTo(other.uuid);
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

}
