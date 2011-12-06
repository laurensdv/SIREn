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



public class TestSirenReqExclScorer
extends AbstractTestSirenScorer {

  @Test
  public void testNextWithTermExclusion()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" . ",
                                                    "\"aaa\" \"aaa bbb\" . " });

    final SirenReqExclScorer scorer = this.getReqExclScorer("aaa", "bbb");

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextWithPhraseExclusion1()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb ccc\" \"aaa ccc\" . \"aaa bbb ccc ddd\" \"bbb aaa ccc ddd\" . ",
                                                    "\"aaa bbb ccc ccc ddd\" \"aaa bbb ddd ddd ccc\" . ",
                                                    "\"aaa bbb aaa bbb ccc ddd\" \"aaa bbb ddd ccc ddd ccc ddd\" . " });

    final SirenReqExclScorer scorer = this.getReqExclScorer(new String[] {"aaa", "bbb"}, new String[] {"ccc", "ddd"});

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertTrue(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextWithPhraseExclusion2()
  throws Exception {
    _helper.addDocument("\"aaa bbb ccc\" . \"ccc aaa bbb\" . ");

    final SirenReqExclScorer scorer = this.getReqExclScorer(new String[] {"aaa", "bbb"}, new String[] {"bbb", "ccc"});

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextPositionWithTermExclusion1()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" . ",
                                                    "\"aaa\" \"aaa bbb\" . \"aaa bb ccc\" . " });

    final SirenReqExclScorer scorer = this.getReqExclScorer("aaa", "bbb");

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(1, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
  }

  @Test
  public void testNextPositionWithExhaustedProhibitedScorer()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" . ",
                                                    "\"aaa\" \"aaa bbb\" . " });

    final SirenReqExclScorer scorer = this.getReqExclScorer("aaa", "ccc");

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    // here, the prohibited scorer should be set to null (exhausted), let see
    // if there is a null pointer exception somewhere
    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertTrue(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextPositionWithPhraseExclusion()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb ccc\" \"aaa ccc\" . \"aaa bbb ccc dd ddd\" \"bbb aaa ccc ddd\" . ",
                                                    "\"aaa ccc bbb aaa bbb ddd\" \"aaa bbb ddd ccc\" . ",
                                                    "\"aaa bbb aaa bbb ccc ddd\" \"aaa bbb ddd ccc ddd ccc ddd\" . " });

    final SirenReqExclScorer scorer = this.getReqExclScorer(new String[] {"aaa", "bbb"}, new String[] {"ccc", "ddd"});

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(0, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertTrue(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testSkipToWithTermExclusion()
  throws Exception {
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      docs.add("\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" \"aaa aaa\". ");
      docs.add("\"aaa bbb aaa\" . \"aaa ccc bbb\" . ");
    }
    _helper.addDocumentsWithIterator(docs);
    
    final SirenReqExclScorer scorer = this.getReqExclScorer("aaa", "bbb");

    assertFalse(scorer.advance(16) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(16, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(16, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(2, scorer.node()[1]);
    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS); // should match two times (two positions in the cell)
    assertEquals(16, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(2, scorer.node()[1]);
    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(18, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertFalse(scorer.advance(40, new int[] { 1 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(40, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(2, scorer.node()[1]);
    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS); // should match two times (two positions in the cell)
    assertEquals(40, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(2, scorer.node()[1]);
    assertFalse(scorer.advance(60, new int[] { 1, 1 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(60, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(2, scorer.node()[1]);
    assertTrue(scorer.advance(64, new int[] { 2 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertTrue(scorer.advance(65) == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testSkipToWithPhraseExclusion()
  throws Exception {
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      docs.add("\"aaa bbb ccc\" \"aaa ccc\" . \"aaa bbb ccc dd ddd\" \"bbb aaa ccc ddd\" . ");
      docs.add("\"aaa ccc bbb aaa bbb ddd\" \"aaa bbb ddd ccc\" . ");
    }
    _helper.addDocumentsWithIterator(docs);

    final SirenReqExclScorer scorer = this.getReqExclScorer(new String[] {"aaa", "bbb"}, new String[] {"ccc", "ddd"});

    assertFalse(scorer.advance(32) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(32, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(32, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertFalse(scorer.advance(40, new int[] { 1 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(40, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertFalse(scorer.advance(52, new int[] { 1, 0 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(52, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertFalse(scorer.advance(53, new int[] { 0, 1 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(53, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertTrue(scorer.advance(64, new int[] { 2 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertTrue(scorer.advance(65) == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Override
  protected void assertTo(final AssertFunctor functor, final String[] input,
                          final String[] terms, final int[][] deweyPath)
  throws Exception {
  }


}
