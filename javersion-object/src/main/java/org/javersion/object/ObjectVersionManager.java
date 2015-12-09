package org.javersion.object;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Arrays.asList;
import static org.javersion.core.Version.DEFAULT_BRANCH;

import java.util.Collection;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.javersion.core.Merge;
import org.javersion.core.Revision;
import org.javersion.core.Version;
import org.javersion.core.VersionGraph;
import org.javersion.core.VersionNode;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.path.Schema;

@NotThreadSafe
public class ObjectVersionManager<O, M> {

    protected VersionGraph<PropertyPath, Object, M, ?, ?> versionGraph;

    protected Set<Revision> heads;

    protected final ObjectSerializer<O> serializer;

    final boolean useSchemaFilter;

    public ObjectVersionManager(Class<O> clazz) {
        this(new ObjectSerializer<>(clazz), false);
    }

    public ObjectVersionManager(ObjectSerializer<O> serializer, boolean useSchemaFilter) {
        this.serializer = serializer;
        this.useSchemaFilter = useSchemaFilter;
    }

    public ObjectVersionManager<O, M> init() {
        this.versionGraph = ObjectVersionGraph.init();
        return this;
    }

    public ObjectVersionManager<O, M> init(Iterable<ObjectVersion<M>> versions) {
        return init(ObjectVersionGraph.init(versions));
    }

    public ObjectVersionManager<O, M> init(VersionGraph<PropertyPath, Object, M, ?, ?> versionGraph) {
        this.versionGraph = versionGraph;
        heads = null;
        return this;
    }

    public ManagedObjectVersionBuilder<M> versionBuilder(O object) {
        ManagedObjectVersionBuilder<M> builder = new ManagedObjectVersionBuilder<M>(this, serializer.toPropertyMap(object));
        builder.parents(heads);
        return builder;
    }

    public MergeObject<O, M> mergeBranches(String... branches) {
        return mergeBranches(asList(branches));
    }

    public MergeObject<O, M> mergeBranches(Collection<String> branches) {
        if (branches.isEmpty()) {
            return mergeObject(versionGraph.mergeBranches(DEFAULT_BRANCH));
        }
        return mergeObject(versionGraph.mergeBranches(branches));
    }

    private MergeObject<O, M> mergeObject(Merge<PropertyPath, Object, M> merge) {
        MergeObject<O, M> mergeObject = new MergeObject<>(toObject(merge), merge);
        heads = merge.getMergeHeads();
        return mergeObject;
    }

    private O toObject(Merge<PropertyPath, Object, M> merge) {
        return serializer.fromPropertyMap(merge.getProperties());
    }

    public VersionNode<PropertyPath, Object, M> commit(Version<PropertyPath, Object, M> version) {
        versionGraph = versionGraph.commit(version);
        heads = of(version.revision);
        return versionGraph.getTip();
    }

    public VersionGraph<PropertyPath, Object, M, ?, ?> getVersionGraph() {
        return versionGraph;
    }

    public Set<Revision> getHeads() {
        return heads;
    }

    public VersionNode<PropertyPath, Object, M> getVersionNode(Revision revision) {
        return versionGraph.getVersionNode(revision);
    }

    public Set<String> getBranches() {
        return versionGraph.getBranches();
    }

    public Schema<ValueType> getSchema() {
        return serializer.schemaRoot;
    }

    public boolean isEmpty() {
        return versionGraph.isEmpty();
    }
}
