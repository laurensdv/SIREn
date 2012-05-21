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

import static org.sindice.siren.analysis.MockSirenDocument.doc;
import static org.sindice.siren.analysis.MockSirenToken.node;
import static org.sindice.siren.analysis.MockSirenToken.token;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;
import org.sindice.siren.index.DocsAndNodesIterator;
import org.sindice.siren.index.PositionsIterator;
import org.sindice.siren.util.BasicSirenTestCase;

public class TestSiren020DocNodAndPosEnum extends BasicSirenTestCase {

  protected Siren020DocNodAndPosEnum getEnum(final String term) throws IOException {
    final BytesRef ref = new BytesRef(term);
    final AtomicReader r = (AtomicReader) reader;
    final DocsAndPositionsEnum e = r.termPositionsEnum(r.getLiveDocs(), DEFAULT_FIELD, ref, false);
    return new Siren020DocNodAndPosEnum(e);
  }

  @Test
  public void testNextSimpleOccurence1()
  throws Exception {
    this.addDocument("\"word1\" . ");
    final Siren020DocNodAndPosEnum termEnum = this.getEnum("word1");

    // Should return false since nextDoc has not been called
    assertFalse(termEnum.nextNode());
    assertFalse(termEnum.nextPosition());

    assertTrue(termEnum.nextDocument());
    assertEquals(0, termEnum.doc());

    // node and position should be set to -1
    assertEquals(node(-1), termEnum.node());
    assertEquals(-1, termEnum.pos());

    assertEquals(1, termEnum.termFreqInDoc());

    // Should return false since nextNode has not been called
    assertFalse(termEnum.nextPosition());

    assertTrue(termEnum.nextNode());
    assertEquals(node(0,0), termEnum.node());

    // position should be set to -1
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextPosition());
    assertEquals(0, termEnum.pos());

    // end of the list
    assertFalse(termEnum.nextPosition());
    assertFalse(termEnum.nextNode());
    assertFalse(termEnum.nextDocument());

