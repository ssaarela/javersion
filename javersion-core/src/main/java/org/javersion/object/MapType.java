package org.javersion.object;

import java.util.Map;

import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;
import org.javersion.path.PropertyPath.SubPath;

import com.google.common.collect.Maps;

public class MapType implements ValueType {

    public static final String KEY = "@KEY@";

    private final IdentifiableType keyType;
    
    public MapType(IdentifiableType keyType) {
        this.keyType = keyType;
    }
    
    @Override
    public Object instantiate(PropertyTree propertyTree, Object mapSize, ReadContext context) throws Exception {
        Map<Object, Object> map = Maps.newHashMapWithExpectedSize((Integer) mapSize);
        for (PropertyTree entryPath : propertyTree.getChildren()) {
            Object key = context.getAndBindObject(entryPath.get(KEY));
            Object value = context.getObject(entryPath);
            map.put(key, value);
        }
        return map;
    }

    @Override
    public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {}

    @Override
    public void serialize(Object object, WriteContext context) {
        Map<?, ?> map = (Map<?, ?>) object;
        context.put(map.size());
        PropertyPath path = context.getCurrentPath();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            SubPath entryPath = path.index(keyType.toString(key));
            context.serialize(entryPath.property(KEY), key);
            context.serialize(entryPath, value);
        }
    }

    @Override
    public Class<?> getTargetType() {
        // TODO Auto-generated method stub
        return null;
    }

}
