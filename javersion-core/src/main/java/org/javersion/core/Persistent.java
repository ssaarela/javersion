package org.javersion.core;

import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.concurrent.Immutable;

import org.javersion.util.Check;

import com.google.common.collect.ImmutableMap;

/**
 * tombstore - removed key
 * Null - null as value
 * Object (type alias)
 * Array
 * String (Character, Enum…)
 * Boolean
 * Long (Integer, Short, Byte)
 * Double (Float)
 * BigDecimal (BigInteger)
 */
public final class Persistent {

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

    public static final Null NULL = new Null();

    private static final Object GENERIC_OBJECT = new Object(GENERIC_TYPE);

    private static final Array ARRAY = new Array();

    private Persistent() {}

    public enum Type {
        TOMBSTONE(Void.class),
        NULL(Null.class),
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

    @Immutable
    public static final class Null {
        private Null() {}
        @Override
        public boolean equals(java.lang.Object obj) {
            return (obj == this || obj instanceof Null);
        }
        @Override
        public int hashCode() {
            return Null.class.hashCode();
        }
        @Override
        public String toString() {
            return "Null";
        }
    }

    @Immutable
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

    @Immutable
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



}
