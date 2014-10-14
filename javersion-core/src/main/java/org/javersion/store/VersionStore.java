package org.javersion.store;

import javax.annotation.Nullable;

import org.javersion.core.Version;

public interface VersionStore<I, K, V> {

    public void append(I id, Version<K, V> version);

    public void append(I id, Iterable<Version<K, V>> versions);

    public Iterable<Version<K, V>> load(I id);

    public Iterable<Version<K, V>> load(I id, @Nullable Long sinceRevision, @Nullable Long untilRevision);

}
