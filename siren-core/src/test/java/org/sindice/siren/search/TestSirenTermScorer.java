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
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.junit.Test;

public class TestSirenTermScorer extends AbstractTestSirenScorer {

  @Test(expected=RuntimeException.class)
  public void testNextPositionFail()
  throws Exception {
    _helper.addDocument("<http://renaud.delbru.fr/> . ");
    final Term term = new Term(QueryTestingHelper.DEFAULT_FIELD, "renaud");
    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum p = MultiFields.getTermPositionsEnum(reader,
      MultiFields.getLiveDocs(reader), QueryTestingHelper.DEFAULT_FIELD,
      term.bytes());

    final SirenTermScorer scorer = new SirenTermScorer(new ConstantWeight(),
      p, new DefaultSimilarity(), reader.norms(term.field()));
    scorer.nextPosition();
  }

  @Test
  public void testNextWithURI()
  throws Exception {
    this.assertTo(
      new AssertNextEntityFunctor(),
      new String[] { "<http://renaud.delbru.fr/> <http://renaud.delbru.fr/> . " },
      new String[] { "renaud" }, new int[][] { { 0, 0, 0, 0 } });
    this.assertTo(new AssertNextEntityFunctor(), new String[] {
        "<http://renaud.delbru.fr/> <http://renaud.delbru.fr/> . ",
        "<http://renaud.delbru.fr/> <http://test/name> \"Renaud Delbru\" . " },
        new String[] { "renaud" }, new int[][] { { 0, 0, 0, 0 }, { 1, 0, 0, 0 } });
  }

  @Test
  public void testNextPositionWithURI()
  throws Exception {
    this.assertTo(
      new AssertNextPositionEntityFunctor(),
      new String[] { "<http://renaud.delbru.fr/> <http://renaud.delbru.fr/> . " },
      new String[] { "renaud" }, new int[][] { { 0, 0, 0, 0}, { 0, 0, 1, 2 } });
    this.assertTo(new AssertNextPositionEntityFunctor(), new String[] {
        "<http://renaud.delbru.fr/> <http://renaud.delbru.fr/> . \n" +
        "<http://renaud.delbru.fr/> <http://test/name> \"Renaud Delbru\" . " },
      new String[] { "renaud" }, new int[][] { { 0, 0, 0, 0 }, { 0, 0, 1, 2 },
                                               { 0, 1, 0, 4 }, { 0, 1, 2, 8 } });
  }

  @Test
  public void testSkipToEntityTupleCell()
  throws Exception {
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      docs.add("<http://renaud.delbru.fr/> . \"renaud delbru\" \"renaud delbru\" . ");
    }
    _helper.addDocumentsWithIterator(docs);
    final SirenScorer scorer = this.getTermScorer(QueryTestingHelper.DEFAULT_FIELD, "renaud");
    assertFalse(scorer.advance(16, new int[] { 1, 1 } ) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(16, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(4, scorer.pos());
  }

  @Test
  public void testSkipToNonExistingEntityTupleCell()
  throws Exception {
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      docs.add("<http://renaud.delbru.fr/> . \"renaud delbru\" \"renaud delbru\" . ");
    }
    _helper.addDocumentsWithIterator(docs);
    final SirenScorer scorer = this.getTermScorer(QueryTestingHelper.DEFAULT_FIELD, "renaud");
    // does not exist, should skip to entity 17 and first cell
    assertFalse(scorer.advance(16, new int[] { 3, 2 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(17, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(0, scorer.pos());
  }

  @Test
  public void testSkipToEntityTupleCellNextPosition()
  throws Exception {
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      docs.add("<http://renaud.delbru.fr/> . \"renaud delbru\" \"renaud delbru\" . ");
    }
    _helper.addDocumentsWithIterator(docs);
    final SirenScorer scorer = this.getTermScorer(QueryTestingHelper.DEFAULT_FIELD, "delbru");
    assertFalse(scorer.advance(16, new int[] { 1, 0 }) == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(16, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(3, scorer.pos());

    // Should not return match in first tuple (tuple 0)
    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
//    assertEquals(-1, scorer.dataset());
    assertEquals(5, scorer.pos());

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
  }



  @Test(expected=Exception.class)
  public void testInvalidScoreCall() throws IOException {
    _helper.addDocument("\"Renaud\" . ");

    final Term t = new Term(QueryTestingHelper.DEFAULT_FIELD, "renaud");
    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum p = MultiFields.getTermPositionsEnum(reader,
      MultiFields.getLiveDocs(reader), QueryTestingHelper.DEFAULT_FIELD,
      t.bytes());

    final SirenTermScorer scorer = new SirenTermScorer(new ConstantWeight(),
      p, new DefaultSimilarity(), reader.norms(QueryTestingHelper.DEFAULT_FIELD));

    assertNotNull("scorer is null and it shouldn't be", scorer);

    // Invalid call
    scorer.score();
  }

  @Test
  public void testScore() throws IOException {
    _helper.addDocument("\"Renaud\" . ");

    final Term t = new Term(QueryTestingHelper.DEFAULT_FIELD, "renaud");
    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum p = MultiFields.getTermPositionsEnum(reader,
      MultiFields.getLiveDocs(reader), QueryTestingHelper.DEFAULT_FIELD,
      t.bytes());

    final SirenTermScorer scorer = new SirenTermScorer(new ConstantWeight(),
      p, new DefaultSimilarity(), MultiNorms.norms(reader, QueryTestingHelper.DEFAULT_FIELD));
    assertNotNull("scorer is null and it shouldn't be", scorer);

    assertNotNull("next returns null and it shouldn't be", scorer.nextDoc());
    assertEquals(0, scorer.docID());
    // All it does is returning the term frequency since weight is constant
    assertEquals(1.0, scorer.score(), 0.01);
  }

  ///////////////////////////////////
  //
  // END OF TESTS
  // START HELPER METHODS AND CLASSES
  //
  ///////////////////////////////////

  @Override
  protected void assertTo(final AssertFunctor functor, final String[] input,
                          final String[] terms, final int[][] deweyPath)
    throws Exception {
      _helper.reset();
      _helper.addDocuments(input);
      final IndexReader reader = _helper.getIndexReader();
      final HashSet<Integer> docIDs = new HashSet<Integer>();
      for (int[] path : deweyPath) {
        docIDs.add(path[0]);
      }
      assertEquals(docIDs.size(), reader.numDocs());

      SirenTermScorer scorer = null;
      for (final String t : terms) {
        scorer = this.getTermScorer(QueryTestingHelper.DEFAULT_FIELD, t);
        functor.run(scorer, deweyPath);
      }
      reader.close();
    }

}
