// Generated from /Users/samppa/Personal/javersion/javersion-path/src/main/antlr/PropertyPath.g4 by ANTLR 4.5.1
package org.javersion.path.parser;
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
	void enterParsePath(PropertyPathParser.ParsePathContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#parsePath}.
	 * @param ctx the parse tree
	 */
	void exitParsePath(PropertyPathParser.ParsePathContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#parseProperty}.
	 * @param ctx the parse tree
	 */
	void enterParseProperty(PropertyPathParser.ParsePropertyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#parseProperty}.
	 * @param ctx the parse tree
	 */
	void exitParseProperty(PropertyPathParser.ParsePropertyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#indexedOrAny}.
	 * @param ctx the parse tree
	 */
	void enterIndexedOrAny(PropertyPathParser.IndexedOrAnyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#indexedOrAny}.
	 * @param ctx the parse tree
	 */
	void exitIndexedOrAny(PropertyPathParser.IndexedOrAnyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#property}.
	 * @param ctx the parse tree
	 */
	void enterProperty(PropertyPathParser.PropertyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#property}.
	 * @param ctx the parse tree
	 */
	void exitProperty(PropertyPathParser.PropertyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#index}.
	 * @param ctx the parse tree
	 */
	void enterIndex(PropertyPathParser.IndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#index}.
	 * @param ctx the parse tree
	 */
	void exitIndex(PropertyPathParser.IndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#key}.
	 * @param ctx the parse tree
	 */
	void enterKey(PropertyPathParser.KeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#key}.
	 * @param ctx the parse tree
	 */
	void exitKey(PropertyPathParser.KeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#anyProperty}.
	 * @param ctx the parse tree
	 */
	void enterAnyProperty(PropertyPathParser.AnyPropertyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#anyProperty}.
	 * @param ctx the parse tree
	 */
	void exitAnyProperty(PropertyPathParser.AnyPropertyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#anyIndex}.
	 * @param ctx the parse tree
	 */
	void enterAnyIndex(PropertyPathParser.AnyIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#anyIndex}.
	 * @param ctx the parse tree
	 */
	void exitAnyIndex(PropertyPathParser.AnyIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#anyKey}.
	 * @param ctx the parse tree
	 */
	void enterAnyKey(PropertyPathParser.AnyKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#anyKey}.
	 * @param ctx the parse tree
	 */
	void exitAnyKey(PropertyPathParser.AnyKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertyPathParser#any}.
	 * @param ctx the parse tree
	 */
	void enterAny(PropertyPathParser.AnyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertyPathParser#any}.
	 * @param ctx the parse tree
	 */
	void exitAny(PropertyPathParser.AnyContext ctx);
}