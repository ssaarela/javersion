package org.javersion.store;

import javax.annotation.Nullable;

import org.javersion.core.VersionGraph;
import org.javersion.core.VersionGraphBuilder;
import org.javersion.core.Revision;
import org.javersion.core.Version;

public interface VersionStore<I,
        K, V, M,
        G extends VersionGraph<K, V, M, G, B>,
        B extends VersionGraphBuilder<K, V, M, G, B>> {

    long getNode();

    void append(I id, Version<K, V, M> version);

    void append(I id, Iterable<Version<K, V, M>> versions);

    void commit();

    G load(I id);

    G load(I id, @Nullable Revision revision);

}
