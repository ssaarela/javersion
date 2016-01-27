package org.javersion.object;

import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

import org.javersion.core.Revision;
import org.javersion.core.Version;
import org.javersion.core.VersionNode;
import org.javersion.path.PropertyPath;
import org.javersion.path.SchemaPathFilter;

@NotThreadSafe
public class ManagedObjectVersionBuilder<M> extends Version.BuilderBase<PropertyPath, Object, M, ManagedObjectVersionBuilder<M>> {

    private final ObjectVersionManager<?, M> manager;

    private final Map<PropertyPath, Object> newProperties;

    private Iterable<Revision> rebaseOn;

    public ManagedObjectVersionBuilder(ObjectVersionManager<?, M> manager, Map<PropertyPath, Object> newProperties) {
        this.manager = manager;
        this.newProperties = newProperties;
    }

    public ObjectVersion<M> build(boolean commit) {
        if (manager.useSchemaFilter) {
            changeset(newProperties, manager.getVersionGraph(), new SchemaPathFilter(manager.getSchema()));
        } else {
            changeset(newProperties, manager.getVersionGraph());
        }
        if (rebaseOn != null) {
            parents(rebaseOn);
        }
        ObjectVersion<M> version = new ObjectVersion<>(this);
        if (commit) {
            manager.commit(version);
        }
        return version;
    }

    public ManagedObjectVersionBuilder<M> rebaseOn(Iterable<Revision> revisions) {
        this.rebaseOn = revisions;
        return this;
    }

    @Override
    public ObjectVersion<M> build() {
        return build(true);
    }

    public VersionNode<PropertyPath, Object, M> buildVersionNode() {
        ObjectVersion<M> version = build(true);
        return manager.getVersionNode(version.revision);
    }

}