/**
 * Copyright (c) 2009-2011 Sindice Limited. All Rights Reserved.
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
/**
 * @project siren
 * @author Renaud Delbru [ 10 Dec 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.lucene.search.DocIdSetIterator;
import org.junit.Test;
import org.sindice.siren.index.DocsNodesAndPositionsIterator;

public class TestSirenDisjunctionScorer
extends AbstractTestSirenScorer {

  @Test
  public void testNextWithTermDisjunction()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] {"<http://renaud.delbru.fr/> . ",
                                                   "<http://sindice.com/test/name> \"Renaud Delbrut\" . ",
                                                   "<http://sindice.com/test/type> <http://sindice.com/test/Person> . " +
                                                   "<http://sindice.com/test/name> \"R. Delbru\" . " });

    final SirenDisjunctionScorer scorer = this.getDisjunctionScorer(new String[] {"renaud", "delbru"});

    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertEquals(2, scorer.nrMatchers());
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertEquals(1, scorer.nrMatchers());
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(2, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertEquals(1, scorer.nrMatchers());
    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextPositionWithTermDisjunction()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb\" \"aaa ccc\" . \"ccc\" \"bbb ccc\" .",
                                                    "\"aaa ccc bbb\" . \"aaa aaa ccc bbb bbb\" . " });

    final SirenDisjunctionScorer scorer = this.getDisjunctionScorer(new String[] {"aaa", "bbb"});

    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertEquals(2, scorer.nrMatchers());
    assertFalse(scorer.nextPosition() == DocsNodesAndPositionsIterator.NO_MORE_POS);
    assertEquals(0, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertEquals(1, scorer.nrMatchers());
    assertFalse(scorer.nextPosition() == DocsNodesAndPositionsIterator.NO_MORE_POS);
    assertEquals(0, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertEquals(1, scorer.nrMatchers());
    assertTrue(scorer.nextPosition() == DocsNodesAndPositionsIterator.NO_MORE_POS);

    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertEquals(2, scorer.nrMatchers());
    assertFalse(scorer.nextPosition() == DocsNodesAndPositionsIterator.NO_MORE_POS);
    assertEquals(1, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertEquals(2, scorer.nrMatchers());
    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testSkipToNextWithTermDisjunction()
  throws Exception {
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 16; i++) {
      docs.add("\"aaa bbb\" \"aaa ccc\" . \"ccc\" \"bbb ccc\" .");
      docs.add("\"aaa ccc bbb\" . \"aaa aaa ccc bbb bbb\" . ");
    }
    _helper.addDocumentsWithIterator(docs);
    
    final SirenDisjunctionScorer scorer =
      this.getDisjunctionScorer(new String[] {"aaa", "bbb"});

    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertEquals(2, scorer.nrMatchers());
    assertFalse(scorer.skipTo(16) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(16, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertEquals(2, scorer.nrMatchers());
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(17, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertEquals(2, scorer.nrMatchers());
    assertFalse(scorer.skipTo(20, new int[] { 1 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(20, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertEquals(1, scorer.nrMatchers());
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(21, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertFalse(scorer.skipTo(30, new int[] { 1, 0 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(30, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertEquals(1, scorer.nrMatchers());
    assertTrue(scorer.skipTo(34) == DocIdSetIterator.NO_MORE_DOCS);
    assertTrue(scorer.skipTo(42, new int[] { 2 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertTrue(scorer.skipTo(123, new int[] { 98, 12 }) == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Override
  protected void assertTo(final AssertFunctor functor, final String[] input,
                          final String[] terms, final int[][] deweyPath)
  throws Exception {}

}
