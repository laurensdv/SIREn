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
package org.sindice.siren.search.node;

import static org.sindice.siren.analysis.MockSirenToken.node;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanClauseBuilder.must;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanClauseBuilder.not;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanQueryBuilder.nbq;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.sindice.siren.index.DocsAndNodesIterator;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.search.AbstractTestSirenScorer;
import org.sindice.siren.search.base.NodeScorer;

public class TestNodeReqExclScorer extends AbstractTestSirenScorer {

  @Override
  protected void configure() throws IOException {
    this.setAnalyzer(AnalyzerType.TUPLE);
    this.setPostingsFormat(PostingsFormatType.RANDOM);
  }

  @Test
  public void testNextCandidateDocument() throws Exception {
    this.addDocuments(
      "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" . ",
      "\"aaa\" \"aaa bbb\" . "
    );

    final NodeScorer scorer = this.getScorer(
      nbq(must("aaa"), not("bbb"))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);
  }

//  @Test
//  public void testNextWithPhraseExclusion1()
//  throws Exception {
//    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb ccc\" \"aaa ccc\" . \"aaa bbb ccc ddd\" \"bbb aaa ccc ddd\" . ",
//                                                    "\"aaa bbb ccc ccc ddd\" \"aaa bbb ddd ddd ccc\" . ",
//                                                    "\"aaa bbb aaa bbb ccc ddd\" \"aaa bbb ddd ccc ddd ccc ddd\" . " });
//
//    final NodeReqExclScorer scorer = this.getReqExclScorer(new String[] {"aaa", "bbb"}, new String[] {"ccc", "ddd"});
//
//    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
//    assertEquals(0, scorer.doc());
//    assertEquals(0, scorer.node()[0]);
//    assertEquals(0, scorer.node()[1]);
//    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
//    assertEquals(1, scorer.doc());
//    assertEquals(0, scorer.node()[0]);
//    assertEquals(1, scorer.node()[1]);
//    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
//  }
//
//  @Test
//  public void testNextWithPhraseExclusion2()
//  throws Exception {
//    _helper.addDocument("\"aaa bbb ccc\" . \"ccc aaa bbb\" . ");
//
//    final NodeReqExclScorer scorer = this.getReqExclScorer(new String[] {"aaa", "bbb"}, new String[] {"bbb", "ccc"});
//
//    assertFalse(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
//    assertEquals(0, scorer.doc());
//    assertEquals(1, scorer.node()[0]);
//    assertEquals(0, scorer.node()[1]);
//    assertTrue(scorer.nextDocument() == DocIdSetIterator.NO_MORE_DOCS);
//  }

  @Test
  public void testNextNodeWithExhaustedProhibitedScorer() throws Exception {
    this.addDocuments(
      "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" . ",
      "\"aaa\" \"aaa bbb\" . "
    );

    final NodeScorer scorer = this.getScorer(
      nbq(must("aaa"), not("ccc"))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,0), scorer.node());

    // here, the prohibited scorer should be set to null (exhausted), let see
    // if there is a null pointer exception somewhere
    assertTrue(scorer.nextNode());
    assertEquals(1, scorer.doc());
    assertEquals(node(0,1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);
  }

  @Test
  public void testSkipToCandidate() throws Exception {
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      docs.add("\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" \"aaa aaa\". ");
      docs.add("\"aaa bbb aaa\" . \"aaa ccc bbb\" . ");
    }
    this.addDocuments(docs);

    final NodeScorer scorer = this.getScorer(
      nbq(must("aaa"), not("bbb"))
    );

    assertTrue(scorer.skipToCandidate(16));
    assertEquals(16, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,2), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    // Next candidate document should not contain any matching node
    assertTrue(scorer.nextCandidateDocument());
    assertEquals(17, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.skipToCandidate(40));
    assertEquals(40, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,2), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertFalse(scorer.skipToCandidate(65));
    assertEndOfStream(scorer);
  }

}
