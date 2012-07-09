/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 * Scanner.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 *
 * Copied From Weka for the SIREn use case
 */

package org.sindice.siren.analysis;

import static org.sindice.siren.analysis.JsonTokenizer.*;

import java.util.Stack;
import java.util.Arrays;

import org.apache.lucene.util.IntsRef;

/**
 * A scanner for JSON data files.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 5786 $
 */
%%
%debug
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

  public static final String[] TOKEN_TYPES  = getTokenTypes();

  StringBuffer buffer                       = new StringBuffer();

  /** The size of the path buffer */
  private final static int BUFFER_SIZE      = 1024;
  /** Object path */
  private IntsRef nodeObjectPath            = new IntsRef(BUFFER_SIZE);
  /** Value path */
  private IntsRef nodeValuePath             = new IntsRef(BUFFER_SIZE * 2); // each object node can have attribute/value pairs
  /** Stack of lexical states */
  private final Stack<Integer> states       = new Stack();

  public final int yychar() {
    return yychar;
  }

  public double getNumber() {
    return Double.valueOf(buffer.toString());
  }

  /**
   * Return the current string buffer.
   */
  public final char[] getLiteralText() {
    char[] chars = new char[buffer.length()];
    buffer.getChars(0, buffer.length(), chars, 0);
    return chars;
  }

  public IntsRef getNodeObjectPath() {
    return nodeObjectPath;
  }

  public IntsRef getNodeValuePath() {
    return nodeValuePath;
  }

  private void reset() {
    states.clear();
    Arrays.fill(nodeValuePath.ints, -1);
    nodeValuePath.offset = 0;
    nodeValuePath.length = 2;
    Arrays.fill(nodeObjectPath.ints, -1);
    nodeObjectPath.offset = 0;
    nodeObjectPath.length = 0;
  }

  /**
   * Add an object to the current node path
   */
  private void incrNodeObjectPath() {
    nodeObjectPath.length++;
    nodeObjectPath.ints[nodeObjectPath.length - 1] += 1;
    Arrays.fill(nodeObjectPath.ints, nodeObjectPath.length, nodeObjectPath.ints.length, -1); // initialise children nodes
  }

  /** Called when entering a new object */
  private void incrNodeValuePath() {
    nodeValuePath.offset += 2;
    updateValueNode(-1, -1);
  }

  /** Update the path of the current values of the current object node */
  private void updateValueNode(int attId, int valId) {
    nodeValuePath.ints[nodeValuePath.offset] = attId;
    nodeValuePath.ints[nodeValuePath.offset + 1] = valId;
  }

  private int processNumber() {
    final String text = yytext();
    buffer.setLength(0);
    buffer.append(text.substring(text.indexOf(':') + 1));
    return NUMBER;
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

{WHITESPACE}                    { /* ignore white space. */ }

<YYINITIAL> {
  "{"                           { reset();
                                  incrNodeObjectPath();
                                  states.push(sOBJECT);
                                  yybegin(sOBJECT); }
}

<sOBJECT> {
  "}"                            { nodeObjectPath.length--;
                                   final int state = states.pop();
                                   if (state != sOBJECT) {
                                     throw new IllegalStateException("Error parsing JSON document: Expected '}', got " + yychar());
                                   }
                                   if (states.empty()) {
                                     yybegin(YYINITIAL);
                                   } else {
                                     nodeValuePath.offset -= 2;
                                     yybegin(states.peek());
                                   }
                                 }
  ":"{WHITESPACE}*{NULL}         { updateValueNode(nodeValuePath.ints[nodeValuePath.offset], 0); return NULL; }
  ":"{WHITESPACE}*{FALSE}        { updateValueNode(nodeValuePath.ints[nodeValuePath.offset], 0); return FALSE; }
  ":"{WHITESPACE}*{TRUE}         { updateValueNode(nodeValuePath.ints[nodeValuePath.offset], 0); return TRUE; }
  ":"{WHITESPACE}*{NUMBER}       { updateValueNode(nodeValuePath.ints[nodeValuePath.offset], 0); return processNumber(); }
  ":"{WHITESPACE}*"["            { yybegin(sARRAY); states.push(sARRAY); }
  ":"{WHITESPACE}*"{"            { incrNodeValuePath();
                                   incrNodeObjectPath();
                                   states.push(sOBJECT);
                                   yybegin(sOBJECT); }
  ":"{WHITESPACE}*\"             { updateValueNode(nodeValuePath.ints[nodeValuePath.offset], 0);
                                   buffer.setLength(0); yybegin(sSTRING); }
  ","                            { return COMMA; }
  {WHITESPACE}                   { /* ignore white space. */ }
  \"                             { updateValueNode(nodeValuePath.ints[nodeValuePath.offset] + 1, -1);
                                   buffer.setLength(0);
                                   yybegin(sSTRING); }
}

<sARRAY> {
  "]"                            { final int state = states.pop();
                                   if (state != sARRAY) {
                                     throw new IllegalStateException("Error parsing JSON document: Expected ']', got " + yychar());
                                   }
                                   if (states.peek() == sARRAY) {
                                     nodeObjectPath.length--; // nested array
                                   }
                                   yybegin(states.peek());
                                 }
  "{"                            { incrNodeValuePath();
                                   incrNodeObjectPath();
                                   states.push(sOBJECT);
                                   yybegin(sOBJECT); }
  "["                            { incrNodeValuePath();
                                   incrNodeObjectPath();
                                   states.push(sARRAY);
                                   yybegin(sARRAY);
                                 }
  {NULL}                         { updateValueNode(nodeValuePath.ints[nodeValuePath.offset], nodeValuePath.ints[nodeValuePath.offset + 1] + 1); return NULL; }
  {TRUE}                         { updateValueNode(nodeValuePath.ints[nodeValuePath.offset], nodeValuePath.ints[nodeValuePath.offset + 1] + 1); return TRUE; }
  {FALSE}                        { updateValueNode(nodeValuePath.ints[nodeValuePath.offset], nodeValuePath.ints[nodeValuePath.offset + 1] + 1); return FALSE; }
  {NUMBER}                       { updateValueNode(nodeValuePath.ints[nodeValuePath.offset], nodeValuePath.ints[nodeValuePath.offset + 1] + 1); return processNumber(); }
  ","                            { return COMMA; }
  \"                             { updateValueNode(nodeValuePath.ints[nodeValuePath.offset], nodeValuePath.ints[nodeValuePath.offset + 1] + 1);
                                   buffer.setLength(0);
                                   yybegin(sSTRING);
                                 }
}

<sSTRING> {
  \"                             { yybegin(states.peek()); return LITERAL; }
  [^\n\r\"\\]+                   { buffer.append(yytext()); }
  \\\"                           { buffer.append('\"'); }
  \\b                            { buffer.append('\b'); }
  \\f                            { buffer.append('\f'); }
  \\n                            { buffer.append('\n'); }
  \\r                            { buffer.append('\r'); }
  \\t                            { buffer.append('\t'); }
  \\                             { buffer.append('\\'); }
}

/* Check that the states are empty */
<<EOF>>                          { if (!states.empty()) {
                                     throw new IllegalStateException("Error parsing JSON document. Check that all arrays/objects/strings are closed.");
                                   }
                                   return YYEOF;
                                 }

// catch all: ignore them
.                                { /* ignore */ }