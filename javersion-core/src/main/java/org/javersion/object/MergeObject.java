package org.javersion.object;

import java.util.Set;

import org.javersion.core.VersionProperty;
import org.javersion.path.PropertyPath;

import com.google.common.collect.Multimap;

public class MergeObject<T> {

    public final T object;

    public final Set<Long> revisions;

    public final Multimap<PropertyPath, VersionProperty<Object>> conflicts;

    public MergeObject(T object, Set<Long> revisions,
            Multimap<PropertyPath, VersionProperty<Object>> conflicts) {
        this.object = object;
        this.revisions = revisions;
        this.conflicts = conflicts;
    }

}
