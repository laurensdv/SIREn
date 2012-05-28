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
/**
 * @project siren-core
 * @author Renaud Delbru [ 13 Mar 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.search.twig;

import static org.sindice.siren.analysis.MockSirenDocument.doc;
import static org.sindice.siren.analysis.MockSirenToken.node;
import static org.sindice.siren.analysis.MockSirenToken.token;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanClauseBuilder.must;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanClauseBuilder.not;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanClauseBuilder.should;
import static org.sindice.siren.search.AbstractTestSirenScorer.TwigChildBuilder.child;
import static org.sindice.siren.search.AbstractTestSirenScorer.TwigDescendantBuilder.desc;
import static org.sindice.siren.search.AbstractTestSirenScorer.TwigQueryBuilder.twq;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.search.Scorer.ChildScorer;
import org.junit.Test;
import org.sindice.siren.index.DocsAndNodesIterator;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.search.AbstractTestSirenScorer;
import org.sindice.siren.search.base.NodeScorer;
import org.sindice.siren.search.primitive.NodeTermScorer;
import org.sindice.siren.search.twig.TwigScorer.AncestorFilterNodeScorer;

public class TestTwigScorer extends AbstractTestSirenScorer {

  @Override
  protected void configure() throws IOException {
    this.setAnalyzer(AnalyzerType.MOCK);
    this.setPostingsFormat(PostingsFormatType.RANDOM);
  }

  @Test
  public void testSingleChild() throws Exception {
    this.addDocuments(
      doc(token("aaa", node(1)), token("bbb", node(1,0)), token("aaa", node(2))),
      doc(token("aaa", node(1,0)), token("bbb", node(1,0,1,0))),
      doc(token("aaa", node(5,3,6,3)), token("bbb", node(5,3,6,3,7)))
    );

    NodeScorer scorer = this.getScorer(
      twq(1, must("aaa")).with(child(must("bbb")))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(2, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

    scorer = this.getScorer(
      twq(2, must("aaa")).with(child(must("bbb")))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(2, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

    scorer = this.getScorer(
      twq(4, must("aaa")).with(child(must("bbb")))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(2, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(5,3,6,3), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

  }

  @Test
  public void testSingleDescendant() throws Exception {
    this.addDocuments(
      doc(token("aaa", node(1,0)), token("bbb", node(1,0,1))),
      doc(token("aaa", node(1,0)), token("bbb", node(1,0,1,0))),
      doc(token("aaa", node(1,0)), token("bbb", node(1,1,2,3)))
    );

    final NodeScorer scorer = this.getScorer(
      twq(2, must("aaa")).with(desc(4, must("bbb")))
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
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(2, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

  }

  @Test
  public void testChildDescendant() throws Exception {
    this.addDocuments(
      doc(token("aaa", node(1,0)), token("bbb", node(1,0,1,0)), token("ccc", node(1,0,1,0))),
      doc(token("aaa", node(1,0)), token("bbb", node(1,0,1,0)), token("ccc", node(1,0,4)))
    );

    final NodeScorer scorer = this.getScorer(
      twq(2, must("aaa")).with(desc(4, must("bbb")))
                         .with(child(must("ccc")))
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
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

  }

  @Test
  public void testBooleanChild() throws Exception {
    this.addDocuments(
      doc(token("aaa", node(1,0)), token("bbb", node(1,0,1)), token("ccc", node(1,0,4))),
      doc(token("aaa", node(1,0)), token("bbb", node(1,0,1)), token("ccc", node(1,0,1)))
    );

    NodeScorer scorer = this.getScorer(
      twq(2, must("aaa")).with(child(must("bbb"), must("ccc")))
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
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

    scorer = this.getScorer(
      twq(2, must("aaa")).with(child(must("bbb"), should("ccc")))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

    scorer = this.getScorer(
      twq(2, must("aaa")).with(child(must("bbb"), not("ccc")))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

  }

  @Test
  public void testBooleanRoot() throws Exception {
    this.addDocuments(
      doc(token("aaa", node(1,0)), token("bbb", node(1,1)), token("ccc", node(1,0,1))),
      doc(token("aaa", node(1,0)), token("bbb", node(1,0)), token("ccc", node(1,0,1)))
    );

    NodeScorer scorer = this.getScorer(
      twq(2, must("aaa"), must("bbb")).with(child(must("ccc")))
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
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

    scorer = this.getScorer(
      twq(2, must("aaa"), should("bbb")).with(child(must("ccc")))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

    scorer = this.getScorer(
      twq(2, must("aaa"), not("bbb")).with(child(must("ccc")))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

  }

  @Test
  public void testBooleanClause() throws Exception {
    this.addDocuments(
      doc(token("aaa", node(1,0)), token("bbb", node(1,0,1)), token("ccc", node(1,0,4))),
      doc(token("aaa", node(1,0)), token("bbb", node(1,0,1))),
      doc(token("aaa", node(1,0)))
    );

    NodeScorer scorer = this.getScorer(
      twq(2, must("aaa")).with(child(must("bbb")))
                         .optional(child(must("ccc")))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

    scorer = this.getScorer(
      twq(2, must("aaa")).with(child(must("bbb")))
                         .without(child(must("ccc")))
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
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

    scorer = this.getScorer(
      twq(2, must("aaa")).optional(child(must("bbb")))
                         .without(child(must("ccc")))
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
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(2, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

    // Test with only a prohibited clause
    scorer = this.getScorer(
      twq(2, must("aaa")).without(child(must("ccc")))
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
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(2, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

  }

  @Test
  public void testNestedTwig() throws Exception {
    this.addDocuments(
      doc(token("aaa", node(1,0)), token("bbb", node(1,0,1)), token("ccc", node(1,0,1,0)),
                                   token("eee", node(1,0,4)), token("fff", node(1,0,4,1)))
    );

    final NodeScorer scorer = this.getScorer(
      twq(2, must("aaa")).with(child(must(
                                twq(3, must("bbb")).with(child(must("ccc")))
                               )))
                         .with(child(must(
                                twq(3, must("eee")).with(child(must("fff")))
                               )))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,0), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);

  }

  @Test
  public void testNodeConstraints() throws Exception {
    this.addDocuments(
      doc(token("aaa", node(2)), token("bbb", node(2,0))),
      doc(token("aaa", node(0)), token("bbb", node(1,0))),
      doc(token("aaa", node(4)), token("bbb", node(4,0)))
    );

    final NodeScorer scorer = this.getScorer(
      twq(1, must("aaa")).with(child(must("bbb")))
                         .bound(node(1), node(3))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(2), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(2, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertEndOfStream(scorer);
  }

  @Test
  public void testRewriteRootAndClause() throws Exception {
    this.addDocuments(
      doc(token("aaa", node(2)), token("bbb", node(2,0)))
    );

    final NodeScorer scorer = this.getScorer(
      twq(1, must("aaa")).with(child(must("bbb")))
    );

    assertTrue(scorer instanceof TwigScorer);
    final TwigScorer ts = (TwigScorer) scorer;
    final Collection<ChildScorer> childs = ts.getChildren();
    assertEquals(2, childs.size());
    // The root and child must have been rewritten into NodeTermScorer
    NodeScorer s = null;
    for (final ChildScorer child : childs) {
      s = (NodeScorer) child.child;
      if (s instanceof AncestorFilterNodeScorer) {
        s = ((AncestorFilterNodeScorer) child.child).getScorer();
      }
      assertTrue(s instanceof NodeTermScorer);
    }
  }

  @Test
  public void testRewriteWithZeroClause() throws Exception {
    this.addDocuments(
      doc(token("aaa", node(2)), token("bbb", node(2,0)))
    );

    final NodeScorer scorer = this.getScorer(
      twq(1, must("aaa"))
    );

    // Should be rewritten into a NodeTermScorer
    assertTrue(scorer instanceof NodeTermScorer);
  }

}
