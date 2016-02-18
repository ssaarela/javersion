// Generated from /Users/samppa/Personal/javersion/javersion-path/src/main/antlr/PropertyPath.g4 by ANTLR 4.5.1
package org.javersion.path.parser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PropertyPathLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, Identifier=8, 
		Integer=9, Key=10;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "Identifier", 
		"JavaIdentifierStart", "JavaIdentifierPart", "Integer", "Digit", "NonZeroDigit", 
		"Key", "StringCharacters", "StringCharacter", "EscapeSequence", "OctalEscape", 
		"UnicodeEscape", "ZeroToThree", "HexDigit", "OctalDigit"
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

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public PropertyPathLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "PropertyPath.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 8:
			return JavaIdentifierStart_sempred((RuleContext)_localctx, predIndex);
		case 9:
			return JavaIdentifierPart_sempred((RuleContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean JavaIdentifierStart_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return Character.isJavaIdentifierStart(_input.LA(-1));
		case 1:
			return Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)));
		}
		return true;
	}
	private boolean JavaIdentifierPart_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return Character.isJavaIdentifierPart(_input.LA(-1));
		case 3:
			return Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)));
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\f\u0099\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\3\2\3\2\3\3\3\3\3\4"+
		"\3\4\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\t\3\t\7\tC\n\t\f\t"+
		"\16\tF\13\t\3\n\3\n\3\n\3\n\3\n\3\n\5\nN\n\n\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\5\13V\n\13\3\f\3\f\5\fZ\n\f\3\f\3\f\7\f^\n\f\f\f\16\fa\13\f\5\f"+
		"c\n\f\3\r\3\r\5\rg\n\r\3\16\3\16\3\17\3\17\5\17m\n\17\3\17\3\17\3\20\6"+
		"\20r\n\20\r\20\16\20s\3\21\3\21\5\21x\n\21\3\22\3\22\3\22\3\22\5\22~\n"+
		"\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\5\23\u008b"+
		"\n\23\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27"+
		"\2\2\30\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\2\25\2\27\13\31\2\33\2\35"+
		"\f\37\2!\2#\2%\2\'\2)\2+\2-\2\3\2\16\6\2&&C\\aac|\4\2\2\u00a3\ud802\udc01"+
		"\3\2\ud802\udc01\3\2\udc02\ue001\7\2&&\62;C\\aac|\b\2&&\62;C\\aac|\ud802"+
		"\udc01\3\2\63;\4\2$$^^\13\2$$))\61\61^^ddhhppttvv\3\2\62\65\5\2\62;CH"+
		"ch\3\2\629\u009c\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13"+
		"\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\27\3\2\2\2\2\35\3\2\2"+
		"\2\3/\3\2\2\2\5\61\3\2\2\2\7\63\3\2\2\2\t\65\3\2\2\2\138\3\2\2\2\r;\3"+
		"\2\2\2\17>\3\2\2\2\21@\3\2\2\2\23M\3\2\2\2\25U\3\2\2\2\27b\3\2\2\2\31"+
		"f\3\2\2\2\33h\3\2\2\2\35j\3\2\2\2\37q\3\2\2\2!w\3\2\2\2#}\3\2\2\2%\u008a"+
		"\3\2\2\2\'\u008c\3\2\2\2)\u0093\3\2\2\2+\u0095\3\2\2\2-\u0097\3\2\2\2"+
		"/\60\7\60\2\2\60\4\3\2\2\2\61\62\7]\2\2\62\6\3\2\2\2\63\64\7_\2\2\64\b"+
		"\3\2\2\2\65\66\7\60\2\2\66\67\7,\2\2\67\n\3\2\2\289\7]\2\29:\7_\2\2:\f"+
		"\3\2\2\2;<\7}\2\2<=\7\177\2\2=\16\3\2\2\2>?\7,\2\2?\20\3\2\2\2@D\5\23"+
		"\n\2AC\5\25\13\2BA\3\2\2\2CF\3\2\2\2DB\3\2\2\2DE\3\2\2\2E\22\3\2\2\2F"+
		"D\3\2\2\2GN\t\2\2\2HI\n\3\2\2IN\6\n\2\2JK\t\4\2\2KL\t\5\2\2LN\6\n\3\2"+
		"MG\3\2\2\2MH\3\2\2\2MJ\3\2\2\2N\24\3\2\2\2OV\t\6\2\2PQ\n\7\2\2QV\6\13"+
		"\4\2RS\t\4\2\2ST\t\5\2\2TV\6\13\5\2UO\3\2\2\2UP\3\2\2\2UR\3\2\2\2V\26"+
		"\3\2\2\2Wc\7\62\2\2XZ\7/\2\2YX\3\2\2\2YZ\3\2\2\2Z[\3\2\2\2[_\5\33\16\2"+
		"\\^\5\31\r\2]\\\3\2\2\2^a\3\2\2\2_]\3\2\2\2_`\3\2\2\2`c\3\2\2\2a_\3\2"+
		"\2\2bW\3\2\2\2bY\3\2\2\2c\30\3\2\2\2dg\7\62\2\2eg\5\33\16\2fd\3\2\2\2"+
		"fe\3\2\2\2g\32\3\2\2\2hi\t\b\2\2i\34\3\2\2\2jl\7$\2\2km\5\37\20\2lk\3"+
		"\2\2\2lm\3\2\2\2mn\3\2\2\2no\7$\2\2o\36\3\2\2\2pr\5!\21\2qp\3\2\2\2rs"+
		"\3\2\2\2sq\3\2\2\2st\3\2\2\2t \3\2\2\2ux\n\t\2\2vx\5#\22\2wu\3\2\2\2w"+
		"v\3\2\2\2x\"\3\2\2\2yz\7^\2\2z~\t\n\2\2{~\5%\23\2|~\5\'\24\2}y\3\2\2\2"+
		"}{\3\2\2\2}|\3\2\2\2~$\3\2\2\2\177\u0080\7^\2\2\u0080\u008b\5-\27\2\u0081"+
		"\u0082\7^\2\2\u0082\u0083\5-\27\2\u0083\u0084\5-\27\2\u0084\u008b\3\2"+
		"\2\2\u0085\u0086\7^\2\2\u0086\u0087\5)\25\2\u0087\u0088\5-\27\2\u0088"+
		"\u0089\5-\27\2\u0089\u008b\3\2\2\2\u008a\177\3\2\2\2\u008a\u0081\3\2\2"+
		"\2\u008a\u0085\3\2\2\2\u008b&\3\2\2\2\u008c\u008d\7^\2\2\u008d\u008e\7"+
		"w\2\2\u008e\u008f\5+\26\2\u008f\u0090\5+\26\2\u0090\u0091\5+\26\2\u0091"+
		"\u0092\5+\26\2\u0092(\3\2\2\2\u0093\u0094\t\13\2\2\u0094*\3\2\2\2\u0095"+
		"\u0096\t\f\2\2\u0096,\3\2\2\2\u0097\u0098\t\r\2\2\u0098.\3\2\2\2\17\2"+
		"DMUY_bflsw}\u008a\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}