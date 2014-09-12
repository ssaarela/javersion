package org.javersion.object;

import java.util.Set;

import org.javersion.core.Merge;
import org.javersion.path.PropertyPath;

public class MergeObject<T> {

    public final T object;

    public final Merge<PropertyPath, Object> merge;

    public MergeObject(T object, Merge<PropertyPath, Object> merge) {
        this.object = object;
        this.merge = merge;
    }

}
