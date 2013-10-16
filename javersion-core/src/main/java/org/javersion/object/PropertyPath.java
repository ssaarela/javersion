/*
 * Copyright 2013 Samppa Saarela
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
package org.javersion.object;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.List;

import org.javersion.reflect.Check;

import com.google.common.collect.ImmutableList;

public abstract class PropertyPath implements Iterable<PropertyPath> {

    private static final String EMPTY_STRING = "";
    
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
    
    public PropertyPath toSchemaPath() {
        PropertyPath schemaPath = null;
        for (PropertyPath path : this) {
            schemaPath = path.normalize(schemaPath);
        }
        return schemaPath;
    }

    public abstract String toString();
    
    public abstract boolean equals(Object obj);
    
    public abstract int hashCode();
    
    public abstract String getName();
    

    abstract List<PropertyPath> getFullPath();
    
    abstract PropertyPath normalize(PropertyPath newParent);

    
    private volatile List<PropertyPath> fullPath;

    public static final class Root extends PropertyPath {

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
            return EMPTY_STRING;
        }

        @Override
        public int hashCode() {
            return 1;
        }
        
        public boolean isRoot() {
            return true;
        }

        @Override
        public String getName() {
            return EMPTY_STRING;
        }

        @Override
        Root normalize(PropertyPath newParent) {
            return ROOT;
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
            Check.notNullOrEmpty(name, "name");
            this.name = name;
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
        public String getName() {
            return name;
        }

        @Override
        Property normalize(PropertyPath newParent) {
            return parent.equals(newParent) ? this : new Property(newParent, name);
        }

    }
    
    public static final class Index extends SubPath {
        
        public final String index;
        
        private Index(PropertyPath parent, String index) {
            super(parent);
            this.index = Check.notNull(index, "index");
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
        public String getName() {
            return index;
        }
        
        @Override
        Index normalize(PropertyPath newParent) {
            return parent.equals(newParent) && EMPTY_STRING.equals(index) ? this : new Index(newParent, EMPTY_STRING);
        }
        
    }
    
    static String encode(String str) {
        return str.replace("\\", "\\\\").replace(".", "\\.").replace("]", "\\]");
    }

}
