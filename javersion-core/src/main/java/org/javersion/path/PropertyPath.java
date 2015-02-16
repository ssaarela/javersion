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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringEscapeUtils.escapeEcmaScript;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeEcmaScript;

import java.util.Iterator;
import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.RuleNode;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.path.parser.PropertyPathBaseVisitor;
import org.javersion.path.parser.PropertyPathLexer;
import org.javersion.path.parser.PropertyPathParser;

import com.google.common.collect.ImmutableList;

public abstract class PropertyPath implements Iterable<SubPath> {

    private static class SilentParseException extends RuntimeException {
        public SilentParseException() {
            super(null, null, false, false);
        }
    }

    private static final ANTLRErrorListener BASIC_ERROR_LISTENER = new BaseErrorListener() {
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

    private static final ANTLRErrorListener SILENT_ERROR_LISTENER = new BaseErrorListener() {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                                Object offendingSymbol,
                                int line,
                                int charPositionInLine,
                                String msg,
                                RecognitionException e) {
            throw new SilentParseException();
        }
    };

    private static final String EMPTY_STRING = "";

    public static final Root ROOT = Root.ROOT;

    public static PropertyPath parse(String path) {
        checkNotNull(path);
        if (path.length() == 0) {
            return ROOT;
        }
        return newParser(path, false).parsePath().accept(new PropertyPathBaseVisitor<PropertyPath>() {

            private PropertyPath parent = ROOT;

            @Override
            public PropertyPath visitIndex(PropertyPathParser.IndexContext ctx) {
                return parent = new Index(parent, parseInt(ctx.getText()));
            }

            @Override
            public PropertyPath visitKey(PropertyPathParser.KeyContext ctx) {
                String keyLiteral = ctx.getText();
                return parent = new Key(parent, unescapeEcmaScript(keyLiteral.substring(1, keyLiteral.length() - 1)));
            }

            @Override
            public PropertyPath visitProperty(PropertyPathParser.PropertyContext ctx) {
                return parent = new Property(parent, ctx.getText());
            }

            @Override
            public PropertyPath visitAnyIndex(PropertyPathParser.AnyIndexContext ctx) {
                return parent = new AnyIndex(parent);
            }

            @Override
            public PropertyPath visitAnyKey(PropertyPathParser.AnyKeyContext ctx) {
                return parent = new AnyKey(parent);
            }

            @Override
            protected PropertyPath defaultResult() {
                return parent;
            }

        });
    }

    PropertyPath() {}

    public Property property(String name) {
        return newParser(name, false).parseProperty().accept(new PropertyPathBaseVisitor<Property>() {

            @Override
            public Property visitProperty(PropertyPathParser.PropertyContext ctx) {
                return new Property(PropertyPath.this, ctx.getText());
            }

            @Override
            protected boolean shouldVisitNextChild(RuleNode node, Property currentResult) {
                return currentResult == null;
            }

        });
    }

    public final Index index(int index) {
        checkArgument(index >= 0, "index should be >= 0");
        return new Index(this, index);
    }

    public final Key key(String index) {
        checkNotNull(index);
        return new Key(this, index);
    }

    public final AnyIndex anyIndex() {
        return new AnyIndex(this);
    }

    public final AnyKey anyKey() {
        return new AnyKey(this);
    }

