package org.sindice.siren.analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.lucene.util.IntsRef;

public class MockSirenDocument {

  TreeMap<IntsRef, ArrayList<MockSirenToken>> sortedTokens;

  private final Comparator<IntsRef> INTS_COMP = new Comparator<IntsRef>() {

    public int compare(final IntsRef ints1, final IntsRef ints2) {
        return ints1.compareTo(ints2);
    }

  };

  public MockSirenDocument(final MockSirenToken ... tokens) {
    sortedTokens = new TreeMap<IntsRef, ArrayList<MockSirenToken>>(INTS_COMP);

    IntsRef ints;
    for (final MockSirenToken token : tokens) {
      ints = token.nodePath;
      if (!sortedTokens.containsKey(ints)) {
        sortedTokens.put(ints, new ArrayList<MockSirenToken>());
      }
      sortedTokens.get(ints).add(token);
    }
  }

  public Iterator<ArrayList<MockSirenToken>> iterator() {
    return sortedTokens.values().iterator();
  }

  public static MockSirenDocument doc(final MockSirenToken ... tokens) {
    return new MockSirenDocument(tokens);
  }

}

