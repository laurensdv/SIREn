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
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeTermQueryBuilder.ntq;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.junit.Test;
import org.sindice.siren.index.DocsAndNodesIterator;
import org.sindice.siren.search.AbstractTestSirenScorer;
import org.sindice.siren.search.base.NodeScorer;

public class TestNodeDisjunctionScorerNodeQueue extends AbstractTestSirenScorer {

  /**
   * Test method for {@link org.sindice.siren.search.node.NodeDisjunctionScorerQueue#top()}.
   * @throws IOException
   * @throws CorruptIndexException
   */
  @Test
  public void testTop() throws IOException {
    this.addDocument(writer, "\"term1\" . ");
    this.addDocument(writer, "\"term2\" . ");
    this.addDocument(writer, "\"term3\" .  \"term4\" . ");

    final NodeDisjunctionScorerQueue q = new NodeDisjunctionScorerQueue(5);

    final NodeScorer s1 = this.getScorer(ntq("term1"));
    final NodeScorer s2 = this.getScorer(ntq("term2"));
    final NodeScorer s3 = this.getScorer(ntq("term3"));
    final NodeScorer s4 = this.getScorer(ntq("term4"));

    q.put(s3);
    assertSame(s3, q.top());
    q.put(s2);
    assertSame(s2, q.top()); // s2 should be the least scorer
    q.put(s4);
    assertSame(s2, q.top());
    q.put(s1);
    assertSame(s1, q.top()); // s1 should be the least scorer
  }

