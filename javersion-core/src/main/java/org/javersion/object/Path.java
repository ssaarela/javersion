package org.javersion.object;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

public abstract class Path implements Iterable<Path> {
    
    public static final Root ROOT = Root.ROOT;
    
    public Property property(String name) {
        return new Property(this, name);
    }
    
    public Index index(String index) {
        return new Index(this, index);
    }
    
    public Iterator<Path> iterator() {
        return path().iterator();
    }
    
    public List<Path> path() {
        List<Path> fullPath = fullPathSoftReference.get();
        if (fullPath == null) {
            fullPath = getFullPath();
            fullPathSoftReference = new SoftReference<>(fullPath);
        }
        return fullPath;
    }

    
    public abstract boolean equals(Object obj);
    
    public int hashCode() {
        return path().hashCode();
    }
    
    
    
    private volatile SoftReference<List<Path>> fullPathSoftReference = new SoftReference<>(null);
    
    abstract List<Path> getFullPath();

    public static class Root extends Path {

        private static final Root ROOT = new Root();
        
        private static final List<Path> FULL_PATH = ImmutableList.<Path>of(ROOT);
        
        private Root() {}
        
        List<Path> getFullPath() {
            return FULL_PATH;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }
    }
    
    public static abstract class SubPath extends Path {
        
        public final Path parent;
        
        private SubPath(Path parent) {
            this.parent = checkNotNull(parent, "parent");
        }
        
        List<Path> getFullPath() {
            List<Path> parentPath = parent.getFullPath();
            ImmutableList.Builder<Path> pathBuilder = ImmutableList.builder();
            pathBuilder.addAll(parentPath);
            pathBuilder.add(this);
            return pathBuilder.build();
        }
    }
    
    public static class Property extends SubPath {

        public final String name;
        
        private Property(Path parent, String name) {
            super(parent);
            this.name = checkNotNull(name, "name").intern();
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
        
    }
    
    public static class Index extends SubPath {
        
        public final String index;
        
        private Index(Path parent, String index) {
            super(parent);
            this.index = checkNotNull(index, "index").intern();
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
    }
}
