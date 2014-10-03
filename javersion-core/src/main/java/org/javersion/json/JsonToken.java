package org.javersion.json;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public abstract class JsonToken<T> implements Serializable {

    public static enum Type {
        OBJECT, ARRAY, NUMBER, BOOLEAN, STRING, NULL
    }

    private JsonToken() {}

    public abstract T value();

    public abstract Type type();

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof JsonToken) {
            JsonToken<?> other = (JsonToken<?>) obj;
            return Objects.equals(this.type(), other.type()) && Objects.equals(this.value(), other.value());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        Object value = value();
        return  31 * type().hashCode() + (value==null ? 0 : value.hashCode());
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", type(), value());
    }

    public static final class Obj extends JsonToken<Void> {

        public static Obj VALUE = new Obj();

        @Override
        public Void value() {
            return null;
        }

        @Override
        public Type type() {
            return Type.OBJECT;
        }
    }


    public static final class Arr extends JsonToken<Void> {

        public static Arr VALUE = new Arr();

        @Override
        public Void value() {
            return null;
        }

        @Override
        public Type type() {
            return Type.ARRAY;
        }
    }

    public static final class Nbr extends JsonToken<BigDecimal> {

        private final BigDecimal value;

        public Nbr(String value) {
            this(new BigDecimal(value));
        }

        public Nbr(BigDecimal value) {
            this.value = value;
        }

        @Override
        public BigDecimal value() {
            return value;
        }

        @Override
        public Type type() {
            return Type.NUMBER;
        }
    }

    public static final class Bool extends JsonToken<Boolean> {

        public static final JsonToken<?> TRUE = new Bool(true);

        public static final JsonToken<?> FALSE = new Bool(false);

        private final Boolean value;

        public Bool(boolean value) {
            this.value = value;
        }

        @Override
        public Boolean value() {
            return value;
        }

        @Override
        public Type type() {
            return Type.BOOLEAN;
        }
    }

    public static final class Str extends JsonToken<String> {

        private final String value;

        public Str(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public Type type() {
            return Type.STRING;
        }
    }

    public static final class Nil extends JsonToken<Void> {

        public static final Nil VALUE = new Nil();

        @Override
        public Void value() {
            return null;
        }

        @Override
        public Type type() {
            return Type.NULL;
        }
    }

}
