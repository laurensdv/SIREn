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
 * @author Renaud Delbru [ 23 Apr 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.index.codecs.siren10;

import static org.sindice.siren.analysis.MockSirenDocument.doc;
import static org.sindice.siren.analysis.MockSirenToken.node;
import static org.sindice.siren.analysis.MockSirenToken.token;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;
import org.sindice.siren.analysis.MockSirenDocument;
import org.sindice.siren.index.DocsAndNodesIterator;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.index.codecs.siren10.Siren10PostingsReader.Siren10DocsEnum;
import org.sindice.siren.index.codecs.siren10.Siren10PostingsReader.Siren10DocsNodesAndPositionsEnum;
import org.sindice.siren.util.BasicSirenTestCase;

public class TestSiren10PostingsFormat extends BasicSirenTestCase {

  @Override
  protected void configure() throws IOException {
    this.setAnalyzer(AnalyzerType.MOCK);
    this.setPostingsFormat(PostingsFormatType.SIREN_10);
  }

  @Test
  public void testSimpleNextDocument() throws IOException {
    this.addDocuments(
      doc(token("aaa", node(1)), token("bbb", node(1,0)), token("aaa", node(2))),
      doc(token("aaa", node(1,0)), token("bbb", node(1,0,1,0))),
      doc(token("aaa", node(5,3,6,3)), token("bbb", node(5,3,6,3,7)))
    );

    final AtomicReader aReader = SlowCompositeReaderWrapper.wrap(reader);
    final DocsEnum docsEnum = aReader.termDocsEnum(null, DEFAULT_TEST_FIELD, new BytesRef("aaa"), true);
    assertTrue(docsEnum instanceof Siren10DocsEnum);
    final Siren10DocsNodesAndPositionsEnum e = ((Siren10DocsEnum) docsEnum).getDocsNodesAndPositionsEnum();
    assertEquals(-1, e.doc());
    assertEquals(0, e.nodeFreqInDoc());
    assertTrue(e.nextDocument());
    assertEquals(0, e.doc());
    assertEquals(2, e.nodeFreqInDoc());
    assertTrue(e.nextDocument());
    assertEquals(1, e.doc());
    assertEquals(1, e.nodeFreqInDoc());
    assertTrue(e.nextDocument());
    assertEquals(2, e.doc());
    assertEquals(1, e.nodeFreqInDoc());

    assertFalse(e.nextDocument());
    assertEquals(DocsAndNodesIterator.NO_MORE_DOC, e.doc());
  }

  @Test
  public void testSkipDoc() throws IOException {
    final MockSirenDocument[] docs = new MockSirenDocument[2048];
    for (int i = 0; i < 2048; i += 4) {
      docs[i] = doc(token("aaa", node(1)), token("bbb", node(1,0)), token("aaa", node(2)));
      docs[i + 1] = doc(token("aaa", node(1,0)), token("bbb", node(1,0,1,0)));
      docs[i + 2] = doc(token("aaa", node(5,3,6,3)), token("bbb", node(5,3,6,3,7)));
      docs[i + 3] = doc(token("bbb", node(2,0)), token("aaa", node(5,3,6)));
    }
    this.addDocuments(docs);

    final AtomicReader aReader = SlowCompositeReaderWrapper.wrap(reader);
    final DocsEnum docsEnum = aReader.termDocsEnum(null, DEFAULT_TEST_FIELD, new BytesRef("aaa"), true);
    assertTrue(docsEnum instanceof Siren10DocsEnum);
    final Siren10DocsNodesAndPositionsEnum e = ((Siren10DocsEnum) docsEnum).getDocsNodesAndPositionsEnum();

    // first skip in skiplist is at 512
    assertTrue(e.skipTo(502));
    assertEquals(502, e.doc());
    assertEquals(1, e.nodeFreqInDoc());

    // must have used the second skip
    assertTrue(e.skipTo(1624));
    assertEquals(1624, e.doc());
    assertEquals(2, e.nodeFreqInDoc());

    // no other skip, must have used the linear scan
    assertTrue(e.skipTo(2000));
    assertEquals(2000, e.doc());
    assertEquals(2, e.nodeFreqInDoc());

    assertFalse(e.skipTo(256323));

  }

  @Test
  public void testSimpleNextNode() throws IOException {
    this.addDocuments(
      doc(token("aaa", node(1)), token("bbb", node(1,0)), token("aaa", node(2))),
      doc(token("aaa", node(1,0)), token("bbb", node(1,0,1,0))),
      doc(token("aaa", node(5,3,6,3)), token("bbb", node(5,3,6,3,7)))
    );

    final AtomicReader aReader = SlowCompositeReaderWrapper.wrap(reader);
    final DocsEnum docsEnum = aReader.termDocsEnum(null, DEFAULT_TEST_FIELD, new BytesRef("aaa"), true);
    assertTrue(docsEnum instanceof Siren10DocsEnum);
    final Siren10DocsNodesAndPositionsEnum e = ((Siren10DocsEnum) docsEnum).getDocsNodesAndPositionsEnum();
    assertEquals(-1, e.doc());
    assertEquals(0, e.nodeFreqInDoc());
    assertEquals(node(-1), e.node());

    assertTrue(e.nextDocument());
    assertEquals(0, e.doc());
    assertEquals(2, e.nodeFreqInDoc());
    assertTrue(e.nextNode());
    assertEquals(node(1), e.node());
    assertTrue(e.nextNode());
    assertEquals(node(2), e.node());
    assertFalse(e.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, e.node());

    assertTrue(e.nextDocument());
    assertEquals(1, e.doc());
    assertEquals(1, e.nodeFreqInDoc());
    assertTrue(e.nextNode());
    assertEquals(node(1,0), e.node());
    assertFalse(e.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, e.node());

    assertTrue(e.nextDocument());
    assertEquals(2, e.doc());
    assertEquals(1, e.nodeFreqInDoc());
    assertTrue(e.nextNode());
    assertEquals(node(5,3,6,3), e.node());
    assertFalse(e.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, e.node());

    assertFalse(e.nextDocument());
    assertEquals(DocsAndNodesIterator.NO_MORE_DOC, e.doc());
  }

  @Test
  public void testSimpleSkipNode() throws IOException {
    this.addDocuments(
      doc(token("aaa", node(1)), token("bbb", node(1,0)), token("aaa", node(2))),
      doc(token("aaa", node(1,0)), token("bbb", node(1,0,1,0))),
      doc(token("aaa", node(5,3,6,3)), token("bbb", node(5,3,6,3,7)))
    );

    final AtomicReader aReader = SlowCompositeReaderWrapper.wrap(reader);
    final DocsEnum docsEnum = aReader.termDocsEnum(null, DEFAULT_TEST_FIELD, new BytesRef("aaa"), true);
    assertTrue(docsEnum instanceof Siren10DocsEnum);
    final Siren10DocsNodesAndPositionsEnum e = ((Siren10DocsEnum) docsEnum).getDocsNodesAndPositionsEnum();
    assertEquals(-1, e.doc());
    assertEquals(0, e.nodeFreqInDoc());

    // skip to 2 using linear scan. Node should be also be skipped.
    assertTrue(e.skipTo(2));
    assertEquals(2, e.doc());
    assertEquals(1, e.nodeFreqInDoc());
    assertTrue(e.nextNode());
    assertEquals(node(5,3,6,3), e.node());
    assertFalse(e.nextNode());

    assertFalse(e.nextDocument());
  }

  @Test
  public void testSkipNode() throws IOException {
    final MockSirenDocument[] docs = new MockSirenDocument[2048];
    for (int i = 0; i < 2048; i += 4) {
      docs[i] = doc(token("aaa", node(1)), token("bbb", node(1,0)), token("aaa", node(2)));
      docs[i + 1] = doc(token("aaa", node(1,0)), token("bbb", node(1,0,1,0)));
      docs[i + 2] = doc(token("aaa", node(5,3,6,3)), token("bbb", node(5,3,6,3,7)));
      docs[i + 3] = doc(token("bbb", node(2,0)), token("aaa", node(5,3,6)));
    }
    this.addDocuments(docs);

    final AtomicReader aReader = SlowCompositeReaderWrapper.wrap(reader);
    final DocsEnum docsEnum = aReader.termDocsEnum(null, DEFAULT_TEST_FIELD, new BytesRef("aaa"), true);
    assertTrue(docsEnum instanceof Siren10DocsEnum);
    final Siren10DocsNodesAndPositionsEnum e = ((Siren10DocsEnum) docsEnum).getDocsNodesAndPositionsEnum();

    // first skip in skiplist is at 512
    assertTrue(e.skipTo(502));
    assertEquals(502, e.doc());
    assertEquals(1, e.nodeFreqInDoc());
    assertTrue(e.nextNode());
    assertEquals(node(5,3,6,3), e.node());
    assertFalse(e.nextNode());

    // skip to 504 and scan partially nodes
    assertTrue(e.nextDocument());
    assertTrue(e.nextDocument());
    assertEquals(504, e.doc());
    assertEquals(2, e.nodeFreqInDoc());
    assertTrue(e.nextNode());
    assertEquals(node(1), e.node());

    // must have used the second skip
    assertTrue(e.skipTo(1624));
    assertEquals(1624, e.doc());
    assertEquals(2, e.nodeFreqInDoc());
    assertTrue(e.nextNode());
    assertEquals(node(1), e.node());
    assertTrue(e.nextNode());
    assertEquals(node(2), e.node());
    assertFalse(e.nextNode());

    // no other skip, must have used the linear scan
    assertTrue(e.skipTo(2000));
    assertEquals(2000, e.doc());
    assertEquals(2, e.nodeFreqInDoc());
    assertTrue(e.nextNode());
    assertEquals(node(1), e.node());
    assertTrue(e.nextNode());
    assertEquals(node(2), e.node());
    assertFalse(e.nextNode());

    assertFalse(e.skipTo(256323));

  }

  @Test
  public void testSimpleNextPosition() throws IOException {
    this.addDocuments(
      doc(token("aaa", node(1)), token("bbb", node(1,0)), token("aaa", node(2))),
      doc(token("bbb", node(1,0)), token("bbb", node(1,0,1,0))),
      doc(token("bbb", node(5,3,6)), token("aaa", node(5,3,6,3)), token("aaa", node(5,3,6,3)))
    );

    final AtomicReader aReader = SlowCompositeReaderWrapper.wrap(reader);
    final DocsEnum docsEnum = aReader.termDocsEnum(null, DEFAULT_TEST_FIELD, new BytesRef("aaa"), true);
    assertTrue(docsEnum instanceof Siren10DocsEnum);
    final Siren10DocsNodesAndPositionsEnum e = ((Siren10DocsEnum) docsEnum).getDocsNodesAndPositionsEnum();
    assertEquals(-1, e.doc());
    assertEquals(0, e.nodeFreqInDoc());
    assertEquals(node(-1), e.node());
    assertEquals(-1, e.pos());

    assertTrue(e.nextDocument());
    assertEquals(0, e.doc());
    assertEquals(2, e.nodeFreqInDoc());

    assertTrue(e.nextNode());
    assertEquals(node(1), e.node());
    assertEquals(1, e.termFreqInNode());

    assertTrue(e.nextPosition());
    assertEquals(0, e.pos());
    assertFalse(e.nextPosition());

    assertTrue(e.nextNode());
    assertEquals(node(2), e.node());
    assertEquals(1, e.termFreqInNode());

    assertTrue(e.nextPosition());
    assertEquals(0, e.pos());
    assertFalse(e.nextPosition());

    assertFalse(e.nextNode());

    assertTrue(e.nextDocument());
    assertEquals(2, e.doc());
    assertEquals(1, e.nodeFreqInDoc());

    assertTrue(e.nextNode());
    assertEquals(node(5,3,6,3), e.node());
    assertEquals(2, e.termFreqInNode());

    assertTrue(e.nextPosition());
    assertEquals(0, e.pos());
    assertTrue(e.nextPosition());
    assertEquals(1, e.pos());
    assertFalse(e.nextPosition());

    assertFalse(e.nextNode());

    assertFalse(e.nextDocument());
  }

  @Test
  public void testSimpleFrequencies() throws IOException {
    this.addDocuments(
      doc(token("aaa", node(1)), token("bbb", node(1,0)), token("aaa", node(2))),
      doc(token("aaa", node(1)), token("aaa", node(1)), token("aaa", node(2)))
    );

    final AtomicReader aReader = SlowCompositeReaderWrapper.wrap(reader);
    final DocsEnum docsEnum = aReader.termDocsEnum(null, DEFAULT_TEST_FIELD, new BytesRef("aaa"), true);
    assertTrue(docsEnum instanceof Siren10DocsEnum);
    final Siren10DocsNodesAndPositionsEnum e = ((Siren10DocsEnum) docsEnum).getDocsNodesAndPositionsEnum();
    assertEquals(-1, e.doc());

    // freqs should be set to 0 at the beginning
    assertEquals(0, e.nodeFreqInDoc());
    assertEquals(0, e.termFreqInNode());

    // nodeFreqInDoc should be set after calling nextDocument
    assertTrue(e.nextDocument());
    assertEquals(2, e.nodeFreqInDoc());
    // termFreqInNode should be set to 0
    assertEquals(0, e.termFreqInNode());
    // calling termFreqInNode should not change the freq settings
    assertEquals(2, e.nodeFreqInDoc());

    // termFreqInNode should be set after calling nextNode
    assertTrue(e.nextNode());
    // nodeFreqInDoc and nodeFreqInDoc should not have changed of settings
    assertEquals(2, e.nodeFreqInDoc());
    // termFreqInNode should be set to 1
    assertEquals(1, e.termFreqInNode());
    // calling termFreqInNode should not change the freqs settings
    assertEquals(2, e.nodeFreqInDoc());

    // calling nextPosition should not change freqs settings
    assertTrue(e.nextPosition());
    assertEquals(2, e.nodeFreqInDoc());
    assertEquals(1, e.termFreqInNode());

    // partially scanned position should not have consequences on nodeFreqInDoc
    // settings
    assertTrue(e.nextDocument());
    assertEquals(2, e.nodeFreqInDoc());
    assertTrue(e.nextNode());
    assertEquals(2, e.termFreqInNode());
    assertTrue(e.nextPosition());
    assertEquals(2, e.termFreqInNode());
    assertTrue(e.nextNode());
    assertEquals(1, e.termFreqInNode());
  }

  @Test
  public void testSimpleMerge() throws IOException {
    this.addDocuments(
      doc(token("aaa", node(1)), token("bbb", node(1,0)), token("aaa", node(2)))
    );
    logger.debug("Index Files: {}", Arrays.toString(this.directory.listAll()));
    this.addDocuments(
      doc(token("aaa", node(1)), token("bbb", node(1,0)), token("aaa", node(2)))
    );
    logger.debug("Index Files: {}", Arrays.toString(this.directory.listAll()));

    this.forceMerge();

    final AtomicReader aReader = SlowCompositeReaderWrapper.wrap(reader);
    final DocsEnum docsEnum = aReader.termDocsEnum(null, DEFAULT_TEST_FIELD, new BytesRef("aaa"), true);
    assertTrue(docsEnum instanceof Siren10DocsEnum);
    final Siren10DocsNodesAndPositionsEnum e = ((Siren10DocsEnum) docsEnum).getDocsNodesAndPositionsEnum();

    assertTrue(e.nextDocument());
    assertEquals(0, e.doc());
    assertEquals(2, e.nodeFreqInDoc());
    assertTrue(e.nextNode());
    assertEquals(1, e.termFreqInNode());
    assertTrue(e.nextPosition());
    assertEquals(0, e.pos());
    assertTrue(e.nextNode());
    assertEquals(1, e.termFreqInNode());
    assertTrue(e.nextPosition());
    assertEquals(0, e.pos());

    assertTrue(e.nextDocument());
    assertEquals(1, e.doc());
    assertEquals(2, e.nodeFreqInDoc());
    assertTrue(e.nextNode());
    assertEquals(1, e.termFreqInNode());
    assertTrue(e.nextPosition());
    assertEquals(0, e.pos());
    assertTrue(e.nextNode());
    assertEquals(1, e.termFreqInNode());
    assertTrue(e.nextPosition());
    assertEquals(0, e.pos());
  }

  @Test
  public void testMergeBlockSize() throws IOException {
    // reduce block size
    this.setPostingsFormat(new Siren10PostingsFormat(2));

    this.addDocuments(
      doc(token("aaa", node(1)), token("bbb", node(1,0))),
      doc(token("aaa", node(1)), token("bbb", node(1,0))),
      doc(token("aaa", node(1)), token("bbb", node(1,0)))
    );
    logger.debug("Index Files: {}", Arrays.toString(this.directory.listAll()));
    logger.debug("numDocs: {}", this.reader.numDocs());

    this.addDocuments(
      doc(token("aaa", node(1)), token("bbb", node(1,0))),
      doc(token("aaa", node(1)), token("bbb", node(1,0)))
    );

    logger.debug("Index Files: {}", Arrays.toString(this.directory.listAll()));
    logger.debug("numDocs: {}", this.reader.numDocs());

    this.forceMerge();
  }

  @Test
  public void testStressMerge() throws IOException {
    this.addDocuments(
      doc(token("aaa", node(1)), token("bbb", node(1,0)), token("aaa", node(2)))
    );

    while (this.reader.numDocs() < 1000) {
      this.addDocuments(
        doc(token("aaa", node(1)), token("bbb", node(1,0)), token("aaa", node(2)))
      );
      this.forceMerge();
    }

    final AtomicReader aReader = SlowCompositeReaderWrapper.wrap(reader);
    final DocsEnum docsEnum = aReader.termDocsEnum(null, DEFAULT_TEST_FIELD, new BytesRef("aaa"), true);
    assertTrue(docsEnum instanceof Siren10DocsEnum);
    final Siren10DocsNodesAndPositionsEnum e = ((Siren10DocsEnum) docsEnum).getDocsNodesAndPositionsEnum();

    for (int i = 0; i < 1000; i++) {
      assertTrue(e.nextDocument());
      assertEquals(i, e.doc());
      assertEquals(2, e.nodeFreqInDoc());
    }
  }

  @Test
  public void testSkipDataCheckIndex() throws IOException {
    // The Lucene CheckIndex was catching a problem with how skip data level
    // were computed on this configuration.
    this.setPostingsFormat(new Siren10PostingsFormat(256));

    final MockSirenDocument[] docs = new MockSirenDocument[1000];

    for (int i = 0; i < 1000; i++) {
     docs[i] = doc(token("aaa", node(1)), token("bbb", node(1,0)), token("aaa", node(2)));
    }
    this.addDocuments(docs);

    final AtomicReader aReader = SlowCompositeReaderWrapper.wrap(reader);
    final DocsEnum docsEnum = aReader.termDocsEnum(null, DEFAULT_TEST_FIELD, new BytesRef("aaa"), true);
    assertTrue(docsEnum instanceof Siren10DocsEnum);
    final Siren10DocsNodesAndPositionsEnum e = ((Siren10DocsEnum) docsEnum).getDocsNodesAndPositionsEnum();
  }

}
