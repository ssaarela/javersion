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
            return (Long) value;
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, (Long) object);
        }

    };

    public static final PrimitiveValueType INT = new PrimitiveValueType() {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return ((Long) value).intValue();
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, ((Integer) object).longValue());
        }

    };

    public static final PrimitiveValueType SHORT = new PrimitiveValueType() {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return ((Long) value).shortValue();
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, ((Short) object).longValue());
        }

    };

    public static final PrimitiveValueType BYTE = new PrimitiveValueType() {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return ((Long) value).byteValue();
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, ((Byte) object).longValue());
        }

    };

    public static final PrimitiveValueType BOOLEAN = new PrimitiveValueType() {

        private final Long ONE = 1l;

        private final Long ZERO = 0l;

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return ((Long) value).longValue() == 0l ? FALSE : TRUE;
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, ((Boolean) object).booleanValue() ? ONE : ZERO);
        }

    };

    public static final PrimitiveValueType DOUBLE = new PrimitiveValueType() {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return Double.longBitsToDouble((Long) value);
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, doubleToRawLongBits((Double) object));
        }

    };

    public static final PrimitiveValueType FLOAT = new PrimitiveValueType() {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object value, ReadContext context) throws Exception {
            return Float.intBitsToFloat(((Long) value).intValue());
        }

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            context.put(path, Long.valueOf(floatToRawIntBits((Float) object)));
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
