package org.javersion.object;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

public abstract class PropertyPath implements Iterable<PropertyPath> {
    
    public static final Root ROOT = Root.ROOT;
    
    PropertyPath() {}
    
    public Property property(String name) {
        return new Property(this, name);
    }
    
    public Index index(long index) {
        return index(Long.toString(index));
    }
    
    public Index index(String index) {
        return new Index(this, index);
    }
    
    public Iterator<PropertyPath> iterator() {
        return path().iterator();
    }
    
    public List<PropertyPath> path() {
        return fullPath != null ? fullPath : (fullPath = getFullPath());
    }
    
    public boolean isRoot() {
        return false;
    }
    
    public boolean startsWith(PropertyPath other) {
        List<PropertyPath> thisPath = path();
        List<PropertyPath> otherPath = other.path();
        int otherSize = otherPath.size();
        return thisPath.size() >= otherSize && thisPath.get(otherSize - 1).equals(otherPath.get(otherSize - 1));
    }

    public abstract String toString();
    
    public abstract boolean equals(Object obj);
    
    public abstract int hashCode();
    
    public abstract String node();
    
    
    
    private volatile List<PropertyPath> fullPath;
    
    abstract List<PropertyPath> getFullPath();

    public static final class Root extends PropertyPath {
        
        private static final String STR = "";

        private static final Root ROOT = new Root();
        
        private static final List<PropertyPath> FULL_PATH = ImmutableList.<PropertyPath>of(ROOT);
        
        private Root() {}
        
        List<PropertyPath> getFullPath() {
            return FULL_PATH;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }
        
        @Override
        public String toString() {
            return STR;
        }

        @Override
        public int hashCode() {
            return 1;
        }
        
        public boolean isRoot() {
            return true;
        }

        @Override
        public String node() {
            return STR;
        }

    }
    
    public static abstract class SubPath extends PropertyPath {
        
        public final PropertyPath parent;
        
        private SubPath(PropertyPath parent) {
            this.parent = checkNotNull(parent, "parent");
        }
        
        List<PropertyPath> getFullPath() {
            List<PropertyPath> parentPath = parent.getFullPath();
            ImmutableList.Builder<PropertyPath> pathBuilder = ImmutableList.builder();
            pathBuilder.addAll(parentPath);
            pathBuilder.add(this);
            return pathBuilder.build();
        }

    }
    
    public static final class Property extends SubPath {

        public final String name;
        
        private Property(PropertyPath parent, String name) {
            super(parent);
            this.name = checkNotNull(name, "name");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Property) {
                Property other = (Property) obj;
                return this.name.equals(other.name) && parent.equals(other.parent);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return 31 * parent.hashCode() + name.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder().append(parent.toString());
            if (!parent.isRoot()) {
                sb.append('.');
            }
            return sb.append(encode(name)).toString();
        }

        @Override
        public String node() {
            return name;
        }

    }
    
    public static final class Index extends SubPath {
        
        public final String index;
        
        private Index(PropertyPath parent, String index) {
            super(parent);
            this.index = checkNotNull(index, "index");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Index) {
                Index other = (Index) obj;
                return this.index.equals(other.index) && parent.equals(other.parent);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return 31 * parent.hashCode() + index.hashCode();
        }

        @Override
        public String toString() {
            return new StringBuilder().append(parent.toString()).append('[').append(encode(index)).append(']').toString();
        }

        @Override
        public String node() {
            return index;
        }
        
    }
    
    static String encode(String str) {
        return str.replace("\\", "\\\\").replace(".", "\\.").replace("]", "\\]");
    }

}
