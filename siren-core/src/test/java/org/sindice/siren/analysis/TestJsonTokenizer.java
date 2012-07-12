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

import static org.junit.Assert.fail;
import static org.sindice.siren.analysis.JsonTokenizer.FALSE;
import static org.sindice.siren.analysis.JsonTokenizer.LITERAL;
import static org.sindice.siren.analysis.JsonTokenizer.NULL;
import static org.sindice.siren.analysis.JsonTokenizer.NUMBER;
import static org.sindice.siren.analysis.JsonTokenizer.TOKEN_TYPES;
import static org.sindice.siren.analysis.JsonTokenizer.TRUE;
import static org.sindice.siren.analysis.MockSirenToken.node;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.lucene.util.IntsRef;
import org.junit.Before;
import org.junit.Test;
import org.sindice.siren.util.XSDDatatype;

public class TestJsonTokenizer
extends TokenizerHelper {

  private final JsonTokenizer _t      = new JsonTokenizer(new StringReader(""), Integer.MAX_VALUE);

  private final Random        seed    = new Random();
  private final JsonGenerator jsonGen = new JsonGenerator();

  @Before
  public void setUp() {
    final long testseed = seed.nextLong();
    jsonGen.setSeed(testseed);
    System.err.println("TestJsonTokenizer: seed=[" + testseed + "]");
  }

  @Test
  public void testNodePathBufferOverflow()
  throws Exception {
    final int size = 1030;
    final StringBuilder sb = new StringBuilder("{");
    final String[] images = new String[size + 1];
    final String[] types = new String[size + 1];

    for (int i = 0; i < size; i++) {
      images[i] = "o" + i;
      types[i] = TOKEN_TYPES[LITERAL];
    }
    images[size] = "true";
    types[size] = TOKEN_TYPES[TRUE];

    // Creates nested objects
    for (int i = 0; i < size; i++) {
      sb.append("{\"o").append(i).append("\":");
    }
    sb.append("true");
    // Close nested objects
    for (int i = 0; i < size; i++) {
      sb.append("}");
    }
    sb.append("}");
    this.assertTokenizesTo(_t, sb.toString(), images, types);
  }

  @Test
  public void testEmptyJson()
  throws Exception {
    this.assertTokenizesTo(_t, "{}", new String[] {}, new String[] {});
  }

  @Test
  public void testEmptyArray()
  throws Exception {
    this.assertTokenizesTo(_t, "{\"a\":[]}", new String[] { "a" },
      new String[] { TOKEN_TYPES[LITERAL] });
    // nested empty array
    this.assertTokenizesTo(_t, "{\"a\":[ false, [], true ]}", new String[] { "a", "false", "true" },
      new String[] { TOKEN_TYPES[LITERAL], TOKEN_TYPES[FALSE], TOKEN_TYPES[TRUE] },
      new String[] { XSDDatatype.XSD_STRING, XSDDatatype.XSD_BOOLEAN, XSDDatatype.XSD_BOOLEAN },
      new int[] { 1, 1, 1 },
      new IntsRef[] { node(0), node(0, 0), node(0, 2) });
  }

  @Test
  public void testValues()
  throws Exception {
    this.assertTokenizesTo(_t, "{\"v0\":\"stephane\",\"v1\":12.3e-9,\"v2\":true,\"v3\":false,\"v4\":null}",
      new String[] { "v0", "stephane", "v1", "12.3e-9", "v2", "true", "v3", "false", "v4", "null" },
      new String[] { TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[NUMBER],
                     TOKEN_TYPES[LITERAL], TOKEN_TYPES[TRUE], TOKEN_TYPES[LITERAL], TOKEN_TYPES[FALSE],
                     TOKEN_TYPES[LITERAL], TOKEN_TYPES[NULL] },
      new String[] { XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING, XSDDatatype.XSD_DOUBLE,
                     XSDDatatype.XSD_STRING, XSDDatatype.XSD_BOOLEAN, XSDDatatype.XSD_STRING, XSDDatatype.XSD_BOOLEAN,
                     XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING },
      new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
      new IntsRef[] { node(0), node(0, 0), node(1), node(1, 0),
                      node(2), node(2, 0), node(3), node(3, 0), 
                      node(4), node(4, 0) });
  }

  @Test
  public void testArray()
  throws Exception {
    final ArrayList<String> images = new ArrayList<String>() {{
      add("array");
    }};
    final ArrayList<IntsRef> nodes = new ArrayList<IntsRef>() {{
      add(node(0));
    }};
    final ArrayList<String> types = new ArrayList<String>() {{
      add(TOKEN_TYPES[LITERAL]);
    }};

    final int arraySize = jsonGen.rand.nextInt(100);
    String array = "[";
    for (int i = 0; i < arraySize; i++) {
      final String v = jsonGen.getRandomValue();
      array += v + ",";
      images.add(jsonGen.valueType == LITERAL ? v.substring(1, v.length() - 1) : v);
      nodes.add(node(0, i));
      types.add(TOKEN_TYPES[jsonGen.valueType]);
    }
    array += "]";

    final int[] posIncr = new int[images.size()];
    Arrays.fill(posIncr, 1);
    this.assertTokenizesTo(_t, "{\"array\":" + array + "}",
      images.toArray(new String[0]), types.toArray(new String[0]),
      posIncr, nodes.toArray(new IntsRef[0]));
  }

  @Test
  public void testObjects()
  throws Exception {
    this.assertTokenizesTo(_t, "{\"a0\":[{\"t1\":1},{\"t2\":2}],\"a1\":{\"t3\":3}}",
      new String[] { "a0", "t1", "1", "t2", "2", "a1", "t3", "3" },
      new String[] { TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[NUMBER], TOKEN_TYPES[LITERAL],
                     TOKEN_TYPES[NUMBER], TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[NUMBER] },
      new String[] { XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING, XSDDatatype.XSD_DOUBLE, XSDDatatype.XSD_STRING,
                     XSDDatatype.XSD_DOUBLE, XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING, XSDDatatype.XSD_DOUBLE },
      new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
      new IntsRef[] { node(0), node(0, 0, 0), node(0, 0, 0, 0), node(0, 1, 0),
                      node(0, 1, 0, 0), node(1), node(1, 0), node(1, 0, 0) });
    // nested objects
    final String a0a1 = jsonGen.getRandomValue();
    final int a0a1Type = jsonGen.valueType;
    final String a1a0 = jsonGen.getRandomValue();
    final int a1a0Type = jsonGen.valueType;
    this.assertTokenizesTo(_t, "{\"a0\":[{\"t1\":1},{\"t2\":{\"a0a1\":" + a0a1 + "}}],\"a1\":{\"t3\":{\"a1a0\":" + a1a0 + "}}}",
      new String[] { "a0", "t1", "1", "t2", "a0a1", a0a1Type == LITERAL ? a0a1.substring(1, a0a1.length() - 1) : a0a1, "a1", "t3", "a1a0", a1a0Type == LITERAL ? a1a0.substring(1, a1a0.length() - 1) : a1a0 },
      new String[] { TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[NUMBER], TOKEN_TYPES[LITERAL],
                     TOKEN_TYPES[LITERAL], TOKEN_TYPES[a0a1Type], TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL],
                     TOKEN_TYPES[LITERAL], TOKEN_TYPES[a1a0Type] },
      new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
      new IntsRef[] { node(0), node(0, 0, 0), node(0, 0, 0, 0),
                      node(0, 1, 0), node(0, 1, 0, 0), node(0, 1, 0, 0, 0),
                      node(1), node(1, 0), node(1, 0, 0), node(1, 0, 0, 0) });
    // nested objects + arrays
    this.assertTokenizesTo(_t, "{\"a0\":[{\"t1\":[1,2]},{\"t2\":{\"a0a1\":[" + a0a1 + ",23]}}],\"a1\":{\"t3\":{\"a1a0\":[true," + a1a0 + "]}}}",
      new String[] { "a0", "t1", "1", "2", "t2", "a0a1", a0a1Type == LITERAL ? a0a1.substring(1, a0a1.length() - 1) : a0a1, "23", "a1", "t3", "a1a0", "true", a1a0Type == LITERAL ? a1a0.substring(1, a1a0.length() - 1) : a1a0 },
      new String[] { TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[NUMBER], TOKEN_TYPES[NUMBER],
                     TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[a0a1Type], TOKEN_TYPES[NUMBER],
                     TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[TRUE], TOKEN_TYPES[a1a0Type] },
      new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
      new IntsRef[] { node(0), node(0, 0, 0), node(0, 0, 0, 0), node(0, 0, 0, 1),
                      node(0, 1, 0), node(0, 1, 0, 0), node(0, 1, 0, 0, 0), node(0, 1, 0, 0, 1),
                      node(1), node(1, 0), node(1, 0, 0), node(1, 0, 0, 0), node(1, 0, 0, 1) });
    // nested objects + arrays
    this.assertTokenizesTo(_t, "{\"a0\":[\"a\",{\"o6\":[\"b\",9E9]}],\"o2\":{\"o3\":\"obj3\",\"o4\":{\"o5\":null}}}",
      new String[] { "a0", "a", "o6", "b", "9E9", "o2", "o3", "obj3", "o4", "o5", "null" },
      new String[] { TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL],
                     TOKEN_TYPES[NUMBER], TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL],
                     TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[NULL] },
      new String[] { XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING,
                     XSDDatatype.XSD_DOUBLE, XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING,
                     XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING },
      new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
      new IntsRef[] { node(0), node(0, 0), node(0, 1, 0), node(0, 1, 0, 0), node(0, 1, 0, 1),
                      node(1), node(1, 0), node(1, 0, 0), node(1, 1), node(1, 1, 0), node(1, 1, 0, 0) });
  }

  @Test(expected=IllegalStateException.class)
  public void testUnclosedObject()
  throws Exception {
    this.assertTokenizesTo(_t, "{\"a\":{\"34\":34,23}", // the 23 is not parser, because it is not a literal
      new String[] { "a", "34", "34" },
      new String[] { TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[NUMBER] },
      new String[] { XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING, XSDDatatype.XSD_DOUBLE },
      new int[] { 1, 1, 1 },
      new IntsRef[] { node(0), node(0, 0), node(0, 0, 0) });
  }

  @Test(expected=IllegalStateException.class)
  public void testWrongClosingCharacter()
  throws Exception {
    this.assertTokenizesTo(_t, "{\"a\":{\"34\":34],\"a\":1}", // \"a\":1 is not parsed because of the stray ']' character
      new String[] { "a", "34", "34" },
      new String[] { TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[NUMBER] },
      new String[] { XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING, XSDDatatype.XSD_DOUBLE },
      new int[] { 1, 1, 1 },
      new IntsRef[] { node(0), node(0, 0), node(0, 0, 0) });
  }

  @Test(expected=IllegalStateException.class)
  public void testWrongClosingCharacter2()
  throws Exception {
    this.assertTokenizesTo(_t, "{\"a\":[\"34\",34},\"a\":1}", // \"a\":1 is not parsed because of the stray '}' character
      new String[] { "a", "34", "34" },
      new String[] { TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL], TOKEN_TYPES[NUMBER] },
      new String[] { XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING, XSDDatatype.XSD_DOUBLE },
      new int[] { 1, 1, 1 },
      new IntsRef[] { node(0), node(0, 0), node(0, 1) });
  }

  @Test(expected=IllegalStateException.class)
  public void testColonInArray()
  throws Exception {
    this.assertTokenizesTo(_t, "{\"a\":[\"34\":34},\"a\":1}", // 34},\"a\":1 is not parsed because of the stray '}' character
      new String[] { "a", "34" },
      new String[] { TOKEN_TYPES[LITERAL], TOKEN_TYPES[LITERAL] },
      new String[] { XSDDatatype.XSD_STRING, XSDDatatype.XSD_STRING },
      new int[] { 1, 1, 1 },
      new IntsRef[] { node(0), node(0, 0) });
  }

  @Test
  public void testRandomJson()
  throws Exception {
    for (int i = 0; i < 50; i++) {
      try {
        String json = jsonGen.getRandomJson(50);
        final int[] incr = new int[jsonGen.incr.size()];
        for (int j = 0; j < incr.length; j++) {
          incr[j] = jsonGen.incr.get(j);
        }
        this.assertTokenizesTo(_t, json,
          jsonGen.images.toArray(new String[0]),
          jsonGen.types.toArray(new String[0]),
          jsonGen.datatypes.toArray(new String[0]),
          incr,
          jsonGen.nodes.toArray(new IntsRef[0]));
      } catch (IllegalStateException e) {
        if (!jsonGen.shouldFail) {
          fail("Failed to parse json!");
        }
      }
      if (jsonGen.shouldFail) {
        fail("Expected to fail JSON didn't fail!");
      }
    }
  }

}
