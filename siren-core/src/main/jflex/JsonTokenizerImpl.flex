/**
 * Copyright (c) 2009-2012 National University of Ireland, Galway. All Rights Reserved.
 *
 * Project and contact information: http://www.siren.sindice.com/
 *
 * This file is part of the SIREn project.
 *
 * SIREn is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SIREn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with SIREn. If not, see <http://www.gnu.org/licenses/>.
 */

package org.sindice.siren.analysis;

import static org.sindice.siren.analysis.JsonTokenizer.*;

import java.util.Stack;
import java.util.Arrays;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.IntsRef;
import org.sindice.siren.util.ArrayUtils;
import org.sindice.siren.util.XSDDatatype;

/**
 * Json document scanner
 *
 * @author Stephane Campinas [ 12 Jul 2012 ]
 */
%%
%yylexthrow java.lang.IllegalStateException
%public
%class JsonTokenizerImpl


/**
 * Both options cause the generated scanner to use the full 16 bit 
 * Unicode input character set (character codes 0-65535).
 */
%unicode

/**
 * The current character number with the variable yychar.
 */
%char

/**
 * Both cause the scanning method to be declared as of Java type int. 
 * Actions in the specification can then return int values as tokens
 */
%integer

/** 
 * causes JFlex to compress the generated DFA table and to store it 
 * in one or more string literals.
 */
%pack

%function getNextToken

%{

  /** Datatype representing xsd:string */
  private static final char[]   XSD_STRING   = XSDDatatype.XSD_STRING.toCharArray();

  /**
   * Datatype representing xsd:double
   * Any number is by default a double
   */
  private static final char[]   XSD_DOUBLE   = XSDDatatype.XSD_DOUBLE.toCharArray();

  /** Datatype representing xsd:boolean */
  private static final char[]   XSD_BOOLEAN  = XSDDatatype.XSD_BOOLEAN.toCharArray();

  private char[]                datatype;

  /** Buffer containing literal or number value */
  private final StringBuffer    buffer       = new StringBuffer();

  /** The size of the path buffer */
  private final static int      BUFFER_SIZE  = 1024;

  /** The path to a node */
  private final IntsRef         nodePath     = new IntsRef(BUFFER_SIZE);

  /** Stack of lexical states */
  private final Stack<Integer>  states       = new Stack();

  /**
   * Indicates if a leaf node, i.e., a literal, a number, null, or a boolean,
   * was encountered, in which case it needs to be closed, either in the COMMA
   * state, or in the closing curly bracket.
   */
  private boolean               openLeafNode = false;

  /**
   * Return the datatype URI.
   */
  public final char[] getDatatypeURI() {
    return datatype;
  }

  public final int yychar() {
    return yychar;
  }

  public String getNumber() {
    return buffer.toString();
  }

  /**
   * Fills Lucene TermAttribute with the current string buffer.
   */
  public final void getLiteralText(CharTermAttribute t) {
    char[] chars = new char[buffer.length()];
    buffer.getChars(0, buffer.length(), chars, 0);
    t.copyBuffer(chars, 0, chars.length);
  }

  public final IntsRef getNodePath() {
    return nodePath;
  }

  /**
   * Initialise inner variables
   */
  private void reset() {
    states.clear();
    Arrays.fill(nodePath.ints, -1);
    nodePath.offset = 0;
    nodePath.length = 1;
    openLeafNode = false;
    datatype = null;
  }

  /**
   * Add an object to the current node path
   */
  private void incrNodeObjectPath() {
    ArrayUtils.growAndCopy(nodePath, nodePath.length + 1);
    nodePath.length++;
    // initialise node
    setLastNode(-1);
  }

  private void decrNodeObjectPath() {
    nodePath.length--;
  }

  /** Update the path of the current values of the current object node */
  private void setLastNode(int val) {
    nodePath.ints[nodePath.length - 1] = val;
  }

  /** Update the path of the current values of the current object node */
  private void addToLastNode(int val) {
    nodePath.ints[nodePath.length - 1] += val;
  }

  private int processNumber() {
    datatype = XSD_DOUBLE;
    final String text = yytext();
    buffer.setLength(0);
    buffer.append(text.substring(text.indexOf(':') + 1).trim());
    return NUMBER;
  }

  private String errorMessage(String msg) {
    return "Error parsing JSON document at [line=" + yyline + ", column=" + yycolumn + "]: " + msg;
  }

%}

TRUE        = "true"
FALSE       = "false"
NUMBER      = -?[0-9][0-9]*(\.[0-9]+)?([e|E][+|-]?[0-9]+)?
NULL        = "null"
ENDOFLINE   = \r|\n|\r\n
WHITESPACE  = {ENDOFLINE} | [ \t\f]

