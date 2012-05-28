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
 * @author Renaud Delbru [ 8 May 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search.node;

import static org.sindice.siren.analysis.MockSirenToken.node;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanClauseBuilder.must;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanClauseBuilder.not;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanClauseBuilder.should;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanQueryBuilder.nbq;

import java.io.IOException;

import org.junit.Test;
import org.sindice.siren.index.DocsAndNodesIterator;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.search.AbstractTestSirenScorer;
import org.sindice.siren.search.base.NodeScorer;

public class TestNodeBooleanScorer extends AbstractTestSirenScorer {

  @Override
  protected void configure() throws IOException {
    this.setAnalyzer(AnalyzerType.TUPLE);
    this.setPostingsFormat(PostingsFormatType.RANDOM);
  }

  @Test
  public void testNextReqOpt() throws Exception {
    this.addDocuments(
      "\"aaa bbb\" \"aaa ccc\" . \"ccc\" \"bbb ccc\" . ",
      "\"aaa\" \"aaa bbb\" . "
    );

    final NodeScorer scorer = this.getScorer(
      nbq(must("aaa"), should("bbb"))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,0), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,0), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);
  }

  @Test
  public void testNextOptExcl() throws Exception {
    this.addDocuments(
      "\"aaa bbb\" \"aaa ccc\" . \"ccc\" \"bbb ccc\" . ",
      "\"aaa\" \"aaa bbb\" . "
    );

    final NodeScorer scorer = this.getScorer(
      nbq(should("bbb"), not("ccc"))
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
    assertEquals(node(0,1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);
  }

  @Test
  public void testNextReqOptExcl() throws Exception {
    this.addDocuments(
      "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" . ",
      "\"aaa\" \"aaa bbb\" . "
    );

    final NodeScorer scorer = this.getScorer(
      nbq(must("aaa"), should("bbb"), not("ccc"))
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
    assertTrue(scorer.nextNode());
    assertEquals(node(0,1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);
  }

  @Test
  public void testNextReqOptExclWithConstraint() throws Exception {
    this.addDocuments(
      "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" . ",
      "\"aaa\" \"aaa bbb\" . "
    );

    final NodeScorer scorer = this.getScorer(
      nbq(must("aaa"), should("bbb"), not("ccc")).bound(node(0,1), node(0,1))
                                                 .level(2)
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);
  }

  /**
   * Single clause rewrite should keep node constraint information.
   * <p>
   * The {@link NodeBooleanQuery} with one clause will be rewritten into a
   * single node term query.
   */
  @Test
  public void testSingleClauseRewriteWithConstraint() throws Exception {
    this.addDocuments(
      "\"aaa bbb\" \"aaa ccc\" . "
    );

    NodeScorer scorer = this.getScorer(nbq(must("aaa")).level(1));

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

    scorer = this.getScorer(nbq(must("aaa")).bound(node(0,1), node(0,2)));

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);
  }

  /**
   * Clause rewrite should keep node constraint information
   */
  @Test
  public void testClauseRewriteWithConstraint() throws Exception {
    this.addDocuments(
      "\"aaa bbb\" \"aaa bbb\" . "
    );

    // Each nested boolean query will be rewritten into a single clause,
    // therefore the top level boolean query will be rewritten too.
    NodeScorer scorer = this.getScorer(
      nbq(
        must(nbq(must("aaa"))),
        must(nbq(must("bbb")))
      ).level(1)
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

    scorer = this.getScorer(
      nbq(
        must(nbq(must("aaa"))),
        must(nbq(must("bbb")))
      ).bound(node(0,0), node(0,0))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(0,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);
  }

}
