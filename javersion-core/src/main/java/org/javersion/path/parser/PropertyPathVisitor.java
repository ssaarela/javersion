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
package org.javersion.path.parser;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PropertyPathParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PropertyPathVisitor<T> extends ParseTreeVisitor<T> {
    /**
     * Visit a parse tree produced by {@link PropertyPathParser#index}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitIndex(@NotNull PropertyPathParser.IndexContext ctx);

    /**
     * Visit a parse tree produced by {@link PropertyPathParser#root}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitRoot(@NotNull PropertyPathParser.RootContext ctx);

    /**
     * Visit a parse tree produced by {@link PropertyPathParser#property}.
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitProperty(@NotNull PropertyPathParser.PropertyContext ctx);
}