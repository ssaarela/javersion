// Generated from /Users/samppa/Personal/javersion/javersion-core/src/main/antlr/PropertyPath.g4 by ANTLR 4.5
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
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, Identifier=7, Integer=8, 
		Key=9;
	public static final int
		RULE_parsePath = 0, RULE_parseProperty = 1, RULE_property = 2, RULE_indexedOrAny = 3, 
		RULE_index = 4, RULE_key = 5, RULE_anyIndex = 6, RULE_anyKey = 7, RULE_any = 8;
	public static final String[] ruleNames = {
		"parsePath", "parseProperty", "property", "indexedOrAny", "index", "key", 
		"anyIndex", "anyKey", "any"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'.'", "'['", "']'", "'[]'", "'{}'", "'*'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, "Identifier", "Integer", "Key"
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
			setState(20);
			switch (_input.LA(1)) {
			case Identifier:
				{
				setState(18); 
				property();
				}
				break;
			case T__1:
			case T__3:
			case T__4:
			case T__5:
				{
				setState(19); 
				indexedOrAny();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(27);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__3) | (1L << T__4) | (1L << T__5))) != 0)) {
				{
				setState(25);
				switch (_input.LA(1)) {
				case T__0:
					{
					setState(22); 
					match(T__0);
					setState(23); 
					property();
					}
					break;
				case T__1:
				case T__3:
				case T__4:
				case T__5:
					{
					setState(24); 
					indexedOrAny();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(29);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(30); 
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
			setState(32); 
			property();
			setState(33); 
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
		enterRule(_localctx, 4, RULE_property);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(35); 
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

	public static class IndexedOrAnyContext extends ParserRuleContext {
		public IndexContext index() {
			return getRuleContext(IndexContext.class,0);
		}
		public KeyContext key() {
			return getRuleContext(KeyContext.class,0);
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
		enterRule(_localctx, 6, RULE_indexedOrAny);
		try {
			setState(47);
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
				anyIndex();
				}
				break;
			case T__4:
				enterOuterAlt(_localctx, 3);
				{
				setState(45); 
				anyKey();
				}
				break;
			case T__5:
				enterOuterAlt(_localctx, 4);
				{
				setState(46); 
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
			setState(49); 
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
			setState(51); 
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
		enterRule(_localctx, 12, RULE_anyIndex);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53); 
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
		enterRule(_localctx, 14, RULE_anyKey);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(55); 
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
		enterRule(_localctx, 16, RULE_any);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57); 
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

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\13>\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\3\2\5\2"+
		"\27\n\2\3\2\3\2\3\2\7\2\34\n\2\f\2\16\2\37\13\2\3\2\3\2\3\3\3\3\3\3\3"+
		"\4\3\4\3\5\3\5\3\5\5\5+\n\5\3\5\3\5\3\5\3\5\3\5\5\5\62\n\5\3\6\3\6\3\7"+
		"\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\n\2\2\13\2\4\6\b\n\f\16\20\22\2\2;\2\26"+
		"\3\2\2\2\4\"\3\2\2\2\6%\3\2\2\2\b\61\3\2\2\2\n\63\3\2\2\2\f\65\3\2\2\2"+
		"\16\67\3\2\2\2\209\3\2\2\2\22;\3\2\2\2\24\27\5\6\4\2\25\27\5\b\5\2\26"+
		"\24\3\2\2\2\26\25\3\2\2\2\27\35\3\2\2\2\30\31\7\3\2\2\31\34\5\6\4\2\32"+
		"\34\5\b\5\2\33\30\3\2\2\2\33\32\3\2\2\2\34\37\3\2\2\2\35\33\3\2\2\2\35"+
		"\36\3\2\2\2\36 \3\2\2\2\37\35\3\2\2\2 !\7\2\2\3!\3\3\2\2\2\"#\5\6\4\2"+
		"#$\7\2\2\3$\5\3\2\2\2%&\7\t\2\2&\7\3\2\2\2\'*\7\4\2\2(+\5\n\6\2)+\5\f"+
		"\7\2*(\3\2\2\2*)\3\2\2\2+,\3\2\2\2,-\7\5\2\2-\62\3\2\2\2.\62\5\16\b\2"+
		"/\62\5\20\t\2\60\62\5\22\n\2\61\'\3\2\2\2\61.\3\2\2\2\61/\3\2\2\2\61\60"+
		"\3\2\2\2\62\t\3\2\2\2\63\64\7\n\2\2\64\13\3\2\2\2\65\66\7\13\2\2\66\r"+
		"\3\2\2\2\678\7\6\2\28\17\3\2\2\29:\7\7\2\2:\21\3\2\2\2;<\7\b\2\2<\23\3"+
		"\2\2\2\7\26\33\35*\61";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}