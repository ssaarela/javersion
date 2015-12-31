/*
 * Copyright 2015 Samppa Saarela
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javersion.path;

import static org.apache.commons.lang3.StringEscapeUtils.escapeEcmaScript;

import org.javersion.path.PropertyPath.Any;
import org.javersion.path.PropertyPath.AnyIndex;
import org.javersion.path.PropertyPath.AnyKey;
import org.javersion.path.PropertyPath.AnyProperty;
import org.javersion.util.Check;

/**
 * Search (fallback) order of NodeIds:
 *
 * <pre>
 * any *
 *  +- any index []
 *    +- index [1]
 *  + any key {}
 *    +- key ["key"]
 *    +- any property .*
 *      + property
 * </pre>
 */
public abstract class NodeId implements Comparable<NodeId> {

    public static final NodeId ROOT_ID = new SpecialNodeId("", 1, null) {
        @Override
        public PropertyPath toPath(PropertyPath parent) {
            return parent;
        }
    };

    public static final NodeId ANY = new SpecialNodeId("*", 2, null) {
        @Override
        public PropertyPath toPath(PropertyPath parent) {
            return new Any(parent);
        }
    };

    public static final NodeId ANY_INDEX = new SpecialNodeId("[]", 3, ANY) {
        @Override
        public PropertyPath toPath(PropertyPath parent) {
            return new AnyIndex(parent);
        }
    };

    public static final NodeId ANY_KEY = new SpecialNodeId("{}", 5, ANY) {
        @Override
        public PropertyPath toPath(PropertyPath parent) {
            return new AnyKey(parent);
        }
    };

    public static final NodeId ANY_PROPERTY = new SpecialNodeId(".*", 4, ANY_KEY) {
        @Override
        public PropertyPath toPath(PropertyPath parent) {
            return new AnyProperty(parent);
        }
    };


    private static final IndexId[] INDEXES;

    static {
        INDEXES = new IndexId[32];
        for (int i=0; i < INDEXES.length; i++) {
            INDEXES[i] = new IndexId(i);
        }
    }

    public static NodeId index(Number number) {
        return index(number.longValue());
    }

    public static IndexId index(long index) {
        if (index >= 0 && index < INDEXES.length) {
            return INDEXES[(int) index];
        }
        return new IndexId(index);
    }

    public static KeyId key(String key) {
        return new KeyId(key);
    }

    public static PropertyId property(String property) {
        return new PropertyId(property);
    }

    public static NodeId keyOrIndex(Object object) {
        if (object instanceof Number) {
            return index((Number) object);
        } else if (object instanceof String) {
            return key((String) object);
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

    protected abstract int getTypeOrdinal();

    public abstract PropertyPath toPath(PropertyPath parent);

    public static final class IndexId extends NodeId {

        public final long index;

        IndexId(long index) {
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
        public int hashCode() {
            return Long.hashCode(index);
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
        public String toString() {
            return Long.toString(index);
        }

        @Override
        public NodeId fallbackId() {
            return ANY_INDEX;
        }

        @Override
        protected int getTypeOrdinal() {
            return 2;
        }

        @Override
        public PropertyPath toPath(PropertyPath parent) {
            return new PropertyPath.Index(parent, this);
        }

        @Override
        public int compareTo(NodeId nodeId) {
            return nodeId instanceof IndexId
                    ? Long.compare(this.index, ((IndexId) nodeId).index)
                    : Integer.compare(this.getTypeOrdinal(), nodeId.getTypeOrdinal());
        }
    }

    private static abstract class KeyBasedId extends NodeId {

        public final String key;

        KeyBasedId(String key) {
            super();
            Check.notNull(key, "key");
            this.key = key;
        }

        @Override
        public final boolean isKey() {
            return true;
        }

        @Override
        public final String getKey() {
            return key;
        }
    }

    public static final class PropertyId extends KeyBasedId {

        PropertyId(String key) {
            super(key);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else {
                return obj instanceof PropertyId && ((PropertyId) obj).key.equals(this.key);
            }
        }

        @Override
        public final int hashCode() {
            return key.hashCode();
        }

        @Override
        public final int compareTo(NodeId nodeId) {
            return nodeId instanceof PropertyId
                    ? this.key.compareTo(((PropertyId) nodeId).key)
                    : Integer.compare(this.getTypeOrdinal(), nodeId.getTypeOrdinal());
        }

        @Override
        public NodeId fallbackId() {
            return ANY_PROPERTY;
        }

        @Override
        protected final int getTypeOrdinal() {
            return 3;
        }

        @Override
        public PropertyPath toPath(PropertyPath parent) {
            return new PropertyPath.Property(parent, this);
        }

        @Override
        public String toString() {
            return key;
        }
    }

    public final static class KeyId extends KeyBasedId {

        public final String key;

        KeyId(String key) {
            super(key);
            this.key = key;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else {
                return obj instanceof KeyId && ((KeyId) obj).key.equals(this.key);
            }
        }

        @Override
        public final int hashCode() {
            return 17 * key.hashCode();
        }

        @Override
        public final int compareTo(NodeId nodeId) {
            return nodeId instanceof KeyId
                    ? this.key.compareTo(((KeyId) nodeId).key)
                    : Integer.compare(this.getTypeOrdinal(), nodeId.getTypeOrdinal());
        }

        @Override
        public NodeId fallbackId() {
            return ANY_KEY;
        }

        @Override
        protected final int getTypeOrdinal() {
            return 4;
        }

        @Override
        public PropertyPath toPath(PropertyPath parent) {
            return new PropertyPath.Key(parent, this);
        }

        @Override
        public String toString() {
            return new StringBuilder(key.length() + 5).append('\"').append(escapeEcmaScript(key)).append('\"').toString();
        }

    }

    private static abstract class SpecialNodeId extends NodeId {

        private final String str;

        private final int ordinal;

        private final NodeId fallback;

        private SpecialNodeId(String str, int ordinal, NodeId fallback) {
            this.str = str;
            this.ordinal = ordinal;
            this.fallback = fallback;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else {
                return obj instanceof SpecialNodeId && ((SpecialNodeId) obj).str.equals(this.str);
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

        @Override
        protected int getTypeOrdinal() {
            return 1;
        }

        @Override
        public int compareTo(NodeId nodeId) {
            return nodeId instanceof SpecialNodeId
                    ? Integer.compare(this.ordinal, ((SpecialNodeId) nodeId).ordinal)
                    : Integer.compare(this.getTypeOrdinal(), nodeId.getTypeOrdinal());
        }
    }
}
