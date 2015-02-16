// Generated from /Users/samppa/Personal/javersion/javersion-core/src/main/antlr/PropertyPath.g4 by ANTLR 4.5
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
	 * Visit a parse tree produced by {@link PropertyPathParser#parsePath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParsePath(@NotNull PropertyPathParser.ParsePathContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#parseProperty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParseProperty(@NotNull PropertyPathParser.ParsePropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#property}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty(@NotNull PropertyPathParser.PropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#indexed}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexed(@NotNull PropertyPathParser.IndexedContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex(@NotNull PropertyPathParser.IndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey(@NotNull PropertyPathParser.KeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#anyIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyIndex(@NotNull PropertyPathParser.AnyIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#anyKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyKey(@NotNull PropertyPathParser.AnyKeyContext ctx);
}