// Generated from /Users/samppa/Personal/javersion/javersion-core/src/main/antlr/PropertyPath.g4 by ANTLR 4.5
package org.javersion.path.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.NotNull;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PropertyPathLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, Identifier=6, Integer=7, Key=8;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "Identifier", "JavaIdentifierStart",
		"JavaIdentifierPart", "Integer", "Digit", "NonZeroDigit", "Key", "StringCharacters",
		"StringCharacter", "EscapeSequence", "OctalEscape", "UnicodeEscape", "ZeroToThree",
		"HexDigit", "OctalDigit"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'.'", "'['", "']'", "'[]'", "'{}'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, "Identifier", "Integer", "Key"
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
		case 6:
			return JavaIdentifierStart_sempred((RuleContext)_localctx, predIndex);
		case 7:
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\n\u008d\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\6"+
		"\3\6\3\6\3\7\3\7\7\7:\n\7\f\7\16\7=\13\7\3\b\3\b\3\b\3\b\3\b\3\b\5\bE"+
		"\n\b\3\t\3\t\3\t\3\t\3\t\3\t\5\tM\n\t\3\n\3\n\3\n\7\nR\n\n\f\n\16\nU\13"+
		"\n\5\nW\n\n\3\13\3\13\5\13[\n\13\3\f\3\f\3\r\3\r\5\ra\n\r\3\r\3\r\3\16"+
		"\6\16f\n\16\r\16\16\16g\3\17\3\17\5\17l\n\17\3\20\3\20\3\20\3\20\5\20"+
		"r\n\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\5\21\177"+
		"\n\21\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25"+
		"\2\2\26\3\3\5\4\7\5\t\6\13\7\r\b\17\2\21\2\23\t\25\2\27\2\31\n\33\2\35"+
		"\2\37\2!\2#\2%\2\'\2)\2\3\2\16\6\2&&C\\aac|\4\2\2\u00a3\ud802\udc01\3"+
		"\2\ud802\udc01\3\2\udc02\ue001\7\2&&\62;C\\aac|\b\2&&\62;C\\aac|\ud802"+
		"\udc01\3\2\63;\4\2$$^^\n\2$$))^^ddhhppttvv\3\2\62\65\5\2\62;CHch\3\2\62"+
		"9\u008f\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2"+
		"\r\3\2\2\2\2\23\3\2\2\2\2\31\3\2\2\2\3+\3\2\2\2\5-\3\2\2\2\7/\3\2\2\2"+
		"\t\61\3\2\2\2\13\64\3\2\2\2\r\67\3\2\2\2\17D\3\2\2\2\21L\3\2\2\2\23V\3"+
		"\2\2\2\25Z\3\2\2\2\27\\\3\2\2\2\31^\3\2\2\2\33e\3\2\2\2\35k\3\2\2\2\37"+
		"q\3\2\2\2!~\3\2\2\2#\u0080\3\2\2\2%\u0087\3\2\2\2\'\u0089\3\2\2\2)\u008b"+
		"\3\2\2\2+,\7\60\2\2,\4\3\2\2\2-.\7]\2\2.\6\3\2\2\2/\60\7_\2\2\60\b\3\2"+
		"\2\2\61\62\7]\2\2\62\63\7_\2\2\63\n\3\2\2\2\64\65\7}\2\2\65\66\7\177\2"+
		"\2\66\f\3\2\2\2\67;\5\17\b\28:\5\21\t\298\3\2\2\2:=\3\2\2\2;9\3\2\2\2"+
		";<\3\2\2\2<\16\3\2\2\2=;\3\2\2\2>E\t\2\2\2?@\n\3\2\2@E\6\b\2\2AB\t\4\2"+
		"\2BC\t\5\2\2CE\6\b\3\2D>\3\2\2\2D?\3\2\2\2DA\3\2\2\2E\20\3\2\2\2FM\t\6"+
		"\2\2GH\n\7\2\2HM\6\t\4\2IJ\t\4\2\2JK\t\5\2\2KM\6\t\5\2LF\3\2\2\2LG\3\2"+
		"\2\2LI\3\2\2\2M\22\3\2\2\2NW\7\62\2\2OS\5\27\f\2PR\5\25\13\2QP\3\2\2\2"+
		"RU\3\2\2\2SQ\3\2\2\2ST\3\2\2\2TW\3\2\2\2US\3\2\2\2VN\3\2\2\2VO\3\2\2\2"+
		"W\24\3\2\2\2X[\7\62\2\2Y[\5\27\f\2ZX\3\2\2\2ZY\3\2\2\2[\26\3\2\2\2\\]"+
		"\t\b\2\2]\30\3\2\2\2^`\7$\2\2_a\5\33\16\2`_\3\2\2\2`a\3\2\2\2ab\3\2\2"+
		"\2bc\7$\2\2c\32\3\2\2\2df\5\35\17\2ed\3\2\2\2fg\3\2\2\2ge\3\2\2\2gh\3"+
		"\2\2\2h\34\3\2\2\2il\n\t\2\2jl\5\37\20\2ki\3\2\2\2kj\3\2\2\2l\36\3\2\2"+
		"\2mn\7^\2\2nr\t\n\2\2or\5!\21\2pr\5#\22\2qm\3\2\2\2qo\3\2\2\2qp\3\2\2"+
		"\2r \3\2\2\2st\7^\2\2t\177\5)\25\2uv\7^\2\2vw\5)\25\2wx\5)\25\2x\177\3"+
		"\2\2\2yz\7^\2\2z{\5%\23\2{|\5)\25\2|}\5)\25\2}\177\3\2\2\2~s\3\2\2\2~"+
		"u\3\2\2\2~y\3\2\2\2\177\"\3\2\2\2\u0080\u0081\7^\2\2\u0081\u0082\7w\2"+
		"\2\u0082\u0083\5\'\24\2\u0083\u0084\5\'\24\2\u0084\u0085\5\'\24\2\u0085"+
		"\u0086\5\'\24\2\u0086$\3\2\2\2\u0087\u0088\t\13\2\2\u0088&\3\2\2\2\u0089"+
		"\u008a\t\f\2\2\u008a(\3\2\2\2\u008b\u008c\t\r\2\2\u008c*\3\2\2\2\16\2"+
		";DLSVZ`gkq~\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}