    public final SubPath propertyOrKey(String string) {
        checkNotNull(string);
        try {
            return newParser(string, true).parseProperty().accept(new PropertyPathBaseVisitor<SubPath>() {

                @Override
                public SubPath visitProperty(PropertyPathParser.PropertyContext ctx) {
                    return new Property(PropertyPath.this, ctx.getText());
                }

                @Override
                protected boolean shouldVisitNextChild(RuleNode node, SubPath currentResult) {
                    return currentResult == null;
                }

            });
        } catch (SilentParseException e) {
            return new Key(this, string);
        }
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
            schemaPath = path.toSchemaPath(schemaPath);
        }
        return schemaPath;
    }

    public abstract String toString();

    public abstract boolean equals(Object obj);

    public abstract int hashCode();

    public abstract String getName();


    abstract List<SubPath> getFullPath();

    abstract PropertyPath toSchemaPath(PropertyPath newParent);


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
        Root toSchemaPath(PropertyPath newParent) {
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

    public static final class Property extends SubPath {

        public final String name;

        private Property(PropertyPath parent, String name) {
            super(parent);
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
            StringBuilder sb = new StringBuilder();
            if (!parent.isRoot()) {
                sb.append(parent.toString());
                sb.append('.');
            }
            return sb.append(name).toString();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        Property toSchemaPath(PropertyPath newParent) {
            return parent.equals(newParent) ? this : new Property(newParent, name);
        }

    }

    public static final class AnyIndex extends SubPath {

        public static final String ID = "[]";

        private AnyIndex(PropertyPath parent) {
            super(parent);
        }

        @Override
        public String toString() {
            return new StringBuilder().append(parent.toString()).append(ID).toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof AnyIndex) {
                AnyIndex other = (AnyIndex) obj;
                return parent.equals(other.parent);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return 31 * parent.hashCode() + -1;
        }

        @Override
        public String getName() {
            return ID;
        }

        @Override
        PropertyPath toSchemaPath(PropertyPath newParent) {
            return parent.equals(newParent) ? this : new AnyKey(newParent);
        }
    }

    public static final class AnyKey extends SubPath {

        public static final String ID = "{}";

        private AnyKey(PropertyPath parent) {
            super(parent);
        }

        @Override
        public String toString() {
            return new StringBuilder().append(parent.toString()).append(ID).toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof AnyKey) {
                AnyKey other = (AnyKey) obj;
                return parent.equals(other.parent);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return 31 * parent.hashCode() + -2;
        }

        @Override
        public String getName() {
            return ID;
        }

        @Override
        AnyKey toSchemaPath(PropertyPath newParent) {
            return parent.equals(newParent) ? this : new AnyKey(newParent);
        }
    }

    public static final class Index extends SubPath {

        public final int index;

        private Index(PropertyPath parent, int index) {
            super(parent);
            checkArgument(index >= 0, "index should be >= 0");
            this.index = index;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Index) {
                Index other = (Index) obj;
                return this.index == other.index && parent.equals(other.parent);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return 31 * parent.hashCode() + index;
        }

        @Override
        public String toString() {
            return new StringBuilder().append(parent.toString()).append('[').append(index).append(']').toString();
        }

        @Override
        public String getName() {
            return Integer.toString(index);
        }

        @Override
        SubPath toSchemaPath(PropertyPath newParent) {
            return new AnyIndex(newParent);
        }

    }

    public static final class Key extends SubPath {

        public final String key;

        private Key(PropertyPath parent, String key) {
            super(parent);
            this.key = key;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Key) {
                Key other = (Key) obj;
                return this.key.equals(other.key) && parent.equals(other.parent);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return 31 * parent.hashCode() + key.hashCode();
        }

        @Override
        public String toString() {
            return new StringBuilder().append(parent.toString()).append("[\"").append(escapeEcmaScript(key)).append("\"]").toString();
        }

        @Override
        public String getName() {
            return key;
        }

        @Override
        SubPath toSchemaPath(PropertyPath newParent) {
            return new AnyKey(newParent);
        }
    }

    private static PropertyPathParser newParser(String input, boolean silent) {
        PropertyPathLexer lexer = new PropertyPathLexer(new ANTLRInputStream(input));
        lexer.removeErrorListeners();
        lexer.addErrorListener(silent ? SILENT_ERROR_LISTENER: BASIC_ERROR_LISTENER);
        PropertyPathParser parser = new PropertyPathParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(silent ? SILENT_ERROR_LISTENER: BASIC_ERROR_LISTENER);
        return parser;
    }

}