  @Test
  public void testNextCandidateDocumentAndAdjustElsePop() throws IOException {
    this.addDocument(writer, "\"term1\" \"term2\" . \"term3\" .  \"term4\" . ");
    this.addDocument(writer, "\"term5\" \"term2\" . \"term3\" .  ");

    final NodeDisjunctionScorerQueue q = new NodeDisjunctionScorerQueue(4);
    q.put(this.getScorer(ntq("term2")));
    q.put(this.getScorer(ntq("term3")));
    q.put(this.getScorer(ntq("term4")));
    q.put(this.getScorer(ntq("term5")));

    assertEquals(0, q.doc());
    assertEquals(4, q.size());
    assertTrue(q.nextCandidateDocumentAndAdjustElsePop());
    assertEquals(1, q.doc());
    assertEquals(3, q.size()); // term4 scorer should have been removed
    assertFalse(q.nextCandidateDocumentAndAdjustElsePop());

    assertEquals(DocsAndNodesIterator.NO_MORE_DOC, q.doc());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, q.node());
  }

  @Test
  public void testNextNodeAndAdjust() throws IOException {
    this.addDocument(writer, "\"term1\" \"term2\" . \"term3\" .  \"term4\" . ");
    this.addDocument(writer, "\"term2\" \"term3\" . \"term5\" .  ");
    this.addDocument(writer, "\"term2\" \"term1\" . \"term5\" .  ");

    final NodeDisjunctionScorerQueue q = new NodeDisjunctionScorerQueue(2);
    q.put(this.getScorer(ntq("term1")));
    q.put(this.getScorer(ntq("term5")));

    assertEquals(0, q.doc());
    assertTrue(q.nextNodeAndAdjust());
    assertEquals(node(0,0), q.node());
    assertFalse(q.nextNodeAndAdjust());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, q.node());

    assertTrue(q.nextCandidateDocumentAndAdjustElsePop());
    assertEquals(1, q.doc());
    assertTrue(q.nextNodeAndAdjust());
    assertEquals(node(1,0), q.node());
    assertFalse(q.nextNodeAndAdjust());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, q.node());

    assertTrue(q.nextCandidateDocumentAndAdjustElsePop());
    assertEquals(2, q.doc());
    assertTrue(q.nextNodeAndAdjust());
    assertEquals(node(0,1), q.node());
    assertTrue(q.nextNodeAndAdjust());
    assertEquals(node(1,0), q.node());
    assertFalse(q.nextNodeAndAdjust());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, q.node());

    assertFalse(q.nextCandidateDocumentAndAdjustElsePop());

    assertEquals(DocsAndNodesIterator.NO_MORE_DOC, q.doc());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, q.node());
  }

  @Test
  public void testNrMatches() throws IOException {
    this.addDocument(writer, "\"term1\" \"term2\" . \"term3\" .  \"term4\" . ");
    this.addDocument(writer, "\"term2\" \"term3\" . \"term5\" .  ");
    this.addDocument(writer, "\"term2\" \"term1 term5\" .  ");

    final NodeDisjunctionScorerQueue q = new NodeDisjunctionScorerQueue(2);
    q.put(this.getScorer(ntq("term1")));
    q.put(this.getScorer(ntq("term5")));

    assertEquals(0, q.doc());
    assertTrue(q.nextNodeAndAdjust());
    q.countAndSumMatchers();
    // there must be 1 matchers for node {0,0}
    assertEquals(1, q.nrMatchersInNode());

    assertTrue(q.nextCandidateDocumentAndAdjustElsePop());
    assertEquals(1, q.doc());
    assertTrue(q.nextNodeAndAdjust());
    q.countAndSumMatchers();
    // there must be 1 matchers for node {1,0}
    assertEquals(1, q.nrMatchersInNode());

    assertTrue(q.nextCandidateDocumentAndAdjustElsePop());
    assertEquals(2, q.doc());
    assertTrue(q.nextNodeAndAdjust());
    q.countAndSumMatchers();
    // there must be 2 matchers for node {0,1}
    assertEquals(2, q.nrMatchersInNode());
  }

  @Test
  public void testScoreSum() throws IOException {
    this.addDocument(writer, "\"term1 term2 term3\" .  \"term4\" . ");

    final NodeScorer s1 = this.getScorer(ntq("term1"));
    final NodeScorer s2 = this.getScorer(ntq("term2"));
    final NodeScorer s3 = this.getScorer(ntq("term3"));

    final NodeDisjunctionScorerQueue q = new NodeDisjunctionScorerQueue(3);
    q.put(s1);
    q.put(s2);
    q.put(s3);

    assertEquals(0, q.doc());
    assertTrue(q.nextNodeAndAdjust());
    q.countAndSumMatchers();
    // there must be 3 matchers
    assertEquals(3, q.nrMatchersInNode());
    assertEquals(s1.score() + s2.score() + s3.score(), q.scoreInNode(), 0f);
  }

  @Test
  public void testskipToCandidateAndAdjustElsePop() throws IOException {
    this.addDocument(writer, "\"term1\" \"term2\" . ");
    this.addDocument(writer, "\"term3\" .  \"term1\" . ");
    this.addDocument(writer, "\"term2\" \"term3\" . ");
    this.addDocument(writer, "\"term3\" .  \"term1\" . ");
    this.addDocument(writer, "\"term3\" .  \"term3\" . ");

    final NodeDisjunctionScorerQueue q = new NodeDisjunctionScorerQueue(3);
    q.put(this.getScorer(ntq("term1")));
    q.put(this.getScorer(ntq("term2")));
    q.put(this.getScorer(ntq("term3")));

    assertEquals(0, q.doc());
    assertTrue(q.skipToCandidateAndAdjustElsePop(3));
    assertEquals(3, q.doc());
    assertEquals(2, q.size()); // term2 should have been removed
    assertTrue(q.nextNodeAndAdjust());
    assertEquals(node(0,0), q.node());
    assertTrue(q.nextNodeAndAdjust());
    assertEquals(node(1,0), q.node());
    assertFalse(q.nextNodeAndAdjust());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, q.node());

    assertTrue(q.skipToCandidateAndAdjustElsePop(2));
    assertEquals(3, q.doc()); // queue should have not moved
    q.skipToCandidateAndAdjustElsePop(3);
    assertEquals(3, q.doc()); // queue should have not moved
    assertTrue(q.skipToCandidateAndAdjustElsePop(4));
    assertEquals(4, q.doc());
    assertEquals(1, q.size()); // term1 should have been removed
    assertTrue(q.nextNodeAndAdjust());
    assertEquals(node(0,0), q.node());
    assertTrue(q.nextNodeAndAdjust());
    assertEquals(node(1,0), q.node());
    assertFalse(q.nextNodeAndAdjust());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, q.node());

    assertFalse(q.skipToCandidateAndAdjustElsePop(7));
    assertEquals(DocsAndNodesIterator.NO_MORE_DOC, q.doc());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, q.node());
  }

  @Test
  public void testNextNodeHeapTraversal() throws IOException {
    this.addDocument(writer, "\"term1 term3\" \"term5 term2\" . \"term1 term3\" .  \"term5 term4 term3\" . ");

    final NodeDisjunctionScorerQueue q = new NodeDisjunctionScorerQueue(4);
    q.put(this.getScorer(ntq("term2")));
    q.put(this.getScorer(ntq("term3")));
    q.put(this.getScorer(ntq("term4")));
    q.put(this.getScorer(ntq("term5")));

    // test if the heap traversal is done properly.
    for (int i = 0; i < 4; i++) {
      q.nextNodeAndAdjust();
    }
    assertFalse(q.nextNodeAndAdjust());
  }

}
