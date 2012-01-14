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
 * @author Renaud Delbru [ 28 Apr 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.lucene.search.DocIdSetIterator;
import org.junit.Test;
import org.sindice.siren.index.DocsNodesAndPositionsIterator;

public class TestSirenConjunctionScorer
extends AbstractTestSirenScorer {

  @Test
  public void testNextWithTermConjunction()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "<http://renaud.delbru.fr/> . ",
                                                    "<http://sindice.com/test/name> \"Renaud Delbru\" . ",
                                                    "<http://sindice.com/test/type> <http://sindice.com/test/Person> . " +
                                                    "<http://sindice.com/test/name> \"Renaud Delbru\" . " });

    final SirenConjunctionScorer scorer =
      this.getConjunctionScorer(new String[] {"renaud", "delbru"});

    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(2, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNoNext() throws IOException {
    _helper.addDocument("\"eee\" . \"ddd\" . ");

    final SirenConjunctionScorer scorer =
      this.getConjunctionScorer(new String[] {"ddd", "eee"});

    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextWithPhraseConjunction()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb aaa\". ",
                                                    "\"aaa bbb aba\" \"aaa ccc bbb aaa\" . ",
                                                    "\"aaa bbb ccc\" \"aaa ccc aaa aaa ccc\" . " +
                                                    "\" bbb ccc aaa \" \"aaa bbb bbb ccc aaa ccc\" . "});

    final SirenConjunctionScorer scorer =
      this.getConjunctionScorer(new String[][] {{"aaa", "bbb"}, {"aaa", "ccc"}});

    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(2, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testSkipToWithTermConjunction()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "<http://renaud.delbru.fr/> . ",
                                                    "<http://sindice.com/test/name> \"Renaud Delbru\" . ",
                                                    "<http://sindice.com/test/type> <http://sindice.com/test/Person> . " +
                                                    "<http://sindice.com/test/name> \"Renaud Delbru\" . "});

    final SirenConjunctionScorer scorer =
      this.getConjunctionScorer(new String[] {"renaud", "delbru"});

    assertFalse(scorer.skipTo(1, new int[] { 0 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.skipTo(2, new int[] { 0, 0 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(2, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertTrue(scorer.skipTo(2, new int[] { 2, 1 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertTrue(scorer.skipTo(4) == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testSkipToWithPhrase()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "<http://renaud.delbru.fr/> . ",
                                                    "<http://sindice.com/test/name> \"Renaud Delbru\" . ",
                                                    "<http://sindice.com/test/type> <http://sindice.com/test/Person> . " +
                                                    "<http://sindice.com/test/name> \"Renaud Delbru\" . ",
                                                    "<http://sindice.com/test/name> \"Delbru Renaud\" . ",
                                                    "<http://sindice.com/test/name> \"Renaud aaaa Delbru\" . "});

    final SirenConjunctionScorer scorer =
      this.getConjunctionScorer(new String[][] {{"renaud", "delbru"}});

    assertFalse(scorer.skipTo(1, new int[] { 0 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.skipTo(2, new int[] { 0, 0 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(2, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertTrue(scorer.skipTo(2, new int[] { 2, 1 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertTrue(scorer.skipTo(4) == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testSkipToWithPhraseConjunction()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb aaa\". ",
                                                    "\"aaa bbb\" \"aaa ccc\" . \" bbb ccc \" . \"aaa bbb aaa ccc\" . ",
                                                    "\"aaa bbb aba\" \"aaa ccc bbb aaa\" . ",
                                                    "\"aaa bbb ccc\" \"aaa ccc aaa aaa ccc\" . " +
                                                    "\" bbb ccc aaa \" \"aaa bbb bbb ccc aaa ccc\" . "});

    final SirenConjunctionScorer scorer =
      this.getConjunctionScorer(new String[][] {{"aaa", "bbb"}, {"aaa", "ccc"}});

    assertFalse(scorer.skipTo(1, new int[] { 0 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.doc());
    assertEquals(2, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertFalse(scorer.skipTo(2, new int[] { 0, 1 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(3, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertTrue(scorer.skipTo(3, new int[] { 2, 1 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertTrue(scorer.skipTo(4) == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextPositionWithTermConjunction()
  throws Exception {
    _helper.addDocument(
      "<http://sindice.com/test/type> <http://sindice.com/test/Person> . " +
      "<http://sindice.com/test/homepage> <http://renaud.delbru.fr/> . " +
      "<http://sindice.com/test/name> \"Renaud Delbru\" ." +
      "<http://sindice.com/test/description> \"aaaa Renaud bbbb aaaa Delbru aaaa\" .");

    final SirenConjunctionScorer scorer =
      this.getConjunctionScorer(new String[] {"renaud", "delbru"});

    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.nextPosition() == DocsNodesAndPositionsIterator.NO_MORE_POS);
    assertEquals(0, scorer.doc());
    assertEquals(2, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.nextPosition() == DocsNodesAndPositionsIterator.NO_MORE_POS);
    assertEquals(0, scorer.doc());
    assertEquals(3, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertTrue(scorer.nextPosition() == DocsNodesAndPositionsIterator.NO_MORE_POS);
    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextPositionWithPhraseConjunction()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb aaa\". ",
                                                    "\"aaa bbb\" \"aaa ccc\" . " +
                                                    "\" bbb ccc \" . " +
                                                    "\"aaa bbb aaa ccc\" . ",
                                                    "\"aaa bbb ccc\" \"aaa ccc aaa bbb ccc\" . " +
                                                    "\" bbb ccc aaa \" \"aaa bbb bbb ccc aaa ccc\" . "});

    final SirenConjunctionScorer scorer =
      this.getConjunctionScorer(new String[][] {{"aaa", "bbb"}, {"aaa", "ccc"}});

    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.doc());
    assertEquals(2, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.nextPosition() == DocsNodesAndPositionsIterator.NO_MORE_POS);
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(2, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.nextPosition() == DocsNodesAndPositionsIterator.NO_MORE_POS);
    assertEquals(2, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertTrue(scorer.nextPosition() == DocsNodesAndPositionsIterator.NO_MORE_POS);
    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testScoreWithTermConjunction()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "<http://renaud.delbru.fr/> . ",
                                                    "<http://sindice.com/test/name> \"Renaud Delbru\" . ",
                                                    "<http://sindice.com/test/type> <http://sindice.com/test/Person> . " +
                                                    "<http://sindice.com/test/name> \"Renaud Delbru\" . ",
                                                    "<http://sindice.com/test/type> <http://sindice.com/test/Person> . " +
                                                    "<http://sindice.com/test/homepage> <http://renaud.delbru.fr/> . " +
                                                    "<http://sindice.com/test/name> \"Renaud Delbru\" ."});

    final SirenConjunctionScorer scorer =
      this.getConjunctionScorer(new String[] {"renaud", "delbru"});

    float lastScore = 0;
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    lastScore = scorer.score();
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertTrue(lastScore > scorer.score());
    lastScore = scorer.score();
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertTrue(lastScore > scorer.score());
    lastScore = scorer.score();
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertTrue(lastScore < scorer.score());
    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testScoreWithPhraseConjunction()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "<http://renaud.delbru.fr/> . ",
                                                    "<http://sindice.com/test/name> \"Renaud Delbru\" . ",
                                                    "<http://sindice.com/test/type> <http://sindice.com/test/Person> . " +
                                                    "<http://sindice.com/test/name> \"Renaud Delbru\" . ",
                                                    "<http://sindice.com/test/type> <http://sindice.com/test/Person> . " +
                                                    "<http://sindice.com/test/homepage> <http://renaud.delbru.fr/> . " +
                                                    "<http://sindice.com/test/name> \"Renaud Delbru\" ."});
    
    final SirenConjunctionScorer scorer =
      this.getConjunctionScorer(new String[][] {{"renaud", "delbru"}});

    float lastScore = 0;
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    lastScore = scorer.score();
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertTrue(lastScore > scorer.score());
    lastScore = scorer.score();
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertTrue(lastScore > scorer.score());
    lastScore = scorer.score();
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertTrue(lastScore < scorer.score());
    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Override
  protected void assertTo(final AssertFunctor functor, final String[] input,
                          final String[] terms, final int[][] deweyPath)
  throws Exception {

  }



}
