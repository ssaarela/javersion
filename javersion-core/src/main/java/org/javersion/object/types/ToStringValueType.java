package org.javersion.object.types;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.NodeId;
import org.javersion.path.PropertyTree;

public class ToStringValueType extends AbstractScalarType {

    private final Constructor<?> constructor;

    public ToStringValueType(Class<?> type) {
        try {
            constructor = type.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
        return constructor.newInstance(value.toString());
    }

    @Override
    public void serialize(PropertyPath path, Object object, WriteContext context) {
        context.put(path, object.toString());
    }

    @Override
    public Object fromNodeId(NodeId nodeId) {
        try {
            return constructor.newInstance(nodeId.getKey());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NodeId toNodeId(Object object) {
        return NodeId.valueOf(object.toString());
    }
}
