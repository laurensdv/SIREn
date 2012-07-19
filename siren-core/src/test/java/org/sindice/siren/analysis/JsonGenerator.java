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

import static org.sindice.siren.analysis.JsonTokenizer.FALSE;
import static org.sindice.siren.analysis.JsonTokenizer.LITERAL;
import static org.sindice.siren.analysis.JsonTokenizer.NULL;
import static org.sindice.siren.analysis.JsonTokenizer.NUMBER;
import static org.sindice.siren.analysis.JsonTokenizer.TRUE;
import static org.sindice.siren.analysis.JsonTokenizer.TOKEN_TYPES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

import org.apache.lucene.util.IntsRef;
import org.sindice.siren.util.ArrayUtils;
import org.sindice.siren.util.XSDDatatype;

public class JsonGenerator {

  public Random                   rand;
  public int                      valueType;

  // used for generating a random json document
  private final StringBuilder     sb          = new StringBuilder();
  private final Stack<Integer>    states      = new Stack<Integer>();
  private static final int        ARRAY       = 0;
  private static final int        OBJECT_ATT  = 1;
  private static final int        OBJECT_VAL  = 2;
  public final ArrayList<String>  images      = new ArrayList<String>();
  public final ArrayList<IntsRef> nodes       = new ArrayList<IntsRef>();
  public final ArrayList<Integer> incr        = new ArrayList<Integer>();
  public final ArrayList<String>  types       = new ArrayList<String>();
  public final ArrayList<String>  datatypes   = new ArrayList<String>();
  private final IntsRef           curNodePath = new IntsRef(1024);
  public boolean                  shouldFail  = false;
  private final int               MAX_DEPTH   = 50;

  public void setSeed(long seed) {
    rand = new Random(seed);
  }

  /**
   * Create a random Json document with random values
   * @param nbNodes
   */
  public String getRandomJson(int nbNodes) {
    // init
    sb.setLength(0);
    sb.append("{");
    states.clear();
    states.add(OBJECT_ATT);
    images.clear();
    nodes.clear();
    incr.clear();
    datatypes.clear();
    types.clear();
    curNodePath.length = 1;
    curNodePath.offset = 0;
    Arrays.fill(curNodePath.ints, -1);
    shouldFail = false;

    // <= so that when nbNodes == 1, the json is still valid
    /*
     * the generated json might be uncomplete, if states is not empty, and
     * the maximum number of nodes has been reached.
     */
    for (int i = 0; i <= nbNodes && !states.empty(); nbNodes++) {
      sb.append(getWhitespace()).append(getNextNode()).append(getWhitespace());
    }
    shouldFail = shouldFail ? true : !states.empty();
    return sb.toString();
  }

