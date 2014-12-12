package org.javersion.object.types;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Float.floatToRawIntBits;

import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyTree;

import com.google.common.base.Strings;

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
        public Object fromString(String str) {
            if (isNullOrEmpty(str)) {
                return null;
            }
            return Long.valueOf(str);
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
        public Object fromString(String str) {
            if (isNullOrEmpty(str)) {
                return null;
            }
            return Integer.valueOf(str);
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
        public Object fromString(String str) {
            if (isNullOrEmpty(str)) {
                return null;
            }
            return Short.valueOf(str);
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
        public Object fromString(String str) {
            if (isNullOrEmpty(str)) {
                return null;
            }
            return Byte.valueOf(str);
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

        @Override
        public Object fromString(String str) {
            if (isNullOrEmpty(str)) {
                return null;
            }
            return Boolean.valueOf(str);
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
        public Object fromString(String str) {
            if (isNullOrEmpty(str)) {
                return null;
            }
            return Double.valueOf(str);
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
        public Object fromString(String str) {
            if (isNullOrEmpty(str)) {
                return null;
            }
            return Float.valueOf(str);
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
        public Object fromString(String str) {
            if (isNullOrEmpty(str)) {
                return null;
            }
            return Character.valueOf(str.charAt(0));
        }

    };

}
