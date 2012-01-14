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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.MultiNorms;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Weight;
import org.junit.Test;
import org.sindice.siren.index.DocsNodesAndPositionsIterator;
import org.sindice.siren.search.SirenScorer.InvalidCallException;

public class TestSirenExactPhraseScorer
extends AbstractTestSirenScorer {

  /**
   * Test exact phrase scorer: should not match two words in separate cells
   *
   * @throws Exception
   */
  @Test
  public void testExactNextFail1()
  throws Exception {
    final String field = "content";
    _helper.addDocument("\"word1 word2 word3\" \"word4 word5\" . ");

    final SirenExactPhraseScorer scorer = this.getExactScorer(field, new int[] { 0, 1 },
      new String[] { "word1", "word4" });
    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
  }

  /**
   * Test exact phrase scorer: should not match phrase with a gap of 1 between
   * the two phrase query term
   *
   * @throws Exception
   */
  @Test
  public void testExactNextFail2()
  throws Exception {
    final String field = "content";
    _helper.addDocument("\"word1 word2 word3\" \"word4 word5\" . ");
    final SirenExactPhraseScorer scorer = this.getExactScorer(field,
      new int[] { 0, 2 }, new String[] { "word4", "word5" });
    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextWithURI()
  throws Exception {
    this.assertTo(
      new AssertNextEntityFunctor(),
      new String[] { "<http://renaud.delbru.fr/> <http://renaud.delbru.fr/> . " },
      new String[] { "renaud", "delbru" }, new int[][] { { 0, 0, 0, 0 } });
    this.assertTo(new AssertNextEntityFunctor(), new String[] {
        "<http://renaud.delbru.fr/> <http://renaud.delbru.fr/> . ",
        "<http://renaud.delbru.fr/> <http://test/name> \"Renaud Delbru\" . " },
      new String[] { "renaud", "delbru" }, new int[][] { { 0, 0, 0, 0 }, { 1, 0, 0, 0 } });
  }

  @Test
  public void testNextPositionWithURI()
  throws Exception {
    this.assertTo(
      new AssertNextPositionEntityFunctor(),
      new String[] { "<http://renaud.delbru.fr/> <http://renaud.delbru.fr/> . " },
      new String[] { "renaud", "delbru" }, new int[][] { { 0, 0, 0, 0 }, { 0, 0, 1, 2 } });
    this.assertTo(new AssertNextPositionEntityFunctor(), new String[] {
        "<http://renaud.delbru.fr/> <http://renaud.delbru.fr/> . ",
        "<http://renaud.delbru.fr/> <http://test/name> \"Renaud Delbru\" . " },
      new String[] { "renaud" }, new int[][] { { 0, 0, 0, 0 },
                                               { 0, 0, 1, 2 },
                                               { 1, 0, 0, 0 },
                                               { 1, 0, 2, 4 } });
  }

  @Test
  public void testNextPositionWithMultipleOccurrencesInLiteral()
  throws Exception {
    this.assertTo(
      new AssertNextPositionEntityFunctor(),
      new String[] { "<http://renaud.delbru.fr/> \"renaud delbru delbru renaud renaud delbru\" . " },
      new String[] { "renaud", "delbru" }, new int[][] { { 0, 0, 0, 0 },
                                                         { 0, 0, 1, 2 },
                                                         { 0, 0, 1, 6 } });
  }

  @Test
  public void testSkipToEntity()
  throws Exception {
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 32; i++)
      docs.add("<http://renaud.delbru.fr/> . ");
    _helper.addDocumentsWithIterator(docs);
    final SirenScorer scorer = this.getExactScorer(QueryTestingHelper.DEFAULT_FIELD, new String[] {"renaud", "delbru"});
    assertFalse(scorer.skipTo(16) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(16, scorer.doc());

    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(0, scorer.pos());
  }

  @Test
  public void testSkipToEntityNext()
  throws Exception {
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 32; i++)
      docs.add("<http://renaud.delbru.fr/> . ");
    _helper.addDocumentsWithIterator(docs);
    final SirenScorer scorer = this.getExactScorer(QueryTestingHelper.DEFAULT_FIELD, new String[] {"renaud", "delbru"});
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertFalse(scorer.skipTo(16) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(16, scorer.doc());

    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(0, scorer.pos());
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(17, scorer.doc());
  }

  /**
   * Check if {@link SirenPhraseScorer#advance(int, int, int)} works correctly
   * when advancing to the same entity.
   */
  @Test
  public void testNextSkipToEntity1()
  throws Exception {
    _helper.addDocument("\"aaa bbb aaa\" . \"aaa bbb ccc\" .");
    final SirenScorer scorer = this.getExactScorer(QueryTestingHelper.DEFAULT_FIELD, new String[] {"bbb", "ccc"});
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.doc());
    assertEquals(0, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(4, scorer.pos());
    assertFalse(scorer.skipTo(0, new int[] { 1, 0 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.doc());
    assertEquals(0, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(4, scorer.pos());
    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
  }

  /**
   * Check if {@link SirenPhraseScorer#advance(int, int, int)} works correctly
   * when advancing to the same entity.
   */
  @Test
  public void testNextSkipToEntity2()
  throws Exception {
    _helper.addDocument("\"aaa bbb aaa\" . \"ccc bbb ccc\" . \"aaa bbb ccc\" .");
    final SirenScorer scorer = this.getExactScorer(QueryTestingHelper.DEFAULT_FIELD, new String[] {"bbb", "ccc"});
    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.doc());
    assertEquals(0, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(4, scorer.pos());
    assertFalse(scorer.skipTo(0, new int[] { 0 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.doc());
    assertEquals(0, scorer.doc());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertFalse(scorer.skipTo(0, new int[] { 1, 2 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.doc());
    assertEquals(0, scorer.doc());
    assertEquals(2, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());

    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testSkipToEntityNextPosition()
  throws Exception {
    for (int i = 0; i < 32; i++)
      _helper.addDocument("<http://renaud.delbru.fr/> . \"renaud delbru\" .");
    final SirenScorer scorer = this.getExactScorer(QueryTestingHelper.DEFAULT_FIELD, new String[] {"renaud", "delbru"});
    assertFalse(scorer.skipTo(16) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(16, scorer.doc());

    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(0, scorer.pos());

    assertFalse(scorer.nextPosition() == DocsNodesAndPositionsIterator.NO_MORE_POS);
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(2, scorer.pos());

    assertTrue(scorer.nextPosition() == DocsNodesAndPositionsIterator.NO_MORE_POS);
  }

  @Test
  public void testSkipToEntityTuple()
  throws Exception {
    for (int i = 0; i < 32; i++)
      _helper.addDocument("<http://renaud.delbru.fr/> . \"renaud delbru\" . \"renaud delbru\" . ");
    final SirenScorer scorer = this.getExactScorer(QueryTestingHelper.DEFAULT_FIELD, new String[] {"renaud", "delbru"});
    assertFalse(scorer.skipTo(16, new int[] { 2 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(16, scorer.doc());

    assertEquals(2, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(4, scorer.pos());
  }

  @Test
  public void testSkipToEntityTupleCell()
  throws Exception {
    for (int i = 0; i < 32; i++)
      _helper.addDocument("<http://renaud.delbru.fr/> . \"renaud delbru\" \"renaud delbru\" . ");
    final SirenScorer scorer = this.getExactScorer(QueryTestingHelper.DEFAULT_FIELD, new String[] {"renaud", "delbru"});
    assertFalse(scorer.skipTo(16, new int[] { 1, 1 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(16, scorer.doc());

    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(4, scorer.pos());
  }

  @Test
  public void testSkipToNonExistingEntityTupleCell()
  throws Exception {
    for (int i = 0; i < 32; i++)
      _helper.addDocument("<http://renaud.delbru.fr/> . \"renaud delbru\" \"renaud delbru\" . ");
    final SirenScorer scorer = this.getExactScorer(QueryTestingHelper.DEFAULT_FIELD, new String[] {"renaud", "delbru"});
    assertFalse(scorer.skipTo(16, new int[] { 3, 2 }) == DocIdSetIterator.NO_MORE_DOCS); // does not exist, should skip to entity 17
    assertEquals(17, scorer.doc());
    assertEquals(17, scorer.doc());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(0, scorer.pos());
  }

  @Test
  public void testSkipToEntityTupleCellNextPosition()
  throws Exception {
    for (int i = 0; i < 32; i++)
      _helper.addDocument("<http://renaud.delbru.fr/> . \"renaud delbru\" \"renaud delbru\" . ");
    final SirenScorer scorer = this.getExactScorer(QueryTestingHelper.DEFAULT_FIELD, new String[] {"renaud", "delbru"});
    assertFalse(scorer.skipTo(16, new int[] { 1, 0 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(16, scorer.doc());

    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(2, scorer.pos());

    // Should not return match in first tuple (tuple 0)
    assertFalse(scorer.nextPosition() == DocsNodesAndPositionsIterator.NO_MORE_POS);
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(4, scorer.pos());

    assertTrue(scorer.nextPosition() == DocsNodesAndPositionsIterator.NO_MORE_POS);
  }

  @Test(expected=InvalidCallException.class)
  public void testInvalidScoreCall() throws IOException {
    _helper.addDocument("\"Renaud Delbru\" . ");

    final Term t1 = new Term(QueryTestingHelper.DEFAULT_FIELD, "renaud");
    final Term t2 = new Term(QueryTestingHelper.DEFAULT_FIELD, "delbru");
    final SirenPhraseQuery query = new SirenPhraseQuery();
    query.add(t1); query.add(t2);
    final Weight w = query.createWeight(_helper.getIndexSearcher());

    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum[] tps = new DocsAndPositionsEnum[2];
    tps[0] = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), t1.field(), t1.bytes());
    tps[1] = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), t2.field(), t2.bytes());

    final SirenPhraseScorer scorer = new SirenExactPhraseScorer(w, tps, new int[] {0, 1},
      _helper.getIndexSearcher().getSimilarityProvider().get(QueryTestingHelper.DEFAULT_FIELD),
      MultiNorms.norms(reader, QueryTestingHelper.DEFAULT_FIELD));
    assertNotNull("ts is null and it shouldn't be", scorer);

    // Invalid call
    scorer.score();
  }

  @Test
  public void testScore() throws IOException {
    _helper.addDocument("\"Renaud Delbru\" . <http://renaud.delbru.fr> . ");

    final Term t1 = new Term(QueryTestingHelper.DEFAULT_FIELD, "renaud");
    final Term t2 = new Term(QueryTestingHelper.DEFAULT_FIELD, "delbru");
    final SirenPhraseQuery query = new SirenPhraseQuery();
    query.add(t1); query.add(t2);

    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum[] tps = new DocsAndPositionsEnum[2];
    tps[0] = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), t1.field(), t1.bytes());
    tps[1] = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), t2.field(), t2.bytes());

    final SirenPhraseScorer scorer = new SirenExactPhraseScorer(
      new ConstantWeight(), tps, new int[] {0, 1},
      _helper.getIndexSearcher().getSimilarityProvider().get(QueryTestingHelper.DEFAULT_FIELD),
      MultiNorms.norms(reader, QueryTestingHelper.DEFAULT_FIELD));
    assertNotNull("ts is null and it shouldn't be", scorer);

    assertFalse("no doc returned", scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.doc());
    assertEquals(0.70, scorer.score(), 0.01);
  }

  // /////////////////////////////////
  //
  // END OF TESTS
  // START HELPER METHODS AND CLASSES
  //
  // /////////////////////////////////

  @Override
  protected void assertTo(final AssertFunctor functor, final String[] input,
                          final String[] phraseTerms,
                          final int[][] deweyPath)
  throws Exception {
    _helper.reset();
    _helper.addDocuments(input);
    final IndexReader reader = _helper.getIndexReader();
    final HashSet<Integer> docs = new HashSet<Integer>();
    for (int[] dp : deweyPath) {
      docs.add(dp[0]);
    }
    assertEquals(docs.size(), reader.numDocs());

    final SirenExactPhraseScorer scorer = this.getExactScorer(QueryTestingHelper.DEFAULT_FIELD, phraseTerms);
    functor.run(scorer, deweyPath);
    reader.close();
  }

}
