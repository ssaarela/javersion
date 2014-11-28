package org.javersion.object;

import static org.javersion.core.Diff.diff;

import java.util.Map;

import org.javersion.core.Merge;
import org.javersion.path.PropertyPath;

public class ManagedObjectVersionBuilder<M> extends ObjectVersionBuilder<M> {

    private final ObjectVersionManager<?, M> manager;

    private final Map<PropertyPath, Object> newProperties;

    public ManagedObjectVersionBuilder(ObjectVersionManager<?, M> manager, Map<PropertyPath, Object> newProperties) {
        this.manager = manager;
        this.newProperties = newProperties;
    }

    @Override
    public ObjectVersion<M> build() {
        Merge<PropertyPath, Object, M> merge = manager.mergeRevisions(parentRevisions);
        changeset(diff(merge.getProperties(), newProperties));
        ObjectVersion<M> version = new ObjectVersion<>(this);
        manager.commit(version);
        return version;
    }
}