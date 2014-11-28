package org.javersion.object;

import org.javersion.core.Revision;
import org.javersion.core.Version;
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

}