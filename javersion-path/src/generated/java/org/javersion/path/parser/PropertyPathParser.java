// Generated from /Users/samppa/Personal/javersion/javersion-path/src/main/antlr/PropertyPath.g4 by ANTLR 4.5
package org.javersion.path.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PropertyPathParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, Identifier=8, 
		Integer=9, Key=10;
	public static final int
		RULE_parsePath = 0, RULE_parseProperty = 1, RULE_indexedOrAny = 2, RULE_property = 3, 
		RULE_index = 4, RULE_key = 5, RULE_anyProperty = 6, RULE_anyIndex = 7, 
		RULE_anyKey = 8, RULE_any = 9;
	public static final String[] ruleNames = {
		"parsePath", "parseProperty", "indexedOrAny", "property", "index", "key", 
		"anyProperty", "anyIndex", "anyKey", "any"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'.'", "'['", "']'", "'.*'", "'[]'", "'{}'", "'*'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, "Identifier", "Integer", 
		"Key"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override
	@NotNull
	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "PropertyPath.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public PropertyPathParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ParsePathContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(PropertyPathParser.EOF, 0); }
		public List<PropertyContext> property() {
			return getRuleContexts(PropertyContext.class);
		}
		public PropertyContext property(int i) {
			return getRuleContext(PropertyContext.class,i);
		}
		public List<IndexedOrAnyContext> indexedOrAny() {
			return getRuleContexts(IndexedOrAnyContext.class);
		}
		public IndexedOrAnyContext indexedOrAny(int i) {
			return getRuleContext(IndexedOrAnyContext.class,i);
		}
		public ParsePathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parsePath; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).enterParsePath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).exitParsePath(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PropertyPathVisitor ) return ((PropertyPathVisitor<? extends T>)visitor).visitParsePath(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParsePathContext parsePath() throws RecognitionException {
		ParsePathContext _localctx = new ParsePathContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_parsePath);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(22);
			switch (_input.LA(1)) {
			case Identifier:
				{
				setState(20); 
				property();
				}
				break;
			case T__1:
			case T__3:
			case T__4:
			case T__5:
			case T__6:
				{
				setState(21); 
				indexedOrAny();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(29);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__6))) != 0)) {
				{
				setState(27);
				switch (_input.LA(1)) {
				case T__0:
					{
					setState(24); 
					match(T__0);
					setState(25); 
					property();
					}
					break;
				case T__1:
				case T__3:
				case T__4:
				case T__5:
				case T__6:
					{
					setState(26); 
					indexedOrAny();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(31);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(32); 
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParsePropertyContext extends ParserRuleContext {
		public PropertyContext property() {
			return getRuleContext(PropertyContext.class,0);
		}
		public TerminalNode EOF() { return getToken(PropertyPathParser.EOF, 0); }
		public ParsePropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parseProperty; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).enterParseProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).exitParseProperty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PropertyPathVisitor ) return ((PropertyPathVisitor<? extends T>)visitor).visitParseProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParsePropertyContext parseProperty() throws RecognitionException {
		ParsePropertyContext _localctx = new ParsePropertyContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_parseProperty);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(34); 
			property();
			setState(35); 
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IndexedOrAnyContext extends ParserRuleContext {
		public IndexContext index() {
			return getRuleContext(IndexContext.class,0);
		}
		public KeyContext key() {
			return getRuleContext(KeyContext.class,0);
		}
		public AnyPropertyContext anyProperty() {
			return getRuleContext(AnyPropertyContext.class,0);
		}
		public AnyIndexContext anyIndex() {
			return getRuleContext(AnyIndexContext.class,0);
		}
		public AnyKeyContext anyKey() {
			return getRuleContext(AnyKeyContext.class,0);
		}
		public AnyContext any() {
			return getRuleContext(AnyContext.class,0);
		}
		public IndexedOrAnyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexedOrAny; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).enterIndexedOrAny(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).exitIndexedOrAny(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PropertyPathVisitor ) return ((PropertyPathVisitor<? extends T>)visitor).visitIndexedOrAny(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexedOrAnyContext indexedOrAny() throws RecognitionException {
		IndexedOrAnyContext _localctx = new IndexedOrAnyContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_indexedOrAny);
		try {
			setState(48);
			switch (_input.LA(1)) {
			case T__1:
				enterOuterAlt(_localctx, 1);
				{
				setState(37); 
				match(T__1);
				setState(40);
				switch (_input.LA(1)) {
				case Integer:
					{
					setState(38); 
					index();
					}
					break;
				case Key:
					{
					setState(39); 
					key();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(42); 
				match(T__2);
				}
				break;
			case T__3:
				enterOuterAlt(_localctx, 2);
				{
				setState(44); 
				anyProperty();
				}
				break;
			case T__4:
				enterOuterAlt(_localctx, 3);
				{
				setState(45); 
				anyIndex();
				}
				break;
			case T__5:
				enterOuterAlt(_localctx, 4);
				{
				setState(46); 
				anyKey();
				}
				break;
			case T__6:
				enterOuterAlt(_localctx, 5);
				{
				setState(47); 
				any();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(PropertyPathParser.Identifier, 0); }
		public PropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_property; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).enterProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).exitProperty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PropertyPathVisitor ) return ((PropertyPathVisitor<? extends T>)visitor).visitProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyContext property() throws RecognitionException {
		PropertyContext _localctx = new PropertyContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_property);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50); 
			match(Identifier);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IndexContext extends ParserRuleContext {
		public TerminalNode Integer() { return getToken(PropertyPathParser.Integer, 0); }
		public IndexContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_index; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).enterIndex(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).exitIndex(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PropertyPathVisitor ) return ((PropertyPathVisitor<? extends T>)visitor).visitIndex(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexContext index() throws RecognitionException {
		IndexContext _localctx = new IndexContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_index);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(52); 
			match(Integer);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeyContext extends ParserRuleContext {
		public TerminalNode Key() { return getToken(PropertyPathParser.Key, 0); }
		public KeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_key; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).enterKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).exitKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PropertyPathVisitor ) return ((PropertyPathVisitor<? extends T>)visitor).visitKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeyContext key() throws RecognitionException {
		KeyContext _localctx = new KeyContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_key);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(54); 
			match(Key);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnyPropertyContext extends ParserRuleContext {
		public AnyPropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_anyProperty; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).enterAnyProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).exitAnyProperty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PropertyPathVisitor ) return ((PropertyPathVisitor<? extends T>)visitor).visitAnyProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnyPropertyContext anyProperty() throws RecognitionException {
		AnyPropertyContext _localctx = new AnyPropertyContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_anyProperty);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(56); 
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnyIndexContext extends ParserRuleContext {
		public AnyIndexContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_anyIndex; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).enterAnyIndex(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).exitAnyIndex(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PropertyPathVisitor ) return ((PropertyPathVisitor<? extends T>)visitor).visitAnyIndex(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnyIndexContext anyIndex() throws RecognitionException {
		AnyIndexContext _localctx = new AnyIndexContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_anyIndex);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(58); 
			match(T__4);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnyKeyContext extends ParserRuleContext {
		public AnyKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_anyKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).enterAnyKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).exitAnyKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PropertyPathVisitor ) return ((PropertyPathVisitor<? extends T>)visitor).visitAnyKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnyKeyContext anyKey() throws RecognitionException {
		AnyKeyContext _localctx = new AnyKeyContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_anyKey);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(60); 
			match(T__5);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnyContext extends ParserRuleContext {
		public AnyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_any; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).enterAny(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PropertyPathListener ) ((PropertyPathListener)listener).exitAny(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PropertyPathVisitor ) return ((PropertyPathVisitor<? extends T>)visitor).visitAny(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnyContext any() throws RecognitionException {
		AnyContext _localctx = new AnyContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_any);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(62); 
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\fC\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\3"+
		"\2\3\2\5\2\31\n\2\3\2\3\2\3\2\7\2\36\n\2\f\2\16\2!\13\2\3\2\3\2\3\3\3"+
		"\3\3\3\3\4\3\4\3\4\5\4+\n\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4\63\n\4\3\5\3\5"+
		"\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\13\2\2\f\2\4\6\b"+
		"\n\f\16\20\22\24\2\2@\2\30\3\2\2\2\4$\3\2\2\2\6\62\3\2\2\2\b\64\3\2\2"+
		"\2\n\66\3\2\2\2\f8\3\2\2\2\16:\3\2\2\2\20<\3\2\2\2\22>\3\2\2\2\24@\3\2"+
		"\2\2\26\31\5\b\5\2\27\31\5\6\4\2\30\26\3\2\2\2\30\27\3\2\2\2\31\37\3\2"+
		"\2\2\32\33\7\3\2\2\33\36\5\b\5\2\34\36\5\6\4\2\35\32\3\2\2\2\35\34\3\2"+
		"\2\2\36!\3\2\2\2\37\35\3\2\2\2\37 \3\2\2\2 \"\3\2\2\2!\37\3\2\2\2\"#\7"+
		"\2\2\3#\3\3\2\2\2$%\5\b\5\2%&\7\2\2\3&\5\3\2\2\2\'*\7\4\2\2(+\5\n\6\2"+
		")+\5\f\7\2*(\3\2\2\2*)\3\2\2\2+,\3\2\2\2,-\7\5\2\2-\63\3\2\2\2.\63\5\16"+
		"\b\2/\63\5\20\t\2\60\63\5\22\n\2\61\63\5\24\13\2\62\'\3\2\2\2\62.\3\2"+
		"\2\2\62/\3\2\2\2\62\60\3\2\2\2\62\61\3\2\2\2\63\7\3\2\2\2\64\65\7\n\2"+
		"\2\65\t\3\2\2\2\66\67\7\13\2\2\67\13\3\2\2\289\7\f\2\29\r\3\2\2\2:;\7"+
		"\6\2\2;\17\3\2\2\2<=\7\7\2\2=\21\3\2\2\2>?\7\b\2\2?\23\3\2\2\2@A\7\t\2"+
		"\2A\25\3\2\2\2\7\30\35\37*\62";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}