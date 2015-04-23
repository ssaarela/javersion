package org.javersion.object;

import java.util.Map;

import org.javersion.core.VersionNode;
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
        changeset(newProperties, manager.getVersionGraph());
        ObjectVersion<M> version = super.build();
        manager.commit(version);
        return version;
    }

    public VersionNode<PropertyPath, Object, M> buildVersionNode() {
        ObjectVersion<M> version = build();
        return manager.getVersionNode(version.revision);
    }

}