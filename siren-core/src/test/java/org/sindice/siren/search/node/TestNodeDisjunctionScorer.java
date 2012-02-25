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

import java.util.ArrayList;

import org.junit.Test;
import org.sindice.siren.index.DocsAndNodesIterator;
import org.sindice.siren.search.AbstractTestSirenScorer;

public class TestNodeDisjunctionScorer extends AbstractTestSirenScorer {

  @Test
  public void testNextCandidateNextNode()
  throws Exception {
    this.deleteAll(writer);
    this.addDocumentsWithIterator(writer, new String[] { "<http://renaud.delbru.fr/> . ",
      "<http://sindice.com/test/name> \"Renaud Delbru\" . ",
      "<http://sindice.com/test/type> <http://sindice.com/test/Person> . ",
      "<aaa> <bbb> . <http://sindice.com/test/name> \"R. Delbru\" . " });

    final NodeBooleanScorer scorer =
      this.getDisjunctionScorer(new String[] {"renaud", "delbru"});

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

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(3, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertTrue(scorer.nextNode());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

    assertFalse(scorer.nextCandidateDocument());
    assertEquals(DocsAndNodesIterator.NO_MORE_DOC, scorer.doc());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());
  }

  @Test
  public void testSkipToCandidateNextNode()
  throws Exception {
    this.deleteAll(writer);
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 16; i++) {
      docs.add("\"aaa bbb\" \"aaa ccc\" . \"ccc\" \"bbb ccc\" .");
      docs.add("\"aaa ccc bbb\" . \"aaa aaa ccc bbb bbb\" . ");
    }
    this.addDocumentsWithIterator(writer, docs);

    final NodeBooleanScorer scorer =
      this.getDisjunctionScorer(new String[] {"aaa", "bbb"});

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.skipToCandidate(16));
    assertEquals(16, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.nextCandidateDocument());
    assertEquals(17, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.skipToCandidate(20));
    assertEquals(20, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.nextCandidateDocument());
    assertEquals(21, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertTrue(scorer.skipToCandidate(30));
    assertEquals(30, scorer.doc());
    assertEquals(-1, scorer.node()[0]);
    assertTrue(scorer.nextNode());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);
    assertFalse(scorer.skipToCandidate(34));
    assertEquals(DocsAndNodesIterator.NO_MORE_DOC, scorer.doc());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());
  }

}
