package org.javersion.store;

import org.javersion.core.VersionGraph;
import org.javersion.core.VersionGraphBuilder;
import org.javersion.core.VersionNode;

import com.google.common.collect.ImmutableSet;

public interface VersionStore<I,
        K, V, M,
        G extends VersionGraph<K, V, M, G, B>,
        B extends VersionGraphBuilder<K, V, M, G, B>> {

    long getNode();

    default void append(I id, VersionNode<K, V, M> version) {
        append(id, ImmutableSet.of(version));
    }

    void append(I id, Iterable<VersionNode<K, V, M>> versions);

    void commit();

    default G load(I... docId) {
        return load(ImmutableSet.copyOf(docId));
    }

    G load(Iterable<I> docIds);

}
