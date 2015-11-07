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
import org.javersion.path.NodeId.IndexId;
import org.javersion.path.NodeId.SpecialNodeId;
import org.javersion.path.PropertyPath.SubPath;
import org.javersion.path.parser.PropertyPathBaseVisitor;
import org.javersion.path.parser.PropertyPathLexer;
import org.javersion.path.parser.PropertyPathParser;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public abstract class PropertyPath implements Iterable<SubPath> {

    private static final class SilentParseException extends RuntimeException {
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

    public static final Root ROOT = new Root();

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
            public PropertyPath visitAnyProperty(@NotNull PropertyPathParser.AnyPropertyContext ctx) {
                return parent = new AnyProperty(parent);
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
            public PropertyPath visitAny(PropertyPathParser.AnyContext ctx) {
                return parent = new Any(parent);
            }

            @Override
            protected PropertyPath defaultResult() {
                return parent;
            }

        });
    }


    private transient List<SubPath> fullPath;

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

    public final Index index(long index) {
        checkArgument(index >= 0, "index should be >= 0");
        return new Index(this, index);
    }

    public final Key key(String index) {
        checkNotNull(index);
        return new Key(this, index);
    }

    public final SubPath keyOrIndex(Object object) {
        return keyOrIndex(NodeId.valueOf(object));
    }

    public final SubPath keyOrIndex(NodeId nodeId) {
        Preconditions.checkNotNull(nodeId);
        if (nodeId.isKey()) {
            return new Key(this, nodeId);
        } else if (nodeId.isIndex()) {
            return new Index(this, nodeId);
        } else {
            throw new IllegalArgumentException("Expected KeyId or IndexId, got " + nodeId.getClass());
        }
    }

    public final AnyProperty anyProperty() {
        return new AnyProperty(this);
    }

    public final AnyIndex anyIndex() {
        return new AnyIndex(this);
    }

    public final AnyKey anyKey() {
        return new AnyKey(this);
    }

    public final Any any() {
        return new Any(this);
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

    public final PropertyPath path(PropertyPath path) {
        PropertyPath result = this;
        for (SubPath subPath : path) {
            result = subPath.withParent(result);
        }
        return result;
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
        for (SubPath path : this) {
            schemaPath = path.toSchemaPath(schemaPath);
        }
        return schemaPath;
    }

    public abstract String toString();

    public abstract boolean equals(Object obj);

    public abstract int hashCode();

    public abstract NodeId getNodeId();


    abstract List<SubPath> getFullPath();

    abstract PropertyPath withParent(PropertyPath newParent);


    public static final class Root extends PropertyPath {

        public static final NodeId ID = new SpecialNodeId(EMPTY_STRING, null);

        private static final List<SubPath> FULL_PATH = ImmutableList.of();

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
        public NodeId getNodeId() {
            return ID;
        }

        @Override
        PropertyPath withParent(PropertyPath newParent) {
            return newParent;
        }
    }

    public static abstract class SubPath extends PropertyPath {

        public final NodeId nodeId;

        public final PropertyPath parent;

        SubPath(PropertyPath parent, NodeId nodeId) {
            this.parent = checkNotNull(parent, "parent");
            this.nodeId = checkNotNull(nodeId, "nodeId");
        }

        List<SubPath> getFullPath() {
            List<SubPath> parentPath = parent.getFullPath();
            ImmutableList.Builder<SubPath> pathBuilder = ImmutableList.builder();
            pathBuilder.addAll(parentPath);
            pathBuilder.add(this);
            return pathBuilder.build();
        }

        @Override
        public final boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof SubPath) {
                SubPath other = (SubPath) obj;
                return this.nodeId.equals(other.nodeId) && parent.equals(other.parent);
            } else {
                return false;
            }
        }

        @Override
        public final int hashCode() {
            return 31 * parent.hashCode() + nodeId.hashCode();
        }

        @Override
        public final NodeId getNodeId() {
            return nodeId;
        }

        public final String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(parent.toString());
            appendNode(sb);
            return sb.toString();
        }

        PropertyPath toSchemaPath(PropertyPath newParent) {
            return withParent(newParent);
        }

        protected abstract void appendNode(StringBuilder sb);
    }

    public static final class Property extends SubPath {

        Property(PropertyPath parent, String name) {
            super(parent, new NodeId.PropertyId(name));
        }

        Property(PropertyPath parent, NodeId.PropertyId nodeId) {
            super(parent, nodeId);
        }

        @Override
        Property toSchemaPath(PropertyPath newParent) {
            // NOTE: SchemaPath of a Property is itself - there's no changing index/key
            return withParent(newParent);
        }

        @Override
        Property withParent(PropertyPath newParent) {
            return newParent.equals(parent) ? this : new Property(newParent, (NodeId.PropertyId) nodeId);
        }

        @Override
        protected void appendNode(StringBuilder sb) {
            if (!parent.isRoot()) {
                sb.append('.');
            }
            sb.append(nodeId);
        }
    }

    public static final class Index extends SubPath {

        private Index(PropertyPath parent, long index) {
            super(parent, new IndexId(index));
        }

        private Index(PropertyPath parent, NodeId id) {
            super(parent, id);
        }

        @Override
        AnyIndex toSchemaPath(PropertyPath newParent) {
            return new AnyIndex(newParent);
        }

        @Override
        Index withParent(PropertyPath newParent) {
            return new Index(newParent, nodeId);
        }

        @Override
        protected void appendNode(StringBuilder sb) {
            sb.append('[').append(nodeId.getIndex()).append(']');
        }

    }

    public static final class Key extends SubPath {

        private Key(PropertyPath parent, String key) {
            super(parent, new NodeId.KeyId(key));
        }

        private Key(PropertyPath parent, NodeId id) {
            super(parent, id);
        }

        @Override
        AnyKey toSchemaPath(PropertyPath newParent) {
            return new AnyKey(newParent);
        }

        @Override
        SubPath withParent(PropertyPath newParent) {
            return new Key(newParent, nodeId);
        }

        @Override
        protected void appendNode(StringBuilder sb) {
            sb.append("[\"").append(escapeEcmaScript(nodeId.getKey())).append("\"]").toString();
        }

    }

    public static final class AnyProperty extends SubPath {

        private AnyProperty(PropertyPath parent) {
            super(parent, NodeId.ANY_PROPERTY);
        }

        @Override
        AnyProperty withParent(PropertyPath newParent) {
            return newParent.equals(parent) ? this : new AnyProperty(newParent);
        }

        @Override
        protected void appendNode(StringBuilder sb) {
            sb.append(NodeId.ANY_PROPERTY);
        }
    }

    public static final class AnyIndex extends SubPath {

        private AnyIndex(PropertyPath parent) {
            super(parent, NodeId.ANY_INDEX);
        }

        @Override
        AnyIndex withParent(PropertyPath newParent) {
            return newParent.equals(parent) ? this : new AnyIndex(newParent);
        }

        @Override
        protected void appendNode(StringBuilder sb) {
            sb.append(NodeId.ANY_INDEX);
        }
    }

    public static final class AnyKey extends SubPath {

        private AnyKey(PropertyPath parent) {
            super(parent, NodeId.ANY_KEY);
        }

        @Override
        AnyKey withParent(PropertyPath newParent) {
            return newParent.equals(parent) ? this : new AnyKey(newParent);
        }

        @Override
        protected void appendNode(StringBuilder sb) {
            sb.append(NodeId.ANY_KEY);
        }

    }

    public static final class Any extends SubPath {

        private Any(PropertyPath parent) {
            super(parent, NodeId.ANY);
        }

        @Override
        Any withParent(PropertyPath newParent) {
            return newParent.equals(parent) ? this : new Any(newParent);
        }

        @Override
        protected void appendNode(StringBuilder sb) {
            sb.append(NodeId.ANY);
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
