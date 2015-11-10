package org.javersion.object;

import java.util.Set;

import org.javersion.core.Merge;
import org.javersion.core.Revision;
import org.javersion.core.VersionProperty;
import org.javersion.path.PropertyPath;
import org.javersion.util.Check;

import com.google.common.collect.Multimap;

public class MergeObject<T, M> {

    public final T object;

    public final Merge<PropertyPath, Object, M> merge;

    public MergeObject(T object, Merge<PropertyPath, Object, M> merge) {
        this.object = object;
        this.merge = Check.notNull(merge, "merge");
    }

    public T getObject() {
        return object;
    }

    public Set<Revision> getMergeHeads() {
        return merge.getMergeHeads();
    }

    public Multimap<PropertyPath, VersionProperty<Object>> getConflicts() {
        return merge.getConflicts();
    }

}
