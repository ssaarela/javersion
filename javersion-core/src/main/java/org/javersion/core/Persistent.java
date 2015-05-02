package org.javersion.core;

import java.math.BigDecimal;
import java.util.Map;

import org.javersion.util.Check;

import com.google.common.collect.ImmutableMap;

/**
 * Object (type alias)
 * Array
 * String (Character, Enum…)
 * Boolean
 * Long (Integer, Short, Byte)
 * Double (Float)
 * BigDecimal (BigInteger)
 */
public final class Persistent {

    private Persistent() {}

    public enum Type {
        NULL(Void.class),
        OBJECT(Object.class),
        ARRAY(Array.class),
        STRING(String.class),
        BOOLEAN(Boolean.class),
        LONG(Long.class),
        DOUBLE(Double.class),
        BIG_DECIMAL(BigDecimal.class);

        public final Class<?> clazz;

        public static final Map<Class<?>, Type> TYPES_BY_CLASS;

        static {
            ImmutableMap.Builder<Class<?>, Type> builder = ImmutableMap.builder();
            for (Type type : Type.values()) {
                builder.put(type.clazz, type);
            }
            TYPES_BY_CLASS = builder.build();
        }

        public static Type of(java.lang.Object object) {
            Class<?> clazz = object != null ? object.getClass() : Void.class;
            return TYPES_BY_CLASS.get(clazz);
        }

        Type(Class<?> clazz) {
            this.clazz = clazz;
        }

    }

    public static final String GENERIC_TYPE = "Map";

    public static Object object() {
        return GENERIC_OBJECT;
    }

    public static Object object(String alias) {
        return new Object(alias);
    }

    public static Array array() {
        return ARRAY;
    }

    public static final class Array {
        private Array() {}
        @Override
        public boolean equals(java.lang.Object obj) {
            return (obj == this || obj instanceof Array);
        }
        @Override
        public int hashCode() {
            return Array.class.hashCode();
        }
        @Override
        public String toString() {
            return "Array()";
        }
    }

    public static final class Object {
        public final String type;
        private Object(String type) {
            this.type = Check.notNull(type, "type");
        }
        @Override
        public boolean equals(java.lang.Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Object) {
                Object other = (Object) obj;
                return this.type.equals(other.type);
            } else {
                return false;
            }
        }
        public boolean isGeneric() {
            return GENERIC_TYPE.equals(type);
        }
        @Override
        public int hashCode() {
            return type.hashCode();
        }
        @Override
        public String toString() {
            return "Object(" + type + ")";
        }
    }

    private static final Object GENERIC_OBJECT = new Object(GENERIC_TYPE);

    private static final Array ARRAY = new Array();

}
