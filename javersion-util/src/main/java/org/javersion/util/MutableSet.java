package org.javersion.util;

import java.util.Set;

public interface MutableSet<E> extends Set<E> {

    PersistentSet<E> toPersistentSet();

    /**
     * NOTE: addAll(Iterable&lt;E&gt;) complains about ambiguous method on the usage side e.g. when called with a Set.
     * 
     * @param iterable
     * @return
     */
    boolean addAllFrom(Iterable<E> iterable);

}
