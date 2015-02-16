// Generated from /Users/samppa/Personal/javersion/javersion-core/src/main/antlr/PropertyPath.g4 by ANTLR 4.5
package org.javersion.path.parser;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PropertyPathParser}.
 */
public interface PropertyPathListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#parsePath}.
	 * @param ctx the parse tree
	 */
	void enterParsePath(@NotNull PropertyPathParser.ParsePathContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#parsePath}.
	 * @param ctx the parse tree
	 */
	void exitParsePath(@NotNull PropertyPathParser.ParsePathContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#parseProperty}.
	 * @param ctx the parse tree
	 */
	void enterParseProperty(@NotNull PropertyPathParser.ParsePropertyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#parseProperty}.
	 * @param ctx the parse tree
	 */
	void exitParseProperty(@NotNull PropertyPathParser.ParsePropertyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#property}.
	 * @param ctx the parse tree
	 */
	void enterProperty(@NotNull PropertyPathParser.PropertyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#property}.
	 * @param ctx the parse tree
	 */
	void exitProperty(@NotNull PropertyPathParser.PropertyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#indexed}.
	 * @param ctx the parse tree
	 */
	void enterIndexed(@NotNull PropertyPathParser.IndexedContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#indexed}.
	 * @param ctx the parse tree
	 */
	void exitIndexed(@NotNull PropertyPathParser.IndexedContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#index}.
	 * @param ctx the parse tree
	 */
	void enterIndex(@NotNull PropertyPathParser.IndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#index}.
	 * @param ctx the parse tree
	 */
	void exitIndex(@NotNull PropertyPathParser.IndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#key}.
	 * @param ctx the parse tree
	 */
	void enterKey(@NotNull PropertyPathParser.KeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#key}.
	 * @param ctx the parse tree
	 */
	void exitKey(@NotNull PropertyPathParser.KeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#anyIndex}.
	 * @param ctx the parse tree
	 */
	void enterAnyIndex(@NotNull PropertyPathParser.AnyIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#anyIndex}.
	 * @param ctx the parse tree
	 */
	void exitAnyIndex(@NotNull PropertyPathParser.AnyIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#anyKey}.
	 * @param ctx the parse tree
	 */
	void enterAnyKey(@NotNull PropertyPathParser.AnyKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#anyKey}.
	 * @param ctx the parse tree
	 */
	void exitAnyKey(@NotNull PropertyPathParser.AnyKeyContext ctx);
}