%xstate sSTRING
%state sOBJECT
%state sARRAY

%%

{WHITESPACE}                     { /* ignore white space. */ }

<YYINITIAL> {
  "{"                            { reset();
                                   states.push(sOBJECT);
                                   yybegin(sOBJECT);
                                 }
}

<sOBJECT> {
  "}"                            { decrNodeObjectPath();
                                   if (openLeafNode) { // unclosed entry to a lead node
                                     decrNodeObjectPath();
                                     openLeafNode = false;
                                   }
                                   final int state = states.pop();
                                   if (state != sOBJECT) {
                                     throw new IllegalStateException(errorMessage("Expected '}', got " + yychar()));
                                   }
                                   yybegin(states.empty() ? YYINITIAL : states.peek());
                                 }
  ":"{WHITESPACE}*{NULL}         { datatype = XSD_STRING; openLeafNode = true; incrNodeObjectPath(); setLastNode(0); return NULL; }
  ":"{WHITESPACE}*{FALSE}        { datatype = XSD_BOOLEAN; openLeafNode = true; incrNodeObjectPath(); setLastNode(0); return FALSE; }
  ":"{WHITESPACE}*{TRUE}         { datatype = XSD_BOOLEAN; openLeafNode = true; incrNodeObjectPath(); setLastNode(0); return TRUE; }
  ":"{WHITESPACE}*{NUMBER}       { openLeafNode = true; incrNodeObjectPath(); setLastNode(0); return processNumber(); }
  ":"{WHITESPACE}*"["            { incrNodeObjectPath();
                                   yybegin(sARRAY);
                                   states.push(sARRAY);
                                 }
  ":"{WHITESPACE}*"{"            { incrNodeObjectPath();
                                   states.push(sOBJECT);
                                   yybegin(sOBJECT);
                                 }
  ":"{WHITESPACE}*\"             { openLeafNode = true;
                                   incrNodeObjectPath();
                                   setLastNode(0);
                                   buffer.setLength(0);
                                   yybegin(sSTRING);
                                 }
  ","                            { if (openLeafNode) {
                                     openLeafNode = false;
                                     decrNodeObjectPath();
                                   }
                                 }
  \"                             { addToLastNode(1);
                                   buffer.setLength(0);
                                   yybegin(sSTRING);
                                 }
  // Error state
  .                              { throw new IllegalStateException(errorMessage("Found bad character while in OBJECT state")); }
}

<sARRAY> {
  "]"                            { decrNodeObjectPath();
                                   int state = states.pop();
                                   if (state != sARRAY) {
                                     throw new IllegalStateException(errorMessage("Expected ']', got " + yychar()));
                                   }
                                   yybegin(states.peek());
                                 }
  "{"                            { addToLastNode(1);
                                   incrNodeObjectPath();
                                   states.push(sOBJECT);
                                   yybegin(sOBJECT);
                                 }
  "["                            { addToLastNode(1);
                                   incrNodeObjectPath();
                                   states.push(sARRAY);
                                   yybegin(sARRAY);
                                 }
  {NULL}                         { datatype = XSD_STRING; addToLastNode(1); return NULL; }
  {TRUE}                         { datatype = XSD_BOOLEAN; addToLastNode(1); return TRUE; }
  {FALSE}                        { datatype = XSD_BOOLEAN; addToLastNode(1); return FALSE; }
  {NUMBER}                       { addToLastNode(1); return processNumber(); }
  ","                            { /* nothing */ }
  \"                             { addToLastNode(1);
                                   buffer.setLength(0);
                                   yybegin(sSTRING);
                                 }
  // Error state
  .                              { throw new IllegalStateException(errorMessage("Found bad character while in ARRAY state")); }
}

<sSTRING> {
  \"                             { datatype = XSD_STRING; yybegin(states.peek()); return LITERAL; }
  [^\n\r\"\\]+                   { buffer.append(yytext()); }
  \\\"                           { buffer.append('\"'); }
  \\.                            { buffer.append(yytext()); }
  \\u[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F] { buffer.append(Character.toChars(Integer.parseInt(new String(zzBufferL, zzStartRead+2, zzMarkedPos - zzStartRead - 2 ), 16))); }
}

/* Check that the states are empty */
<<EOF>>                          { if (!states.empty()) {
                                     throw new IllegalStateException(errorMessage("Check that all arrays/objects/strings are closed"));
                                   }
                                   return YYEOF;
                                 }

// catch all: ignore them
.                                { /* ignore */ }
