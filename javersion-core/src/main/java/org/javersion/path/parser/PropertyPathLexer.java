/*
 * Copyright 2013 Samppa Saarela
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
        new PredictionContextCache();
    public static final int
        T__2=1, T__1=2, T__0=3, NAME=4;
    public static String[] modeNames = {
        "DEFAULT_MODE"
    };

    public static final String[] tokenNames = {
        "<INVALID>",
        "']'", "'.'", "'['", "NAME"
    };
    public static final String[] ruleNames = {
        "T__2", "T__1", "T__0", "NAME", "ESC"
    };


    public PropertyPathLexer(CharStream input) {
        super(input);
        _interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
    }

    @Override
    public String getGrammarFileName() { return "PropertyPath.g4"; }

    @Override
    public String[] getTokenNames() { return tokenNames; }

    @Override
    public String[] getRuleNames() { return ruleNames; }

    @Override
    public String[] getModeNames() { return modeNames; }

    @Override
    public ATN getATN() { return _ATN; }

    public static final String _serializedATN =
        "\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\6\34\b\1\4\2\t\2"+
        "\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\6\5\26"+
        "\n\5\r\5\16\5\27\3\6\3\6\3\6\2\7\3\3\1\5\4\1\7\5\1\t\6\1\13\2\1\3\2\3"+
        "\4\2\60\60]_\34\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\3\r\3"+
        "\2\2\2\5\17\3\2\2\2\7\21\3\2\2\2\t\25\3\2\2\2\13\31\3\2\2\2\r\16\7_\2"+
        "\2\16\4\3\2\2\2\17\20\7\60\2\2\20\6\3\2\2\2\21\22\7]\2\2\22\b\3\2\2\2"+
        "\23\26\5\13\6\2\24\26\n\2\2\2\25\23\3\2\2\2\25\24\3\2\2\2\26\27\3\2\2"+
        "\2\27\25\3\2\2\2\27\30\3\2\2\2\30\n\3\2\2\2\31\32\7^\2\2\32\33\t\2\2\2"+
        "\33\f\3\2\2\2\5\2\25\27";
    public static final ATN _ATN =
        ATNSimulator.deserialize(_serializedATN.toCharArray());
    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}