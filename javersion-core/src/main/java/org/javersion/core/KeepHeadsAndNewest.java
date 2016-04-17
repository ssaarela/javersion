package org.javersion.core;

import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.concurrent.NotThreadSafe;

import org.javersion.util.Check;

@NotThreadSafe
public class KeepHeadsAndNewest<K, V, M> implements Predicate<VersionNode<K, V, M>> {

    private int keepCount;

    private final Set<Revision> heads;

    public KeepHeadsAndNewest(VersionGraph<K, V, M, ?, ?> graph, int count) {
        Check.that(count >= 0, "count should be >= 0");
        keepCount = count;
        heads = graph.getHeads().keyStream()
                .map(branchAndRevision -> branchAndRevision.revision)
                .collect(toSet());
    }

    @Override
    public boolean test(VersionNode<K, V, M> versionNode) {
        return heads.contains(versionNode.revision) || keepCount-- > 0;
    }

}
