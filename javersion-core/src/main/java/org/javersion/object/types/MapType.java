package org.javersion.object.types;

import java.util.Map;

import org.javersion.object.Persistent;
import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.path.PropertyTree;

import com.google.common.collect.Maps;

public class MapType implements ValueType {

    public static final Persistent.Object CONSTANT = Persistent.object();

    public static final String KEY = "@KEY";

    private final IdentifiableType keyType;

    public MapType(IdentifiableType keyType) {
        this.keyType = keyType;
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object mapSize, ReadContext context) throws Exception {
        prepareKeys(propertyTree, context);
        return Maps.newHashMapWithExpectedSize(propertyTree.getChildren().size());
    }

    private void prepareKeys(PropertyTree propertyTree, ReadContext context) {
        for (PropertyTree entryPath : propertyTree.getChildren()) {
            if (!isScalar()) {
                context.prepareObject(entryPath.get(KEY));
            }
        }
    }

    private boolean isScalar() {
        return keyType instanceof ScalarType;
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
        @SuppressWarnings("unchecked")
        Map<Object, Object> map = (Map<Object, Object>) object;
        for (PropertyTree entryPath : propertyTree.getChildren()) {
            Object key;
            if (isScalar()) {
                key = ((ScalarType) keyType).fromString(entryPath.path.getName());
            } else {
                key = context.getObject(entryPath.get(KEY));
            }
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
            SubPath entryPath = path.index(keyType.toString(key));
            if (!isScalar()) {
                context.serialize(entryPath.property(KEY), key);
            }
            context.serialize(entryPath, value);
        }
    }

    @Override
    public boolean isReference() {
        return false;
    }

}
