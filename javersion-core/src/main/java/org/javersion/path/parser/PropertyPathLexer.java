// Generated from /Users/samppa/Personal/javersion/javersion-core/src/main/antlr/PropertyPath.g4 by ANTLR 4.5
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
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, Identifier=7, Integer=8, 
		Key=9;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "Identifier", "JavaIdentifierStart", 
		"JavaIdentifierPart", "Integer", "Digit", "NonZeroDigit", "Key", "StringCharacters", 
		"StringCharacter", "EscapeSequence", "OctalEscape", "UnicodeEscape", "ZeroToThree", 
		"HexDigit", "OctalDigit"
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
		case 7: 
			return JavaIdentifierStart_sempred((RuleContext)_localctx, predIndex);
		case 8: 
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\13\u0091\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\3\2\3\2\3\3\3\3\3\4\3\4"+
		"\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\b\3\b\7\b>\n\b\f\b\16\bA\13\b\3\t\3"+
		"\t\3\t\3\t\3\t\3\t\5\tI\n\t\3\n\3\n\3\n\3\n\3\n\3\n\5\nQ\n\n\3\13\3\13"+
		"\3\13\7\13V\n\13\f\13\16\13Y\13\13\5\13[\n\13\3\f\3\f\5\f_\n\f\3\r\3\r"+
		"\3\16\3\16\5\16e\n\16\3\16\3\16\3\17\6\17j\n\17\r\17\16\17k\3\20\3\20"+
		"\5\20p\n\20\3\21\3\21\3\21\3\21\5\21v\n\21\3\22\3\22\3\22\3\22\3\22\3"+
		"\22\3\22\3\22\3\22\3\22\3\22\5\22\u0083\n\22\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\2\2\27\3\3\5\4\7\5\t\6\13\7\r"+
		"\b\17\t\21\2\23\2\25\n\27\2\31\2\33\13\35\2\37\2!\2#\2%\2\'\2)\2+\2\3"+
		"\2\16\6\2&&C\\aac|\4\2\2\u00a3\ud802\udc01\3\2\ud802\udc01\3\2\udc02\ue001"+
		"\7\2&&\62;C\\aac|\b\2&&\62;C\\aac|\ud802\udc01\3\2\63;\4\2$$^^\n\2$$)"+
		")^^ddhhppttvv\3\2\62\65\5\2\62;CHch\3\2\629\u0093\2\3\3\2\2\2\2\5\3\2"+
		"\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\25"+
		"\3\2\2\2\2\33\3\2\2\2\3-\3\2\2\2\5/\3\2\2\2\7\61\3\2\2\2\t\63\3\2\2\2"+
		"\13\66\3\2\2\2\r9\3\2\2\2\17;\3\2\2\2\21H\3\2\2\2\23P\3\2\2\2\25Z\3\2"+
		"\2\2\27^\3\2\2\2\31`\3\2\2\2\33b\3\2\2\2\35i\3\2\2\2\37o\3\2\2\2!u\3\2"+
		"\2\2#\u0082\3\2\2\2%\u0084\3\2\2\2\'\u008b\3\2\2\2)\u008d\3\2\2\2+\u008f"+
		"\3\2\2\2-.\7\60\2\2.\4\3\2\2\2/\60\7]\2\2\60\6\3\2\2\2\61\62\7_\2\2\62"+
		"\b\3\2\2\2\63\64\7]\2\2\64\65\7_\2\2\65\n\3\2\2\2\66\67\7}\2\2\678\7\177"+
		"\2\28\f\3\2\2\29:\7,\2\2:\16\3\2\2\2;?\5\21\t\2<>\5\23\n\2=<\3\2\2\2>"+
		"A\3\2\2\2?=\3\2\2\2?@\3\2\2\2@\20\3\2\2\2A?\3\2\2\2BI\t\2\2\2CD\n\3\2"+
		"\2DI\6\t\2\2EF\t\4\2\2FG\t\5\2\2GI\6\t\3\2HB\3\2\2\2HC\3\2\2\2HE\3\2\2"+
		"\2I\22\3\2\2\2JQ\t\6\2\2KL\n\7\2\2LQ\6\n\4\2MN\t\4\2\2NO\t\5\2\2OQ\6\n"+
		"\5\2PJ\3\2\2\2PK\3\2\2\2PM\3\2\2\2Q\24\3\2\2\2R[\7\62\2\2SW\5\31\r\2T"+
		"V\5\27\f\2UT\3\2\2\2VY\3\2\2\2WU\3\2\2\2WX\3\2\2\2X[\3\2\2\2YW\3\2\2\2"+
		"ZR\3\2\2\2ZS\3\2\2\2[\26\3\2\2\2\\_\7\62\2\2]_\5\31\r\2^\\\3\2\2\2^]\3"+
		"\2\2\2_\30\3\2\2\2`a\t\b\2\2a\32\3\2\2\2bd\7$\2\2ce\5\35\17\2dc\3\2\2"+
		"\2de\3\2\2\2ef\3\2\2\2fg\7$\2\2g\34\3\2\2\2hj\5\37\20\2ih\3\2\2\2jk\3"+
		"\2\2\2ki\3\2\2\2kl\3\2\2\2l\36\3\2\2\2mp\n\t\2\2np\5!\21\2om\3\2\2\2o"+
		"n\3\2\2\2p \3\2\2\2qr\7^\2\2rv\t\n\2\2sv\5#\22\2tv\5%\23\2uq\3\2\2\2u"+
		"s\3\2\2\2ut\3\2\2\2v\"\3\2\2\2wx\7^\2\2x\u0083\5+\26\2yz\7^\2\2z{\5+\26"+
		"\2{|\5+\26\2|\u0083\3\2\2\2}~\7^\2\2~\177\5\'\24\2\177\u0080\5+\26\2\u0080"+
		"\u0081\5+\26\2\u0081\u0083\3\2\2\2\u0082w\3\2\2\2\u0082y\3\2\2\2\u0082"+
		"}\3\2\2\2\u0083$\3\2\2\2\u0084\u0085\7^\2\2\u0085\u0086\7w\2\2\u0086\u0087"+
		"\5)\25\2\u0087\u0088\5)\25\2\u0088\u0089\5)\25\2\u0089\u008a\5)\25\2\u008a"+
		"&\3\2\2\2\u008b\u008c\t\13\2\2\u008c(\3\2\2\2\u008d\u008e\t\f\2\2\u008e"+
		"*\3\2\2\2\u008f\u0090\t\r\2\2\u0090,\3\2\2\2\16\2?HPWZ^dkou\u0082\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}