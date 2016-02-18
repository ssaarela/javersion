// Generated from /Users/samppa/Personal/javersion/javersion-path/src/main/antlr/PropertyPath.g4 by ANTLR 4.5.1
package org.javersion.path.parser;
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
	T visitParsePath(PropertyPathParser.ParsePathContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#parseProperty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParseProperty(PropertyPathParser.ParsePropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#indexedOrAny}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexedOrAny(PropertyPathParser.IndexedOrAnyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#property}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty(PropertyPathParser.PropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex(PropertyPathParser.IndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey(PropertyPathParser.KeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#anyProperty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyProperty(PropertyPathParser.AnyPropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#anyIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyIndex(PropertyPathParser.AnyIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#anyKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyKey(PropertyPathParser.AnyKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertyPathParser#any}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAny(PropertyPathParser.AnyContext ctx);
}