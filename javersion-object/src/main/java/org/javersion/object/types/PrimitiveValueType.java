package org.javersion.object.types;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.NodeId;
import org.javersion.path.PropertyTree;

public abstract class PrimitiveValueType extends AbstractScalarType {

    private PrimitiveValueType() {}

    public static final PrimitiveValueType LONG = new PrimitiveValueType() {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return ((Number) value).longValue();
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, (Long) object);
        }

        @Override
        public Object fromNodeId(NodeId nodeId, ReadContext context) {
            return nodeId.getIndex();
        }

        @Override
        public NodeId toNodeId(Object object, WriteContext context) {
            return NodeId.index((Long) object);
        }
    };

    public static final PrimitiveValueType INT = new PrimitiveValueType() {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return ((Number) value).intValue();
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, ((Integer) object).longValue());
        }

        @Override
        public Object fromNodeId(NodeId nodeId, ReadContext context) {
            return (int) nodeId.getIndex();
        }

        @Override
        public NodeId toNodeId(Object object, WriteContext context) {
            return NodeId.index((Integer) object);
        }
    };

    public static final PrimitiveValueType SHORT = new PrimitiveValueType() {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return ((Number) value).shortValue();
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, ((Short) object).longValue());
        }

        @Override
        public Object fromNodeId(NodeId nodeId, ReadContext context) {
            return (short) nodeId.getIndex();
        }

        @Override
        public NodeId toNodeId(Object object, WriteContext context) {
            return NodeId.index((Short) object);
        }
    };

    public static final PrimitiveValueType BYTE = new PrimitiveValueType() {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return ((Number) value).byteValue();
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, ((Byte) object).longValue());
        }

        @Override
        public Object fromNodeId(NodeId nodeId, ReadContext context) {
            return (byte) nodeId.getIndex();
        }

        @Override
        public NodeId toNodeId(Object object, WriteContext context) {
            return NodeId.index((Byte) object);
        }
    };

    public static final PrimitiveValueType BOOLEAN = new PrimitiveValueType() {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return value;
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, (Boolean) object);
        }

        @Override
        public Object fromNodeId(NodeId nodeId, ReadContext context) {
            return nodeId.getIndex() != 0;
        }

        @Override
        public NodeId toNodeId(Object object, WriteContext context) {
            return (Boolean) object ? NodeId.index(1) : NodeId.index(0);
        }
    };

    public static final PrimitiveValueType DOUBLE = new PrimitiveValueType() {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return ((Number) value).doubleValue();
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, (Double) object);
        }

        @Override
        public Object fromNodeId(NodeId nodeId, ReadContext context) {
            return Double.longBitsToDouble(nodeId.getIndex());
        }

        @Override
        public NodeId toNodeId(Object object, WriteContext context) {
            return NodeId.index(Double.doubleToRawLongBits((Double) object));
        }
    };

    public static final PrimitiveValueType FLOAT = new PrimitiveValueType() {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return ((Number) value).floatValue();
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, ((Float) object).doubleValue());
        }

        @Override
        public Object fromNodeId(NodeId nodeId, ReadContext context) {
            return (float) Double.longBitsToDouble(nodeId.getIndex());
        }

        @Override
        public NodeId toNodeId(Object object, WriteContext context) {
            double d = ((Float) object).doubleValue();
            return NodeId.index(Double.doubleToRawLongBits(d));
        }
    };

    public static final PrimitiveValueType CHAR = new PrimitiveValueType() {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return value.toString().charAt(0);
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, object.toString());
        }

        @Override
        public Object fromNodeId(NodeId nodeId, ReadContext context) {
            return Character.valueOf(nodeId.getKey().charAt(0));
        }

        @Override
        public NodeId toNodeId(Object object, WriteContext context) {
            return NodeId.key(object.toString());
        }
    };

}
