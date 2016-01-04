package org.javersion.object.types;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static org.javersion.core.Persistent.NULL;

import java.util.Map;

import org.javersion.core.Persistent;
import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;
import org.javersion.util.Check;

public class MapType implements ValueType {

    public static final Persistent.Object CONSTANT = Persistent.object();

    private final ScalarType keyType;

    public MapType(ScalarType keyType) {
        this.keyType = Check.notNull(keyType, "keyType");
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object constant, ReadContext context) throws Exception {
        Map<Object, Object> map = newMap(propertyTree.getChildren().size());
        for (PropertyTree entryPath : propertyTree.getChildren()) {
            Object key = keyType.fromNodeId(entryPath.path.getNodeId(), context);
            Object value = null;
            if (!NULL.equals(context.getProperty(entryPath))) {
                value = context.getObject(entryPath);
            }
            map.put(key, value);
        }
        return map;
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {}

    protected Map<Object, Object> newMap(int size) {
        return newHashMapWithExpectedSize(size);
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        Map<?, ?> map = (Map<?, ?>) object;
        context.put(path, CONSTANT);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            PropertyPath entryPath = path.node(keyType.toNodeId(key, context));
            if (value == null) {
                // Skip nested serialization of null values
                context.put(entryPath, NULL);
            } else {
                context.serialize(entryPath, value);
            }
        }
    }

}
