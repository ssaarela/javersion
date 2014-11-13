package org.javersion.object;

import java.util.Set;

import org.javersion.core.Merge;
import org.javersion.path.PropertyPath;

public class MergeObject<T, M> {

    public final T object;

    public final Merge<PropertyPath, Object, M> merge;

    public MergeObject(T object, Merge<PropertyPath, Object, M> merge) {
        this.object = object;
        this.merge = merge;
    }

}