    // everything should be set to sentinel value
    assertEquals(DocsAndNodesIterator.NO_MORE_DOC, termEnum.doc());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, termEnum.node());
    assertEquals(PositionsIterator.NO_MORE_POS, termEnum.pos());
  }

  @Test
  public void testNextSimpleOccurence2()
  throws Exception {
    this.addDocument("\"word1\" \"word2 word3 word4\" . ");
    final Siren020DocNodAndPosEnum termEnum = this.getEnum("word3");

    assertTrue(termEnum.nextDocument());
    assertEquals(0, termEnum.doc());

    assertEquals(node(-1), termEnum.node());
    assertEquals(-1, termEnum.pos());

    assertEquals(1, termEnum.termFreqInDoc());

    // here it is assumed that the position of the term is the global position
    // in the document, and not within a cell.
    assertTrue(termEnum.nextNode());
    assertTrue(termEnum.nextPosition());
    assertEquals(node(0,1), termEnum.node());
    assertEquals(2, termEnum.pos());

    // end of the list
    assertFalse(termEnum.nextPosition());
    assertFalse(termEnum.nextNode());
    assertFalse(termEnum.nextDocument());

    // everything should be set to sentinel value
    assertEquals(DocsAndNodesIterator.NO_MORE_DOC, termEnum.doc());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, termEnum.node());
    assertEquals(PositionsIterator.NO_MORE_POS, termEnum.pos());
  }

  @Test
  public void testNextMultipleOccurences1()
  throws Exception {
    this.addDocument("\"word1 word1 word1\" . ");
    final Siren020DocNodAndPosEnum termEnum = this.getEnum("word1");

    assertTrue(termEnum.nextDocument());
    assertEquals(0, termEnum.doc());

    assertEquals(3, termEnum.termFreqInDoc());

    assertTrue(termEnum.nextNode());

    assertEquals(node(0,0), termEnum.node());

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
    final Siren020DocNodAndPosEnum termEnum = this.getEnum("word1");

    assertTrue(termEnum.nextDocument());
    assertEquals(0, termEnum.doc());

    assertEquals(3, termEnum.termFreqInDoc());

    assertTrue(termEnum.nextNode());

    assertEquals(node(0,0), termEnum.node());

    assertFalse(termEnum.nextNode());

    // end of the list
    assertFalse(termEnum.nextPosition());
    assertEquals(PositionsIterator.NO_MORE_POS, termEnum.pos());
    assertFalse(termEnum.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, termEnum.node());
    assertFalse(termEnum.nextDocument());
  }

  @Test
  public void testNextMultipleOccurences2()
  throws Exception {
    this.addDocument("\"word1 word2\" \"word1\" . \"word1 word2\" . \"word1\" . ");
    final Siren020DocNodAndPosEnum termEnum = this.getEnum("word1");

    assertTrue(termEnum.nextDocument());
    assertEquals(0, termEnum.doc());

    assertEquals(node(-1), termEnum.node());
    assertEquals(-1, termEnum.pos());

    assertEquals(4, termEnum.termFreqInDoc());

    assertTrue(termEnum.nextNode());
    assertEquals(node(0,0), termEnum.node());
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(0, termEnum.pos());
    assertFalse(termEnum.nextPosition());
    assertEquals(PositionsIterator.NO_MORE_POS, termEnum.pos());

    assertTrue(termEnum.nextNode());
    assertEquals(node(0,1), termEnum.node());
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(2, termEnum.pos());
    assertFalse(termEnum.nextPosition());
    assertEquals(PositionsIterator.NO_MORE_POS, termEnum.pos());

    assertTrue(termEnum.nextNode());
    assertEquals(node(1,0), termEnum.node());
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(3, termEnum.pos());
    assertFalse(termEnum.nextPosition());
    assertEquals(PositionsIterator.NO_MORE_POS, termEnum.pos());

    assertTrue(termEnum.nextNode());
    assertEquals(node(2,0), termEnum.node());
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(5, termEnum.pos());
    assertFalse(termEnum.nextPosition());
    assertEquals(PositionsIterator.NO_MORE_POS, termEnum.pos());

    // end of the list
    assertFalse(termEnum.nextPosition());
    assertFalse(termEnum.nextNode());
    assertFalse(termEnum.nextDocument());
  }

  @Test
  public void testSkipTo()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 64; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    this.addDocumentsWithIterator(data);

    final Siren020DocNodAndPosEnum termEnum = this.getEnum("aaa");

    assertTrue(termEnum.nextDocument());

    termEnum.skipTo(16);
    assertEquals(16, termEnum.doc());
    assertEquals(4, termEnum.termFreqInDoc());
    assertEquals(node(-1), termEnum.node());
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextNode());
    assertEquals(node(0,0), termEnum.node());
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextPosition());
    assertEquals(0, termEnum.pos());

    assertTrue(termEnum.nextNode());
    assertEquals(node(1,0), termEnum.node());
    assertEquals(-1, termEnum.pos());

    termEnum.skipTo(33);
    assertEquals(33, termEnum.doc());
    assertEquals(2, termEnum.termFreqInDoc());
    assertEquals(node(-1), termEnum.node());
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextNode());
    assertEquals(node(0,0), termEnum.node());
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextNode());
    assertEquals(node(1,0), termEnum.node());
    assertEquals(-1, termEnum.pos());
  }

  /**
   * If the document and node are not found, it should return the first
   * match that is greater than the target. (SRN-17)
   */
  @Test
  public void testSkipToNotFound()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" . \"aaa bbb\" . \"aaa ccc\" \"aaa bbb\" . ");
    }
    this.addDocumentsWithIterator(data);

    final Siren020DocNodAndPosEnum termEnum = this.getEnum("bbb");

    // Should move to the next entity, without updating tuple and cell
    // information
    assertTrue(termEnum.skipTo(16));
    assertEquals(17, termEnum.doc());
    assertEquals(3, termEnum.termFreqInDoc());
    assertEquals(node(-1), termEnum.node());
    assertEquals(-1, termEnum.pos());

    assertFalse(termEnum.skipTo(75));
  }

  @Test
  public void testSkipToSameDocument()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 64; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    this.addDocumentsWithIterator(data);
    final Siren020DocNodAndPosEnum termEnum = this.getEnum("aaa");

    termEnum.skipTo(16);
    assertEquals(16, termEnum.doc());
    assertEquals(4, termEnum.termFreqInDoc());
    assertEquals(node(-1), termEnum.node());
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.skipTo(16));
    assertEquals(16, termEnum.doc());
  }

  @Test
  public void testSkipToNextNodeAndNextPosition()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    this.addDocumentsWithIterator(data);
    final Siren020DocNodAndPosEnum termEnum = this.getEnum("aaa");

    termEnum.skipTo(16);
    assertEquals(16, termEnum.doc());
    assertEquals(4, termEnum.termFreqInDoc());
    assertEquals(node(-1), termEnum.node());
    assertEquals(-1, termEnum.pos());

    assertTrue(termEnum.nextNode());
    assertEquals(node(0,0), termEnum.node());
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(0, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(1, termEnum.pos());
    assertFalse(termEnum.nextPosition());

    assertTrue(termEnum.nextNode());
    assertEquals(node(1,0), termEnum.node());
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(2, termEnum.pos());
    assertFalse(termEnum.nextPosition());

    assertTrue(termEnum.nextNode());
    assertEquals(node(1,1), termEnum.node());
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextPosition());
    assertEquals(3, termEnum.pos());
    assertFalse(termEnum.nextPosition());

    assertFalse(termEnum.nextNode());
  }

  @Test
  public void testSkipToNextDoc()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    this.addDocumentsWithIterator(data);
    final Siren020DocNodAndPosEnum termEnum = this.getEnum("aaa");

    termEnum.nextDocument();
    assertEquals(0, termEnum.doc());

    assertEquals(node(-1), termEnum.node());
    assertEquals(-1, termEnum.pos());

    termEnum.skipTo(16);
    assertEquals(16, termEnum.doc());

    assertEquals(node(-1), termEnum.node());
    assertEquals(-1, termEnum.pos());

    termEnum.nextDocument();
    assertEquals(17, termEnum.doc());

    assertEquals(node(-1), termEnum.node());
    assertEquals(-1, termEnum.pos());
  }

  @Test
  public void testVariableNodeLength() throws Exception {
    this.addDocuments(
      doc(token("aaa", node(1)), token("aaa", node(1,0)), token("aaa", node(2))),
      doc(token("aaa", node(1,0,1,0)), token("aaa", node(1,0)))
    );

    final Siren020DocNodAndPosEnum termEnum = this.getEnum("aaa");

    assertTrue(termEnum.nextDocument());
    assertEquals(0, termEnum.doc());
    assertEquals(node(-1), termEnum.node());
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextNode());
    assertEquals(node(1), termEnum.node());
    assertTrue(termEnum.nextNode());
    assertEquals(node(1,0), termEnum.node());
    assertTrue(termEnum.nextNode());
    assertEquals(node(2), termEnum.node());
    assertFalse(termEnum.nextNode());

    assertTrue(termEnum.nextDocument());
    assertEquals(1, termEnum.doc());
    assertEquals(node(-1), termEnum.node());
    assertEquals(-1, termEnum.pos());
    assertTrue(termEnum.nextNode());
    assertEquals(node(1,0,1,0), termEnum.node());
    assertTrue(termEnum.nextNode());
    assertEquals(node(1,0), termEnum.node());
    assertFalse(termEnum.nextNode());
  }

}
