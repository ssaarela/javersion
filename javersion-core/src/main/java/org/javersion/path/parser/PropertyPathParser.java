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
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
        new PredictionContextCache();
    public static final int
        T__2=1, T__1=2, T__0=3, NAME=4;
    public static final String[] tokenNames = {
        "<INVALID>", "']'", "'.'", "'['", "NAME"
    };
    public static final int
        RULE_root = 0, RULE_index = 1, RULE_property = 2;
    public static final String[] ruleNames = {
        "root", "index", "property"
    };

    @Override
    public String getGrammarFileName() { return "PropertyPath.g4"; }

    @Override
    public String[] getTokenNames() { return tokenNames; }

    @Override
    public String[] getRuleNames() { return ruleNames; }

    @Override
    public ATN getATN() { return _ATN; }

    public PropertyPathParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
    }
    public static class RootContext extends ParserRuleContext {
        public IndexContext index(int i) {
            return getRuleContext(IndexContext.class,i);
        }
        public PropertyContext property(int i) {
            return getRuleContext(PropertyContext.class,i);
        }
        public List<IndexContext> index() {
            return getRuleContexts(IndexContext.class);
        }
        public List<PropertyContext> property() {
            return getRuleContexts(PropertyContext.class);
        }
        public RootContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_root; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof PropertyPathVisitor ) return ((PropertyPathVisitor<? extends T>)visitor).visitRoot(this);
            else return visitor.visitChildren(this);
        }
    }

    public final RootContext root() throws RecognitionException {
        RootContext _localctx = new RootContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_root);
        int _la;
        try {
            setState(21);
            switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
            case 1:
                enterOuterAlt(_localctx, 1);
                {
                setState(8);
                switch (_input.LA(1)) {
                case NAME:
                    {
                    setState(6); property();
                    }
                    break;
                case 3:
                    {
                    setState(7); index();
                    }
                    break;
                case EOF:
                    break;
                default:
                    throw new NoViableAltException(this);
                }
                }
                break;

            case 2:
                enterOuterAlt(_localctx, 2);
                {
                setState(12);
                switch (_input.LA(1)) {
                case NAME:
                    {
                    setState(10); property();
                    }
                    break;
                case 3:
                    {
                    setState(11); index();
                    }
                    break;
                default:
                    throw new NoViableAltException(this);
                }
                setState(17); 
                _errHandler.sync(this);
                _la = _input.LA(1);
                do {
                    {
                    setState(17);
                    switch (_input.LA(1)) {
                    case 2:
                        {
                        setState(14); match(2);
                        setState(15); property();
                        }
                        break;
                    case 3:
                        {
                        setState(16); index();
                        }
                        break;
                    default:
                        throw new NoViableAltException(this);
                    }
                    }
                    setState(19); 
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                } while ( _la==2 || _la==3 );
                }
                break;
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
        public TerminalNode NAME() { return getToken(PropertyPathParser.NAME, 0); }
        public IndexContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_index; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof PropertyPathVisitor ) return ((PropertyPathVisitor<? extends T>)visitor).visitIndex(this);
            else return visitor.visitChildren(this);
        }
    }

    public final IndexContext index() throws RecognitionException {
        IndexContext _localctx = new IndexContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_index);
        try {
            setState(28);
            switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
            case 1:
                enterOuterAlt(_localctx, 1);
                {
                setState(23); match(3);
                setState(24); match(NAME);
                setState(25); match(1);
                }
                break;

            case 2:
                enterOuterAlt(_localctx, 2);
                {
                setState(26); match(3);
                setState(27); match(1);
                }
                break;
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
        public TerminalNode NAME() { return getToken(PropertyPathParser.NAME, 0); }
        public PropertyContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_property; }
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
            setState(30); match(NAME);
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
        "\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\3\6#\4\2\t\2\4\3\t"+
        "\3\4\4\t\4\3\2\3\2\5\2\13\n\2\3\2\3\2\5\2\17\n\2\3\2\3\2\3\2\6\2\24\n"+
        "\2\r\2\16\2\25\5\2\30\n\2\3\3\3\3\3\3\3\3\3\3\5\3\37\n\3\3\4\3\4\3\4\2"+
        "\5\2\4\6\2\2&\2\27\3\2\2\2\4\36\3\2\2\2\6 \3\2\2\2\b\13\5\6\4\2\t\13\5"+
        "\4\3\2\n\b\3\2\2\2\n\t\3\2\2\2\n\13\3\2\2\2\13\30\3\2\2\2\f\17\5\6\4\2"+
        "\r\17\5\4\3\2\16\f\3\2\2\2\16\r\3\2\2\2\17\23\3\2\2\2\20\21\7\4\2\2\21"+
        "\24\5\6\4\2\22\24\5\4\3\2\23\20\3\2\2\2\23\22\3\2\2\2\24\25\3\2\2\2\25"+
        "\23\3\2\2\2\25\26\3\2\2\2\26\30\3\2\2\2\27\n\3\2\2\2\27\16\3\2\2\2\30"+
        "\3\3\2\2\2\31\32\7\5\2\2\32\33\7\6\2\2\33\37\7\3\2\2\34\35\7\5\2\2\35"+
        "\37\7\3\2\2\36\31\3\2\2\2\36\34\3\2\2\2\37\5\3\2\2\2 !\7\6\2\2!\7\3\2"+
        "\2\2\b\n\16\23\25\27\36";
    public static final ATN _ATN =
        ATNSimulator.deserialize(_serializedATN.toCharArray());
    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}