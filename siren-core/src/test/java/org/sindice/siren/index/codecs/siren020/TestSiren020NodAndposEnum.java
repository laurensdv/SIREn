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
 * @author Renaud Delbru [ 21 Apr 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.index.codecs.siren020;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.util.BytesRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sindice.siren.index.DocsNodesAndPositionsIterator;
import org.sindice.siren.util.BasicSirenTestCase;

public class TestSiren020NodAndposEnum extends BasicSirenTestCase {

  @Override
  @Before
  public void setUp()
  throws Exception {
    super.setUp();
  }

  @Override
  @After
  public void tearDown()
  throws Exception {
    super.tearDown();
  }

  protected Siren020NodAndPosEnum getEnum(final String term) throws IOException {
    final BytesRef ref = new BytesRef(term);
    final DocsAndPositionsEnum e = reader.termPositionsEnum(reader.getLiveDocs(), DEFAULT_FIELD, ref);
    return new Siren020NodAndPosEnum(e);
  }

  @Test
  public void testNextSimpleOccurence1()
  throws Exception {
    this.addDocument("\"word1\" . ");
    final Siren020NodAndPosEnum termEnum = this.getEnum("word1");

    // Should return false since nextDoc has not been called
    assertFalse(termEnum.nextNode());
    assertFalse(termEnum.nextPosition());

    assertTrue(termEnum.nextDocument());
    assertEquals(0, termEnum.doc());

    // node and position should be set to -1
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertEquals(1, termEnum.termFreqInDoc());

    // Should return false since nextDoc has not been called
    assertFalse(termEnum.nextPosition());

    assertTrue(termEnum.nextNode());
    assertEquals(0, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);

    // position should be set to -1
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextPosition());
    assertEquals(0, termEnum.pos());

    // end of the list
    assertFalse(termEnum.nextPosition());
    assertFalse(termEnum.nextNode());
    assertFalse(termEnum.nextDocument());

    // everything should be set to sentinel value
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_DOC, termEnum.doc());
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_NOD, termEnum.node()[0]);
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_NOD, termEnum.node()[1]);
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_POS, termEnum.pos());
  }

  @Test
  public void testNextSimpleOccurence2()
  throws Exception {
    this.addDocument("\"word1\" \"word2 word3 word4\" . ");
    final Siren020NodAndPosEnum termEnum = this.getEnum("word3");

    assertTrue(termEnum.nextDocument());
    assertEquals(0, termEnum.doc());

    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertEquals(1, termEnum.termFreqInDoc());

    // here it is assumed that the position of the term is the global position
    // in the document, and not within a cell.
    assertTrue(termEnum.nextNode());
    assertTrue(termEnum.nextPosition());
    assertEquals(0, termEnum.node()[0]);
    assertEquals(1, termEnum.node()[1]);
    assertEquals(2, termEnum.pos());

    // end of the list
    assertFalse(termEnum.nextPosition());
    assertFalse(termEnum.nextNode());
    assertFalse(termEnum.nextDocument());

    // everything should be set to sentinel value
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_DOC, termEnum.doc());
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_NOD, termEnum.node()[0]);
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_NOD, termEnum.node()[1]);
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_POS, termEnum.pos());
  }

  @Test
  public void testNextMultipleOccurences1()
  throws Exception {
    this.addDocument("\"word1 word1 word1\" . ");
    final Siren020NodAndPosEnum termEnum = this.getEnum("word1");

    assertTrue(termEnum.nextDocument());
    assertEquals(0, termEnum.doc());

    assertEquals(3, termEnum.termFreqInDoc());

    assertTrue(termEnum.nextNode());

    assertEquals(0, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);

    assertTrue(termEnum.nextPosition());
    assertEquals(0, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(2, termEnum.pos());

    // end of the list
    assertFalse(termEnum.nextPosition());
    assertFalse(termEnum.nextNode());
    assertFalse(termEnum.nextDocument());
  }

  @Test
  public void testNextNodeMultipleOccurencesInSameCell()
  throws Exception {
    this.addDocument("\"word1 word1 word1\" . ");
    final Siren020NodAndPosEnum termEnum = this.getEnum("word1");

    assertTrue(termEnum.nextDocument());
    assertEquals(0, termEnum.doc());

    assertEquals(3, termEnum.termFreqInDoc());

    assertTrue(termEnum.nextNode());

    assertEquals(0, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);

    assertFalse(termEnum.nextNode());

    // end of the list
    assertFalse(termEnum.nextPosition());
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_POS, termEnum.pos());
    assertFalse(termEnum.nextNode());
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_NOD, termEnum.node()[0]);
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_NOD, termEnum.node()[1]);
    assertFalse(termEnum.nextDocument());
  }

  @Test
  public void testNextMultipleOccurences2()
  throws Exception {
    this.addDocument("\"word1 word2\" \"word1\" . \"word1 word2\" . \"word1\" . ");
    final Siren020NodAndPosEnum termEnum = this.getEnum("word1");

    assertTrue(termEnum.nextDocument());
    assertEquals(0, termEnum.doc());

    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertEquals(4, termEnum.termFreqInDoc());

    assertTrue(termEnum.nextNode());
    assertEquals(0, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(0, termEnum.pos());
    assertFalse(termEnum.nextPosition());
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_POS, termEnum.pos());

    assertTrue(termEnum.nextNode());
    assertEquals(0, termEnum.node()[0]);
    assertEquals(1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(2, termEnum.pos());
    assertFalse(termEnum.nextPosition());
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_POS, termEnum.pos());

    assertTrue(termEnum.nextNode());
    assertEquals(1, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(3, termEnum.pos());
    assertFalse(termEnum.nextPosition());
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_POS, termEnum.pos());

    assertTrue(termEnum.nextNode());
    assertEquals(2, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(5, termEnum.pos());
    assertFalse(termEnum.nextPosition());
    assertEquals(DocsNodesAndPositionsIterator.NO_MORE_POS, termEnum.pos());

    // end of the list
    assertFalse(termEnum.nextPosition());
    assertFalse(termEnum.nextNode());
    assertFalse(termEnum.nextDocument());
  }

  @Test
  public void testAdvance()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 64; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    this.addDocumentsWithIterator(data);

    final Siren020NodAndPosEnum termEnum = this.getEnum("aaa");

    assertTrue(termEnum.nextDocument());

    termEnum.skipTo(16);
    assertEquals(16, termEnum.doc());
    assertEquals(4, termEnum.termFreqInDoc());
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextNode());

    termEnum.skipTo(33, new int[] { 1 });
    assertEquals(33, termEnum.doc());
    assertEquals(2, termEnum.termFreqInDoc());

    assertEquals(1, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextPosition());
    assertEquals(2, termEnum.pos());

    termEnum.skipTo(96, new int[] { 1, 1 });
    assertEquals(96, termEnum.doc());
    assertEquals(4, termEnum.termFreqInDoc());

    assertEquals(1, termEnum.node()[0]);
    assertEquals(1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextPosition());
    assertEquals(3, termEnum.pos());
  }

  /**
   * If the entity, tuple and cell are not found, it should return the first
   * match that is greater than the target. (SRN-17)
   */
  @Test
  public void testAdvanceNotFound()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" . \"aaa bbb\" . \"aaa ccc\" \"aaa bbb\" . ");
    }
    this.addDocumentsWithIterator(data);

    final Siren020NodAndPosEnum termEnum = this.getEnum("bbb");

    // Should move to the next entity, without updating tuple and cell
    // information
    assertTrue(termEnum.skipTo(16));
    assertEquals(17, termEnum.doc());
    assertEquals(3, termEnum.termFreqInDoc());
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    // Should jump to the third tuples
    assertTrue(termEnum.skipTo(17, new int[] { 1 }));
    assertEquals(17, termEnum.doc());
    assertEquals(3, termEnum.termFreqInDoc());
    assertEquals(2, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextPosition());
    assertEquals(5, termEnum.pos());

    // Should jump to the second cell
    assertTrue(termEnum.skipTo(17, new int[] { 3, 0 }));
    assertEquals(17, termEnum.doc());
    assertEquals(3, termEnum.termFreqInDoc());
    assertEquals(3, termEnum.node()[0]);
    assertEquals(1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextPosition());
    assertEquals(9, termEnum.pos());
  }

  @Test
  public void testAdvanceSameEntity()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 64; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    this.addDocumentsWithIterator(data);
    final Siren020NodAndPosEnum termEnum = this.getEnum("aaa");

    termEnum.skipTo(16);
    assertEquals(16, termEnum.doc());
    assertEquals(4, termEnum.termFreqInDoc());
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    termEnum.skipTo(16, new int[] { 1 });
    assertEquals(16, termEnum.doc());
    assertEquals(4, termEnum.termFreqInDoc());
    assertEquals(1, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextPosition());
    assertEquals(2, termEnum.pos());

    termEnum.skipTo(16, new int[] { 1, 1 });
    assertEquals(16, termEnum.doc());
    assertEquals(4, termEnum.termFreqInDoc());
    assertEquals(1, termEnum.node()[0]);
    assertEquals(1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextPosition());
    assertEquals(3, termEnum.pos());
  }

  @Test
  public void testAdvanceEntityNextNodeAndNextPosition()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    this.addDocumentsWithIterator(data);
    final Siren020NodAndPosEnum termEnum = this.getEnum("aaa");

    termEnum.skipTo(16);
    assertEquals(16, termEnum.doc());
    assertEquals(4, termEnum.termFreqInDoc());
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextNode());
    assertEquals(0, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(0, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(1, termEnum.pos());
    assertFalse(termEnum.nextPosition());

    assertTrue(termEnum.nextNode());
    assertEquals(1, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(2, termEnum.pos());
    assertFalse(termEnum.nextPosition());

    assertTrue(termEnum.nextNode());
    assertEquals(1, termEnum.node()[0]);
    assertEquals(1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(3, termEnum.pos());
    assertFalse(termEnum.nextPosition());

    assertFalse(termEnum.nextNode());
  }

  @Test
  public void testAdvanceToCellNextPosition()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    this.addDocumentsWithIterator(data);
    final Siren020NodAndPosEnum termEnum = this.getEnum("aaa");

    termEnum.skipTo(16, new int[] { 1, 0 });
    assertEquals(16, termEnum.doc());
    assertEquals(1, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextPosition());
    assertEquals(2, termEnum.pos());
    assertFalse(termEnum.nextPosition());
  }

  @Test
  public void testAdvanceNextDoc()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    this.addDocumentsWithIterator(data);
    final Siren020NodAndPosEnum termEnum = this.getEnum("aaa");

    termEnum.nextDocument();
    assertEquals(0, termEnum.doc());

    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    termEnum.skipTo(16);
    assertEquals(16, termEnum.doc());

    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    termEnum.nextDocument();
    assertEquals(17, termEnum.doc());

    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
  }

  @Test
  public void testAdvanceNonExistingEntityTupleCell()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 16; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    this.addDocumentsWithIterator(data);
    final Siren020NodAndPosEnum termEnum = this.getEnum("aaa");

    // does not exist, should skip to entity 17 and to the first cell
    assertTrue(termEnum.skipTo(16, new int[] { 3, 2 }));
    assertEquals(17, termEnum.doc());

    assertEquals(0, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(0, termEnum.pos());
    assertFalse(termEnum.nextPosition());

    // does not exist, should skip to entity 19 and to the first cell
    assertTrue(termEnum.skipTo(18, new int[] { 2, 2 }));
    assertEquals(19, termEnum.doc());

    assertEquals(0, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(0, termEnum.pos());
    assertFalse(termEnum.nextPosition());

    assertFalse(termEnum.skipTo(31, new int[] { 2, 0 })); // does not exist, reach end of list: should return false
  }

}
