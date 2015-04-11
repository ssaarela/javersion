package org.javersion.object.types;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;

import java.util.Map;

import org.javersion.object.Persistent;
import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.NodeId;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.path.PropertyTree;

public class MapType implements ValueType {

    public static final Persistent.Object CONSTANT = Persistent.object();

    private final ScalarType keyType;

    public MapType(ScalarType keyType) {
        this.keyType = keyType;
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object constant, ReadContext context) throws Exception {
        prepareKeys(propertyTree, context);
        return newMap(propertyTree.getChildren().size());
    }

    protected Map<Object, Object> newMap(int size) {
        return newHashMapWithExpectedSize(size);
    }

    private void prepareKeys(PropertyTree propertyTree, ReadContext context) throws Exception {
        for (PropertyTree entryPath : propertyTree.getChildren()) {
            keyType.fromNodeId(entryPath.path.getNodeId(), context);
        }
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
        @SuppressWarnings("unchecked")
        Map<Object, Object> map = (Map<Object, Object>) object;
        for (PropertyTree entryPath : propertyTree.getChildren()) {
            Object key = keyType.fromNodeId(entryPath.path.getNodeId(), context);
            Object value = context.getObject(entryPath);
            map.put(key, value);
        }
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        Map<?, ?> map = (Map<?, ?>) object;
        context.put(path, CONSTANT);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            NodeId nodeId = keyType.toNodeId(key, context);
            SubPath entryPath = path.keyOrIndex(nodeId);
            context.serialize(entryPath, value);
        }
    }

}
