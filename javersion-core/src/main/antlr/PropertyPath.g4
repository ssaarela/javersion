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
grammar PropertyPath;

parsePath
	: (property | indexed) ('.' property | indexed)* EOF
	;

parseProperty
	: property EOF
	;

property
	: Identifier
	;

indexed
	: '[' (index | key) ']'
	| anyIndex
	| anyKey
	;

index
	: Integer
	;

key
	: Key
	;

anyIndex
	: '[]'
	;

anyKey
	: '{}'
	;


Identifier
	: JavaIdentifierStart JavaIdentifierPart*
	;

fragment JavaIdentifierStart
	: [a-zA-Z$_]
	| ~[\u0000-\u00A1\uD800-\uDBFF] {Character.isJavaIdentifierStart(_input.LA(-1))}?
	| [\uD800-\uDBFF] [\uDC00-\uDFFF] {Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;

fragment JavaIdentifierPart
	: [a-zA-Z0-9$_]
	| ~[a-zA-Z0-9$_\uD800-\uDBFF] {Character.isJavaIdentifierPart(_input.LA(-1))}?
	| [\uD800-\uDBFF] [\uDC00-\uDFFF] {Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;


Integer
	: '0'
	| NonZeroDigit Digit*
	;

fragment Digit
	: '0'
	| NonZeroDigit
	;

fragment NonZeroDigit
	: [1-9]
	;


Key
	: '"' StringCharacters? '"'
	;

fragment StringCharacters
	: StringCharacter+
	;

fragment StringCharacter
	: ~["\\]
	| EscapeSequence
	;


fragment EscapeSequence
	: '\\' [btnfr"'\\]
	| OctalEscape
	| UnicodeEscape
	;

fragment OctalEscape
	: '\\' OctalDigit
	| '\\' OctalDigit OctalDigit
	| '\\' ZeroToThree OctalDigit OctalDigit
	;

fragment UnicodeEscape
	: '\\' 'u' HexDigit HexDigit HexDigit HexDigit
	;

fragment ZeroToThree
	: [0-3]
	;

fragment HexDigit
	: [0-9a-fA-F]
	;

fragment OctalDigit
	: [0-7]
	;
