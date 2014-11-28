package org.javersion.object;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Set;

import org.javersion.core.Merge;
import org.javersion.core.Revision;
import org.javersion.path.PropertyPath;

public class ObjectVersionManager<O, M> {

    private ObjectVersionGraph<M> versionGraph;

    private Set<Revision> heads = of();

    private final ObjectSerializer<O> serializer;

    public ObjectVersionManager(Class<O> clazz) {
        this(new ObjectSerializer<>(clazz));
    }

    public ObjectVersionManager(ObjectSerializer<O> serializer) {
        this.serializer = serializer;
    }

    public ObjectVersionManager<O, M> init() {
        this.versionGraph = ObjectVersionGraph.init();
        return this;
    }

    public ObjectVersionManager<O, M> init(Iterable<ObjectVersion<M>> versions) {
        this.versionGraph = ObjectVersionGraph.init(versions);
        return this;
    }

    public ObjectVersionBuilder<M> versionBuilder(O object) {
        ObjectVersionBuilder<M> builder = new ManagedObjectVersionBuilder<M>(this, serializer.toPropertyMap(object));
        builder.parents(heads);
        return builder;
    }

    public MergeObject<O, M> mergeObject(String... branches) {
        return mergeObject(asList(branches));
    }

    public MergeObject<O, M> mergeObject(Collection<String> branches) {
        Merge<PropertyPath, Object, M> merge = versionGraph.mergeBranches(branches);
        MergeObject<O, M> mergeObject = new MergeObject<>(toObject(merge), merge);
        heads = merge.getMergeHeads();
        return mergeObject;
    }

    private O toObject(Merge<PropertyPath, Object, M> merge) {
        return serializer.fromPropertyMap(merge.getProperties());
    }

    Merge<PropertyPath, Object, M> mergeRevisions(Iterable<Revision> revisions) {
        return versionGraph.mergeRevisions(revisions);
    }

    void commit(ObjectVersion<M> version) {
        versionGraph = versionGraph.commit(version);
        heads = of(version.revision);
    }

}
