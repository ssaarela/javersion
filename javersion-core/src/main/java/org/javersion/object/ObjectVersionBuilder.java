package org.javersion.object;

import static org.javersion.core.Diff.diff;

import java.util.Map;

import org.javersion.core.Merge;
import org.javersion.core.Version;
import org.javersion.path.PropertyPath;

public class ObjectVersionBuilder<M> extends Version.Builder<PropertyPath, Object, M, ObjectVersionBuilder<M>> {

    private final ObjectVersionManager<?, M> manager;

    private final Map<PropertyPath, Object> properties;

    public ObjectVersionBuilder(ObjectVersionManager<?, M> manager, Map<PropertyPath, Object> properties) {
        this.manager = manager;
        this.properties = properties;
    }

    @Override
    public ObjectVersion<M> build() {
        Merge<PropertyPath, Object, M> merge = manager.mergeRevisions(parentRevisions);
        changeset(diff(merge.getProperties(), properties));
        ObjectVersion<M> version = new ObjectVersion<>(this);
        manager.commit(version);
        return version;
    }
}