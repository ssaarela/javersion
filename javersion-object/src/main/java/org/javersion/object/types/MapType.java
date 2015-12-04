package org.javersion.object.types;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static org.javersion.core.Persistent.NULL;

import java.util.Map;

import org.javersion.core.Persistent;
import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.NodeId;
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
            // Skip binding of null values
            if (NULL.equals(context.getProperty(entryPath))) {
                map.put(key, null);
            } else {
                Object value = context.getObject(entryPath);
                map.put(key, value);
            }
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
            if (value == null) {
                // Skip nested serialization of null values
                context.put(entryPath, NULL);
            } else {
                context.serialize(entryPath, value);
            }
        }
    }

}
