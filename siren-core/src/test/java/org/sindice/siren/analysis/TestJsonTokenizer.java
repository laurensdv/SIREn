package org.sindice.siren.analysis;

import static org.junit.Assert.*;
import static org.sindice.siren.analysis.JsonTokenizer.*;
import static org.sindice.siren.analysis.MockSirenToken.node;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.lucene.util.IntsRef;
import org.junit.Before;
import org.junit.Test;

public class TestJsonTokenizer
extends TokenizerHelper {

  private final JsonTokenizer _t   = new JsonTokenizer(new StringReader(""), Integer.MAX_VALUE);

  private final Random        seed = new Random();
  private Random              rand;
  private int                 valueType;

  @Before
  public void setUp() {
    final long testseed = seed.nextLong();
    rand = new Random(testseed);
    System.err.println("TestJsonTokenizer: seed=[" + testseed + "]");
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

    final int arraySize = rand.nextInt(100);
    String array = "[";
    for (int i = 0; i < arraySize; i++) {
      final String v = getRandomValue();
      array += v + ",";
      images.add(valueType == LITERAL ? v.substring(1, v.length() - 1) : v);
      nodes.add(node(0, i));
      types.add(TOKEN_TYPES[valueType]);
    }
    array += "]";

    final int[] posIncr = new int[images.size()];
    Arrays.fill(posIncr, 1);
    this.assertTokenizesTo(_t, "{\"array\":" + array + "}",
      images.toArray(new String[0]), types.toArray(new String[0]),
      posIncr, nodes.toArray(new IntsRef[0]));
  }

  private String getRandomValue() {
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
