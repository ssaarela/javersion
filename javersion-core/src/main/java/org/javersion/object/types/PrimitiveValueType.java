package org.javersion.object.types;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Float.floatToRawIntBits;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
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

    };

    public static final PrimitiveValueType BOOLEAN = new PrimitiveValueType() {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return (Boolean) value;
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, (Boolean) object);
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

    };

}
