package org.sindice.siren.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MockSirenDocument {

  private final List<MockSirenToken> tokens = new ArrayList<MockSirenToken>();

  public MockSirenDocument(final MockSirenToken ... tokens) {
    for (final MockSirenToken token : tokens) {
      this.tokens.add(token);
    }
  }

  public Iterator<MockSirenToken> iterator() {
    return tokens.iterator();
  }

  public static MockSirenDocument doc(final MockSirenToken ... tokens) {
    return new MockSirenDocument(tokens);
  }

}

