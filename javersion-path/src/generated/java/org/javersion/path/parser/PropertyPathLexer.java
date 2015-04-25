// Generated from /Users/samppa/Personal/javersion/javersion-path/src/main/antlr/PropertyPath.g4 by ANTLR 4.5
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\f\u0096\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\3\2\3\2\3\3\3\3\3\4"+
		"\3\4\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\t\3\t\7\tC\n\t\f\t"+
		"\16\tF\13\t\3\n\3\n\3\n\3\n\3\n\3\n\5\nN\n\n\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\5\13V\n\13\3\f\3\f\3\f\7\f[\n\f\f\f\16\f^\13\f\5\f`\n\f\3\r\3\r"+
		"\5\rd\n\r\3\16\3\16\3\17\3\17\5\17j\n\17\3\17\3\17\3\20\6\20o\n\20\r\20"+
		"\16\20p\3\21\3\21\5\21u\n\21\3\22\3\22\3\22\3\22\5\22{\n\22\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\5\23\u0088\n\23\3\24\3\24"+
		"\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\2\2\30\3\3\5\4"+
		"\7\5\t\6\13\7\r\b\17\t\21\n\23\2\25\2\27\13\31\2\33\2\35\f\37\2!\2#\2"+
		"%\2\'\2)\2+\2-\2\3\2\16\6\2&&C\\aac|\4\2\2\u00a3\ud802\udc01\3\2\ud802"+
		"\udc01\3\2\udc02\ue001\7\2&&\62;C\\aac|\b\2&&\62;C\\aac|\ud802\udc01\3"+
		"\2\63;\4\2$$^^\n\2$$))^^ddhhppttvv\3\2\62\65\5\2\62;CHch\3\2\629\u0098"+
		"\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\27\3\2\2\2\2\35\3\2\2\2\3/\3\2\2\2\5"+
		"\61\3\2\2\2\7\63\3\2\2\2\t\65\3\2\2\2\138\3\2\2\2\r;\3\2\2\2\17>\3\2\2"+
		"\2\21@\3\2\2\2\23M\3\2\2\2\25U\3\2\2\2\27_\3\2\2\2\31c\3\2\2\2\33e\3\2"+
		"\2\2\35g\3\2\2\2\37n\3\2\2\2!t\3\2\2\2#z\3\2\2\2%\u0087\3\2\2\2\'\u0089"+
		"\3\2\2\2)\u0090\3\2\2\2+\u0092\3\2\2\2-\u0094\3\2\2\2/\60\7\60\2\2\60"+
		"\4\3\2\2\2\61\62\7]\2\2\62\6\3\2\2\2\63\64\7_\2\2\64\b\3\2\2\2\65\66\7"+
		"\60\2\2\66\67\7,\2\2\67\n\3\2\2\289\7]\2\29:\7_\2\2:\f\3\2\2\2;<\7}\2"+
		"\2<=\7\177\2\2=\16\3\2\2\2>?\7,\2\2?\20\3\2\2\2@D\5\23\n\2AC\5\25\13\2"+
		"BA\3\2\2\2CF\3\2\2\2DB\3\2\2\2DE\3\2\2\2E\22\3\2\2\2FD\3\2\2\2GN\t\2\2"+
		"\2HI\n\3\2\2IN\6\n\2\2JK\t\4\2\2KL\t\5\2\2LN\6\n\3\2MG\3\2\2\2MH\3\2\2"+
		"\2MJ\3\2\2\2N\24\3\2\2\2OV\t\6\2\2PQ\n\7\2\2QV\6\13\4\2RS\t\4\2\2ST\t"+
		"\5\2\2TV\6\13\5\2UO\3\2\2\2UP\3\2\2\2UR\3\2\2\2V\26\3\2\2\2W`\7\62\2\2"+
		"X\\\5\33\16\2Y[\5\31\r\2ZY\3\2\2\2[^\3\2\2\2\\Z\3\2\2\2\\]\3\2\2\2]`\3"+
		"\2\2\2^\\\3\2\2\2_W\3\2\2\2_X\3\2\2\2`\30\3\2\2\2ad\7\62\2\2bd\5\33\16"+
		"\2ca\3\2\2\2cb\3\2\2\2d\32\3\2\2\2ef\t\b\2\2f\34\3\2\2\2gi\7$\2\2hj\5"+
		"\37\20\2ih\3\2\2\2ij\3\2\2\2jk\3\2\2\2kl\7$\2\2l\36\3\2\2\2mo\5!\21\2"+
		"nm\3\2\2\2op\3\2\2\2pn\3\2\2\2pq\3\2\2\2q \3\2\2\2ru\n\t\2\2su\5#\22\2"+
		"tr\3\2\2\2ts\3\2\2\2u\"\3\2\2\2vw\7^\2\2w{\t\n\2\2x{\5%\23\2y{\5\'\24"+
		"\2zv\3\2\2\2zx\3\2\2\2zy\3\2\2\2{$\3\2\2\2|}\7^\2\2}\u0088\5-\27\2~\177"+
		"\7^\2\2\177\u0080\5-\27\2\u0080\u0081\5-\27\2\u0081\u0088\3\2\2\2\u0082"+
		"\u0083\7^\2\2\u0083\u0084\5)\25\2\u0084\u0085\5-\27\2\u0085\u0086\5-\27"+
		"\2\u0086\u0088\3\2\2\2\u0087|\3\2\2\2\u0087~\3\2\2\2\u0087\u0082\3\2\2"+
		"\2\u0088&\3\2\2\2\u0089\u008a\7^\2\2\u008a\u008b\7w\2\2\u008b\u008c\5"+
		"+\26\2\u008c\u008d\5+\26\2\u008d\u008e\5+\26\2\u008e\u008f\5+\26\2\u008f"+
		"(\3\2\2\2\u0090\u0091\t\13\2\2\u0091*\3\2\2\2\u0092\u0093\t\f\2\2\u0093"+
		",\3\2\2\2\u0094\u0095\t\r\2\2\u0095.\3\2\2\2\16\2DMU\\_ciptz\u0087\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}