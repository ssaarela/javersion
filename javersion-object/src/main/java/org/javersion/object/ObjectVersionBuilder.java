package org.javersion.object;

import static com.google.common.collect.Maps.filterValues;

import java.util.Map;

import org.javersion.core.Revision;
import org.javersion.core.Version;
import org.javersion.core.VersionGraph;
import org.javersion.path.PropertyPath;

public class ObjectVersionBuilder<M> extends Version.BuilderBase<PropertyPath, Object, M, ObjectVersionBuilder<M>> {

    public ObjectVersionBuilder() {}

    public ObjectVersionBuilder(Revision revision) {
        super(revision);
    }

    @Override
    public ObjectVersion<M> build() {
        return new ObjectVersion<>(this);
    }

    public void changeset(Map<PropertyPath, Object> newProperties, VersionGraph<PropertyPath, Object, M, ObjectVersionGraph<M>, ?> versionGraph) {
        if (parentRevisions != null) {
            changeset(versionGraph.mergeRevisions(parentRevisions).diff(newProperties));
        } else if (newProperties != null) {
            changeset(filterValues(newProperties, v -> v != null));
        } else {
            changeset(null);
        }
    }
}