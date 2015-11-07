package org.javersion.path;

import com.google.common.base.Preconditions;

public abstract class NodeId {

    public static final NodeId ANY = new SpecialNodeId("*", null);

    public static final NodeId ANY_INDEX = new SpecialNodeId("[]", ANY);

    public static final NodeId ANY_KEY = new SpecialNodeId("{}", ANY);

    public static final NodeId ANY_PROPERTY = new SpecialNodeId(".*", ANY_KEY);


    private static final IndexId[] INDEXES;

    static {
        INDEXES = new IndexId[32];
        for (int i=0; i < INDEXES.length; i++) {
            INDEXES[i] = new IndexId(i);
        }
    }

    public static IndexId valueOf(long index) {
        Preconditions.checkArgument(index >= 0, "index should be >= 0");
        if (index < INDEXES.length) {
            return INDEXES[(int) index];
        }
        return new IndexId(index);
    }

    public static KeyId valueOf(String key) {
        return new KeyId(key);
    }

    public static NodeId valueOf(Object object) {
        if (object instanceof Number) {
            return valueOf(((Number) object).longValue());
        } else if (object instanceof String) {
            return valueOf((String) object);
        } else {
            throw new IllegalArgumentException("Unsupported NodeId type: " + object);
        }
    }

    NodeId() {}

    public boolean isIndex() {
        return false;
    }

    public boolean isKey() {
        return false;
    }

    public long getIndex() {
        throw new UnsupportedOperationException();
    }

    public String getKey() {
        throw new UnsupportedOperationException();
    }

    public Object getKeyOrIndex() {
        if (isIndex()) {
            return getIndex();
        } else {
            return getKey();
        }
    }

    public abstract NodeId fallbackId();


    public static final class IndexId extends NodeId {

        public final long index;

        IndexId(long index) {
            Preconditions.checkArgument(index >= 0, "index should be >= 0");
            this.index = index;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof IndexId) {
                return ((IndexId) obj).index == this.index;
            } else {
                return false;
            }
        }

        @Override
        public boolean isIndex() {
            return true;
        }

        @Override
        public long getIndex() {
            return index;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(index);
        }

        @Override
        public String toString() {
            return Long.toString(index);
        }

        @Override
        public NodeId fallbackId() {
            return ANY_INDEX;
        }
    }

    public static class KeyId extends NodeId {

        public final String key;

        KeyId(String key) {
            super();
            Preconditions.checkNotNull(key);
            this.key = key;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof KeyId) {
                return ((KeyId) obj).key.equals(this.key);
            } else {
                return false;
            }
        }

        @Override
        public boolean isKey() {
            return true;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public String toString() {
            return key;
        }

        @Override
        public NodeId fallbackId() {
            return ANY_KEY;
        }
    }

    public static final class PropertyId extends KeyId {

        PropertyId(String key) {
            super(key);
        }

        @Override
        public NodeId fallbackId() {
            return ANY_PROPERTY;
        }
    }

    static final class SpecialNodeId extends NodeId {

        private final String str;

        private final NodeId fallback;

        SpecialNodeId(String str, NodeId fallback) {
            this.str = str;
            this.fallback = fallback;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof SpecialNodeId) {
                return ((SpecialNodeId) obj).str.equals(this.str);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return str.hashCode();
        }

        @Override
        public String toString() {
            return str;
        }

        @Override
        public NodeId fallbackId() {
            return fallback;
        }
    }
}
