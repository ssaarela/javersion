package org.javersion.object;

import static org.javersion.core.Diff.diff;

import java.util.Map;

import org.javersion.core.Merge;
import org.javersion.core.Version;
import org.javersion.path.PropertyPath;

public class ObjectVersionBuilder<M> extends Version.Builder<PropertyPath, Object, ObjectVersionBuilder< M>> {

    private final ObjectVersionManager<?, M> manager;

    private final Map<PropertyPath, Object> properties;

    protected M meta;

    public ObjectVersionBuilder(ObjectVersionManager<?, M> manager, Map<PropertyPath, Object> properties) {
        this.manager = manager;
        this.properties = properties;
    }

    public ObjectVersionBuilder<M> meta(M meta) {
        this.meta = meta;
        return this;
    }

    @Override
    public ObjectVersion<M> build() {
        Merge<PropertyPath, Object> merge = manager.mergeRevisions(parentRevisions);
        changeset(diff(merge.getProperties(), properties));
        ObjectVersion<M> version = new ObjectVersion<>(this);
        manager.commit(version);
        return version;
    }
}