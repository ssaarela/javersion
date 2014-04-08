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
package org.javersion.path;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.path.parser.PropertyPathBaseVisitor;
import org.javersion.path.parser.PropertyPathLexer;
import org.javersion.path.parser.PropertyPathParser;
import org.javersion.util.Check;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public abstract class PropertyPath implements Iterable<SubPath> {
    
    private static final ANTLRErrorListener ERROR_LISTERNER = new BaseErrorListener() {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                                Object offendingSymbol,
                                int line,
                                int charPositionInLine,
                                String msg,
                                RecognitionException e) {
            throw new IllegalArgumentException("line " + line + ":" + charPositionInLine + " " + msg);
        }
    };

    private static final String EMPTY_STRING = "";
    
    private static final char ESC = '\\';
    
    private static final Set<Character> ESCAPED_CHARS = ImmutableSet.of('\\', '.', '[', ']');
    
    public static final Root ROOT = Root.ROOT;
    
    public static PropertyPath parse(String path) {
        PropertyPathParser parser = new PropertyPathParser(
                new CommonTokenStream(
                        new PropertyPathLexer(
                                new ANTLRInputStream(path))));
        parser.removeErrorListeners();
        parser.addErrorListener(ERROR_LISTERNER);
        return parser.root().accept(new PropertyPathBaseVisitor<PropertyPath>() {
            
            private PropertyPath parent;

            @Override
            public PropertyPath visitRoot(@NotNull PropertyPathParser.RootContext ctx) {
                parent = ROOT;
                return super.visitRoot(ctx);
            }
            
            @Override
            public PropertyPath visitIndex(@NotNull PropertyPathParser.IndexContext ctx) {
                String name = ctx.getText();
                if (name.length() > 2) {
                    name = name.substring(1, name.length()-1);
                }
                parent = parent.index(unescape(name));
                return super.visitIndex(ctx);
            }

            @Override
            public PropertyPath visitProperty(@NotNull PropertyPathParser.PropertyContext ctx) {
                String name = ctx.getText();
                parent = parent.property(unescape(name));
                return super.visitProperty(ctx);
            }

            @Override
            protected PropertyPath defaultResult() {
                return parent;
            }

        });
    }

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
    
    public Iterator<SubPath> iterator() {
        return asList().iterator();
    }
    
    public List<SubPath> asList() {
        return fullPath != null ? fullPath : (fullPath = getFullPath());
    }
    
    public boolean isRoot() {
        return false;
    }
    
    public boolean startsWith(PropertyPath other) {
        if (other.isRoot()) {
            return true;
        } else if (this.isRoot()) {
            return false;
        } else {
            List<SubPath> otherPath = other.asList();
            List<SubPath> thisPath = asList();
            int otherSize = otherPath.size();
            return thisPath.size() >= otherSize && thisPath.get(otherSize - 1).equals(otherPath.get(otherSize - 1));
        }
    }
    
    public PropertyPath toSchemaPath() {
        PropertyPath schemaPath = ROOT;
        for (PropertyPath path : this) {
            schemaPath = path.normalize(schemaPath);
        }
        return schemaPath;
    }

    public abstract String toString();
    
    public abstract boolean equals(Object obj);
    
    public abstract int hashCode();
    
    public abstract String getName();
    

    abstract List<SubPath> getFullPath();
    
    abstract PropertyPath normalize(PropertyPath newParent);

    
    private volatile List<SubPath> fullPath;

    public static final class Root extends PropertyPath {

        private static final Root ROOT = new Root();
        
        private static final List<SubPath> FULL_PATH = ImmutableList.<SubPath>of();
        
        private Root() {}
        
        List<SubPath> getFullPath() {
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
        
        List<SubPath> getFullPath() {
            List<SubPath> parentPath = parent.getFullPath();
            ImmutableList.Builder<SubPath> pathBuilder = ImmutableList.builder();
            pathBuilder.addAll(parentPath);
            pathBuilder.add(this);
            return pathBuilder.build();
        }

    }
    
    private static void validate(String name) {
        if (name.length() > 0) {
            char firstChar = name.charAt(0);
            if (ESCAPED_CHARS.contains(firstChar) && firstChar != ESC) {
                throw new IllegalArgumentException("Illegal start of name: " + firstChar);
            }
        }
    }
    
    public static final class Property extends SubPath {

        public final String name;
        
        private Property(PropertyPath parent, String name) {
            super(parent);
            Check.notNullOrEmpty(name, "name");
            validate(name);
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
            return sb.append(escape(name)).toString();
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
            validate(index);
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
            return new StringBuilder().append(parent.toString()).append('[').append(escape(index)).append(']').toString();
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
    
    static String unescape(String str) {
        StringBuilder sb = new StringBuilder(str.length());
        boolean escape = false;
        for (int i=0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (!escape && ch == ESC) {
                escape = true;
            } else {
                sb.append(ch);
                escape = false;
            }
        }
        return sb.toString();
    }
    
    static String escape(String str) {
        StringBuilder sb = new StringBuilder(str.length() + 4);
        for (int i=0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ESCAPED_CHARS.contains(ch)) {
                sb.append(ESC);
            }
            sb.append(ch);
        }
        return sb.toString();
    }

}
