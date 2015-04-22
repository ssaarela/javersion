package org.javersion.json.web;

import static java.util.Arrays.asList;
import static org.javersion.json.web.JsonStoreConfig.UnreferencedUpdateStrategy.BRANCH_HEADS;
import static org.javersion.json.web.JsonStoreConfig.UnreferencedUpdateStrategy.TOP;

import java.util.List;

import org.javersion.core.Revision;
import org.javersion.core.VersionGraph;
import org.javersion.core.VersionNode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class JsonStoreConfig {

    public List<UnreferencedUpdateStrategy> unreferencedUpdateStrategy = ImmutableList.of(
            BRANCH_HEADS,
            TOP
    );

    public static enum UnreferencedUpdateStrategy {
        TIP {
            @Override
            Iterable<Revision> getParents(VersionGraph<?, ?, ?, ?, ?> versionGraph, String branch) {
                VersionNode<?, ?, ?> tip = versionGraph.getTip();
                if (tip == null) {
                    return null;
                }
                return asList(tip.getRevision());
            }
        },
        BRANCH_HEAD {
            @Override
            Iterable<Revision> getParents(VersionGraph<?, ?, ?, ?, ?> versionGraph, String branch) {
                VersionNode<?, ?, ?> branchHead = versionGraph.getHead(branch);
                if (branchHead != null) {
                    return asList(branchHead.getRevision());
                } else {
                    return null;
                }
            }
        },
        BRANCH_HEADS {
            @Override
            Iterable<Revision> getParents(VersionGraph<?, ?, ?, ?, ?> versionGraph, String branch) {
                Iterable<? extends VersionNode<?, ?, ?>> heads = versionGraph.getHeads(branch);
                if (heads.iterator().hasNext()) {
                    return Iterables.transform(heads, n -> n.getRevision());
                } else {
                    return null;
                }
            }
        },
        TOP {
            @Override
            Iterable<Revision> getParents(VersionGraph<?, ?, ?, ?, ?> versionGraph, String branch) {
                return asList();
            }
        };
        abstract Iterable<Revision> getParents(VersionGraph<?, ?, ?, ?, ?> versionGraph, String branch);
    }
}
