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
package org.sindice.siren.search.primitive;

import static org.sindice.siren.analysis.MockSirenToken.node;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodePhraseQueryBuilder.npq;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.search.AbstractTestSirenScorer;
import org.sindice.siren.search.node.NodeQuery;

public class TestSirenExactPhraseScorer extends AbstractTestSirenScorer {

  @Override
  protected void configure() throws IOException {
    this.setAnalyzer(AnalyzerType.TUPLE);
    this.setPostingsFormat(PostingsFormatType.RANDOM);
  }

  /**
   * Test exact phrase scorer: should not match two words in separate nodes
   */
  @Test
  public void testEmptyResult1() throws Exception {
    this.addDocument("\"word1 word2 word3\" \"word4 word5\" . ");

    final NodePhraseScorer scorer = (NodePhraseScorer) this.getScorer(npq("word1", "word4"));
    assertTrue(scorer.nextCandidateDocument());
    assertFalse(scorer.nextNode());
  }

  /**
   * Test exact phrase scorer: should not match phrase with a gap of 1 between
   * the two phrase query terms
   */
  @Test
  public void testEmptyResult2() throws Exception {
    this.addDocument("\"word1 word2 word3\" \"word4 word5\" . ");

    final NodePhraseScorer scorer = (NodePhraseScorer) this.getScorer(npq("word4", "", "word5"));
    assertTrue(scorer.nextCandidateDocument());
    assertFalse(scorer.nextNode());
  }

  @Test
  public void testNodeConstraint() throws Exception {
    this.addDocument("\"word1 word2 word3\" \"word4 word5\" . ");

    NodePhraseScorer scorer = (NodePhraseScorer) this.getScorer(npq("word4", "word5"));
    assertTrue(scorer.nextCandidateDocument());
    assertTrue(scorer.nextNode());

    scorer = (NodePhraseScorer) this.getScorer(npq("word4", "word5").level(2));
    assertTrue(scorer.nextCandidateDocument());
    assertTrue(scorer.nextNode());

    scorer = (NodePhraseScorer) this.getScorer(npq("word4", "word5").level(1));
    assertTrue(scorer.nextCandidateDocument());
    assertFalse(scorer.nextNode());

    scorer = (NodePhraseScorer) this.getScorer(npq("word4", "word5").bound(0,0));
    assertTrue(scorer.nextCandidateDocument());
    assertFalse(scorer.nextNode());

    scorer = (NodePhraseScorer) this.getScorer(npq("word4", "word5").bound(0,1));
    assertTrue(scorer.nextCandidateDocument());
    assertTrue(scorer.nextNode());
  }

  @Test
  public void testMultipleOccurrences() throws Exception {
    this.addDocument("<http://renaud.delbru.fr/> \"renaud delbru delbru renaud renaud delbru\" . ");

    NodeQuery q = npq("renaud", "delbru").getQuery();
    NodePhraseScorer scorer = (NodePhraseScorer) this.getScorer(q);

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,0), scorer.node());
    assertEquals(1.0f, scorer.termFreqInNode(), 0);
    assertTrue(scorer.nextNode());
    assertEquals(node(0,1), scorer.node());
    assertEquals(2.0f, scorer.termFreqInNode(), 0);

    assertFalse(scorer.nextNode());
    assertFalse(scorer.nextCandidateDocument());

    q = npq("renaud", "", "delbru").getQuery();
    scorer = (NodePhraseScorer) this.getScorer(q);

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,1), scorer.node());
    assertEquals(2.0f, scorer.termFreqInNode(), 0);

    assertFalse(scorer.nextNode());
    assertFalse(scorer.nextCandidateDocument());
  }

  @Test
  public void testSkipToCandidate() throws Exception {
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      docs.add("<http://renaud.delbru.fr/> . ");
    }
    this.addDocuments(docs);

    final NodePhraseScorer scorer = (NodePhraseScorer) this.getScorer(npq("renaud", "delbru"));
    assertTrue(scorer.skipToCandidate(16));
    assertEquals(16, scorer.doc());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,0), scorer.node());
  }

  @Test
  public void testSkipToCandidateNext() throws Exception {
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 32; i++)
      docs.add("<http://renaud.delbru.fr/> . ");
    this.addDocuments(docs);

    final NodePhraseScorer scorer = (NodePhraseScorer) this.getScorer(npq("renaud", "delbru"));
    assertTrue(scorer.nextCandidateDocument());
    assertTrue(scorer.skipToCandidate(16));
    assertEquals(16, scorer.doc());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,0), scorer.node());
    assertTrue(scorer.nextCandidateDocument());
    assertEquals(17, scorer.doc());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,0), scorer.node());
  }

//  @Test(expected=InvalidCallException.class)
//  public void testInvalidScoreCall() throws IOException {
//    _helper.addDocument("\"Renaud Delbru\" . ");
//
//    final Term t1 = new Term(QueryTestingHelper.DEFAULT_FIELD, "renaud");
//    final Term t2 = new Term(QueryTestingHelper.DEFAULT_FIELD, "delbru");
//    final NodePhraseQuery query = new NodePhraseQuery();
//    query.add(t1); query.add(t2);
//    final Weight w = query.createWeight(_helper.getIndexSearcher());
//
//    final IndexReader reader = _helper.getIndexReader();
//    final DocsAndPositionsEnum[] tps = new DocsAndPositionsEnum[2];
//    tps[0] = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), t1.field(), t1.bytes());
//    tps[1] = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), t2.field(), t2.bytes());
//
//    final NodePhraseScorer scorer = new NodeExactPhraseScorer(w, tps, new int[] {0, 1},
//      _helper.getIndexSearcher().getSimilarityProvider().get(QueryTestingHelper.DEFAULT_FIELD),
//      MultiNorms.norms(reader, QueryTestingHelper.DEFAULT_FIELD));
//    assertNotNull("ts is null and it shouldn't be", scorer);
//
//    // Invalid call
//    scorer.score();
//  }
//
//  @Test
//  public void testScore() throws IOException {
//    _helper.addDocument("\"Renaud Delbru\" . <http://renaud.delbru.fr> . ");
//
//    final Term t1 = new Term(QueryTestingHelper.DEFAULT_FIELD, "renaud");
//    final Term t2 = new Term(QueryTestingHelper.DEFAULT_FIELD, "delbru");
//    final NodePhraseQuery query = new NodePhraseQuery();
//    query.add(t1); query.add(t2);
//
//    final IndexReader reader = _helper.getIndexReader();
//    final DocsAndPositionsEnum[] tps = new DocsAndPositionsEnum[2];
//    tps[0] = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), t1.field(), t1.bytes());
//    tps[1] = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), t2.field(), t2.bytes());
//
//    final NodePhraseScorer scorer = new NodeExactPhraseScorer(
//      new ConstantWeight(), tps, new int[] {0, 1},
//      _helper.getIndexSearcher().getSimilarityProvider().get(QueryTestingHelper.DEFAULT_FIELD),
//      MultiNorms.norms(reader, QueryTestingHelper.DEFAULT_FIELD));
//    assertNotNull("ts is null and it shouldn't be", scorer);
//
//    assertFalse("no doc returned", scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
//    assertEquals(0, scorer.doc());
//    assertEquals(0.70, scorer.score(), 0.01);
//  }


}