  /**
   * Return the next element of the json document
   */
  private String getNextNode() {
    final int popState;

    switch (states.peek()) {
      case ARRAY:
        switch (rand.nextInt(8)) {
          case 0:
            addToLastNode(1);
            nodes.add(IntsRef.deepCopyOf(curNodePath));
            images.add("stepha n e");
            types.add(TOKEN_TYPES[LITERAL]);
            incr.add(1);
            datatypes.add(XSDDatatype.XSD_STRING);
            return "\"stepha n e\"" + getWhitespace() + ",";
          case 1:
            addToLastNode(1);
            nodes.add(IntsRef.deepCopyOf(curNodePath));
            images.add("34.560e-9");
            types.add(TOKEN_TYPES[NUMBER]);
            incr.add(1);
            datatypes.add(XSDDatatype.XSD_DOUBLE);
            return "34.560e-9" + getWhitespace() + ",";
          case 2:
            addToLastNode(1);
            nodes.add(IntsRef.deepCopyOf(curNodePath));
            images.add("true");
            types.add(TOKEN_TYPES[TRUE]);
            incr.add(1);
            datatypes.add(XSDDatatype.XSD_BOOLEAN);
            return "true" + getWhitespace() + ",";
          case 3:
            addToLastNode(1);
            nodes.add(IntsRef.deepCopyOf(curNodePath));
            images.add("false");
            types.add(TOKEN_TYPES[FALSE]);
            incr.add(1);
            datatypes.add(XSDDatatype.XSD_BOOLEAN);
            return "false" + getWhitespace() + ",";
          case 4:
            addToLastNode(1);
            nodes.add(IntsRef.deepCopyOf(curNodePath));
            images.add("null");
            types.add(TOKEN_TYPES[NULL]);
            incr.add(1);
            datatypes.add(XSDDatatype.XSD_STRING);
            return "null" + getWhitespace() + ",";
          case 5:
            if (states.size() <= MAX_DEPTH) {
              addToLastNode(1);
              incrNodeObjectPath();
              states.add(ARRAY);
              return "[";
            }
            return "";
          case 6:
            if (states.size() <= MAX_DEPTH) {
              addToLastNode(1);
              incrNodeObjectPath();
              states.add(OBJECT_ATT);
              return "{";
            }
            return "";
          case 7:
            decrNodeObjectPath();
            popState = states.pop();
            if (popState != ARRAY) {
              shouldFail = true;
            }
            // Remove previous comma, this is not allowed
            final int comma = sb.lastIndexOf(",");
            if (comma != -1 && sb.substring(comma + 1).matches("\\s*")) {
              sb.deleteCharAt(comma);
            }
            return "],";
        }
      case OBJECT_ATT:
        switch (rand.nextInt(2)) {
          case 0:
            types.add(TOKEN_TYPES[LITERAL]);
            images.add("ste ph ane");
            incr.add(1);
            addToLastNode(1);
            nodes.add(IntsRef.deepCopyOf(curNodePath));
            datatypes.add(XSDDatatype.XSD_STRING);

            states.push(OBJECT_VAL);
            return "\"ste ph ane\"" + getWhitespace() + ":";
          case 1:
            decrNodeObjectPath();
            popState = states.pop();
            if (popState != OBJECT_ATT) {
              shouldFail = true;
            }
            return states.empty() ? "}" : "},";
        }
      case OBJECT_VAL:
        switch (rand.nextInt(7)) {
          case 0:
            return doValString("stepha n e");
          case 1:
            images.add("34.560e-9");
            types.add(TOKEN_TYPES[NUMBER]);
            incr.add(1);
            incrNodeObjectPath();
            setLastNode(0);
            nodes.add(IntsRef.deepCopyOf(curNodePath));
            decrNodeObjectPath();
            datatypes.add(XSDDatatype.XSD_DOUBLE);

            states.pop(); // remove OBJECT_VAL state
            return "34.560e-9" + getWhitespace() + ",";
          case 2:
            images.add("true");
            types.add(TOKEN_TYPES[TRUE]);
            incr.add(1);
            incrNodeObjectPath();
            setLastNode(0);
            nodes.add(IntsRef.deepCopyOf(curNodePath));
            decrNodeObjectPath();
            datatypes.add(XSDDatatype.XSD_BOOLEAN);

            states.pop(); // remove OBJECT_VAL state
            return "true" + getWhitespace() + ",";
          case 3:
            images.add("false");
            types.add(TOKEN_TYPES[FALSE]);
            incr.add(1);
            incrNodeObjectPath();
            setLastNode(0);
            nodes.add(IntsRef.deepCopyOf(curNodePath));
            decrNodeObjectPath();
            datatypes.add(XSDDatatype.XSD_BOOLEAN);

            states.pop(); // remove OBJECT_VAL state
            return "false" + getWhitespace() + ",";
          case 4:
            images.add("null");
            types.add(TOKEN_TYPES[NULL]);
            incr.add(1);
            incrNodeObjectPath();
            setLastNode(0);
            nodes.add(IntsRef.deepCopyOf(curNodePath));
            decrNodeObjectPath();
            datatypes.add(XSDDatatype.XSD_STRING);

            states.pop(); // remove OBJECT_VAL state
            return "null" + getWhitespace() + ",";
          case 5:
            if (states.size() <= MAX_DEPTH) {
              states.pop(); // remove OBJECT_VAL state
              incrNodeObjectPath();
              states.add(ARRAY);
              return "[";
            }
            return doValString("");
          case 6:
            if (states.size() <= MAX_DEPTH) {
              states.pop(); // remove OBJECT_VAL state
              incrNodeObjectPath();
              states.add(OBJECT_ATT);
              return "{";
            }
            return doValString("");
        }
      default:
        throw new IllegalStateException("Got unknown lexical state: " + states.peek());
    }
  }

  /**
   * Return a sequence of whitespace characters
   */
  private String getWhitespace() {
    final int nWS = rand.nextInt(5);
    String ws = "";

    for (int i = 0; i < nWS; i++) {
      switch (rand.nextInt(6)) {
        case 0:
          ws += " ";
          break;
        case 1:
          ws += "\t";
         break;
        case 2:
          ws += "\f";
          break;
        case 3:
          ws += "\r";
          break;
        case 4:
          ws += "\n";
          break;
        case 5:
          ws += "\r\n";
          break;
        default:
          break;
      }
    }
    return ws;
  }

  /**
   * Add an object/array to the current node path
   */
  private void incrNodeObjectPath() {
    ArrayUtils.growAndCopy(curNodePath, curNodePath.length + 1);
    curNodePath.length++;
    // initialise node
    setLastNode(-1);
  }

  /**
   * Remove an object/array from the node path
   */
  private void decrNodeObjectPath() {
    curNodePath.length--;
  }

  /** Update the path of the current values of the current object node */
  private void setLastNode(int val) {
    curNodePath.ints[curNodePath.length - 1] = val;
  }

  /** Update the path of the current values of the current object node */
  private void addToLastNode(int val) {
    curNodePath.ints[curNodePath.length - 1] += val;
  }

  /**
   * Add a string value to an object entry
   * @param val
   */
  private String doValString(String val) {
    images.add(val);
    types.add(TOKEN_TYPES[LITERAL]);
    incr.add(1);
    incrNodeObjectPath();
    setLastNode(0);
    nodes.add(IntsRef.deepCopyOf(curNodePath));
    decrNodeObjectPath();
    datatypes.add(XSDDatatype.XSD_STRING);

    states.pop(); // remove OBJECT_VAL state
    return "\"" + val + "\"" + getWhitespace() + ",";
  }

  /**
   * Returns a random value type
   */
  public String getRandomValue() {
    valueType = rand.nextInt(5);

    switch (valueType) {
      case FALSE:
        return "false";
      case LITERAL:
        return "\"stephane\"";
      case NULL:
        return "null";
      case NUMBER:
        return "324.90E-02";
      case TRUE:
        return "true";
      default:
        throw new IllegalArgumentException("No value for index=" + valueType);
    }
  }

}
