package org.javersion.object.types;

import java.util.Map;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;
import org.javersion.path.PropertyPath.SubPath;

import com.google.common.collect.Maps;

public class MapType implements ValueType {

    private static final String CONSTANT = "Map";

    public static final String KEY = "@KEY@";

    private final IdentifiableType keyType;

    public MapType(IdentifiableType keyType) {
        this.keyType = keyType;
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object mapSize, ReadContext context) throws Exception {
        prepareKeysAndValues(propertyTree, context);
        return Maps.newHashMapWithExpectedSize(propertyTree.getChildren().size());
    }

    private void prepareKeysAndValues(PropertyTree propertyTree, ReadContext context) {
        for (PropertyTree entryPath : propertyTree.getChildren()) {
            context.prepareObject(entryPath.get(KEY));
            context.prepareObject(entryPath);
        }
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
        @SuppressWarnings("unchecked")
        Map<Object, Object> map = (Map<Object, Object>) object;
        for (PropertyTree entryPath : propertyTree.getChildren()) {
            Object key = context.getObject(entryPath.get(KEY));
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
            context.serialize(entryPath.property(KEY), key);
            context.serialize(entryPath, value);
        }
    }

    @Override
    public boolean isReference() {
        return false;
    }

}
