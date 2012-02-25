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

import org.junit.Test;
import org.sindice.siren.index.DocsAndNodesIterator;
import org.sindice.siren.search.AbstractTestSirenScorer;

public class TestNodeBooleanScorer extends AbstractTestSirenScorer {

  @Test
  public void testNextReqOpt() throws Exception {
    this.deleteAll(writer);
    this.addDocumentsWithIterator(writer,
      new String[] { "\"aaa bbb\" \"aaa ccc\" . \"ccc\" \"bbb ccc\" . ",
                                                    "\"aaa\" \"aaa bbb\" . " });

    final NodeBooleanScorer scorer = this.getBooleanScorer(new String[] {"aaa"},
      new String[] {"bbb"}, null);

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertFalse(scorer.nextCandidateDocument());
    assertEquals(DocsAndNodesIterator.NO_MORE_DOC, scorer.doc());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());
  }

  @Test
  public void testNextOptExcl() throws Exception {
    this.deleteAll(writer);
    this.addDocumentsWithIterator(writer,
      new String[] { "\"aaa bbb\" \"aaa ccc\" . \"ccc\" \"bbb ccc\" . ",
                                                    "\"aaa\" \"aaa bbb\" . " });

    final NodeBooleanScorer scorer = this.getBooleanScorer(null,
      new String[] {"bbb"}, new String[] {"ccc"});

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertFalse(scorer.nextCandidateDocument());
    assertEquals(DocsAndNodesIterator.NO_MORE_DOC, scorer.doc());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());
  }

  @Test
  public void testNextReqOptExcl() throws Exception {
    this.deleteAll(writer);
    this.addDocumentsWithIterator(writer,
      new String[] { "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" . ",
                                                    "\"aaa\" \"aaa bbb\" . " });

    final NodeBooleanScorer scorer = this.getBooleanScorer(new String[] {"aaa"},
      new String[] {"bbb"}, new String[] {"ccc"});

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertFalse(scorer.nextCandidateDocument());
    assertEquals(DocsAndNodesIterator.NO_MORE_DOC, scorer.doc());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());
  }

  @Test
  public void testNextReqOptExclWithConstraint() throws Exception {
    this.deleteAll(writer);
    this.addDocumentsWithIterator(writer,
      new String[] { "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" . ",
                                                    "\"aaa\" \"aaa bbb\" . " });

    final NodeBooleanScorer scorer = this.getBooleanScorer(new String[] {"aaa"},
      new String[] {"bbb"}, new String[] {"ccc"},
      new int[]{0,1}, new int[]{0,1}, true);

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(1, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertFalse(scorer.nextCandidateDocument());
    assertEquals(DocsAndNodesIterator.NO_MORE_DOC, scorer.doc());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());
  }

}
