package org.javersion.object;

import static org.javersion.core.Diff.diff;

import java.util.Map;

import org.javersion.core.Merge;
import org.javersion.core.Revision;
import org.javersion.core.Version;
import org.javersion.core.VersionGraph;
import org.javersion.path.PropertyPath;

public class ObjectVersionBuilder<M> extends Version.Builder<PropertyPath, Object, M, ObjectVersionBuilder<M>> {

    public ObjectVersionBuilder() {}

    public ObjectVersionBuilder(Revision revision) {
        super(revision);
    }

    @Override
    public ObjectVersion<M> build() {
        return new ObjectVersion<>(this);
    }

    public void changeset(Map<PropertyPath, Object> newProperties, VersionGraph<PropertyPath, Object, M, ObjectVersionGraph<M>, ?> versionGraph) {
        Merge<PropertyPath, Object, M> merge = versionGraph.mergeRevisions(parentRevisions);
        changeset(diff(merge, newProperties));
    }
}