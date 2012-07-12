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

