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
 * @author Renaud Delbru [ 1 Apr 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.index.codecs.siren10;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.codecs.BlockTermState;
import org.apache.lucene.codecs.PostingsReaderBase;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexFileNames;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.index.TermState;
import org.apache.lucene.store.ByteArrayDataInput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CodecUtil;
import org.apache.lucene.util.IntsRef;
import org.sindice.siren.analysis.filter.VIntPayloadCodec;
import org.sindice.siren.index.DocsNodesAndPositionsEnum;
import org.sindice.siren.index.codecs.block.BlockIndexInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete class that reads the current docs, freqs, nodes positions and block
 * skip postings format.
 */
public class Siren10PostingsReader extends PostingsReaderBase {

  final DocsFreqBlockIndexInput docIn;
  final NodBlockIndexInput nodIn;
  final PosBlockIndexInput posIn;

  final IndexInput skipIn;

  int blockSkipInterval;
  int maxSkipLevels;
  int blockSkipMinimum;
  int maxBlockSize;

  protected static final Logger logger = LoggerFactory.getLogger(Siren10PostingsReader.class);

  public Siren10PostingsReader(final Directory dir, final SegmentInfo segmentInfo,
                               final IOContext context, final String segmentSuffix,
                               final Siren10BlockStreamFactory factory)
  throws IOException {
    boolean success = false;
    try {
      final String docFileName = IndexFileNames.segmentFileName(segmentInfo.name,
        segmentSuffix, Siren10PostingsWriter.DOC_EXTENSION);
      docIn = factory.openDocsFreqInput(dir, docFileName, context);

      nodIn = factory.openNodInput(dir, IndexFileNames.segmentFileName(segmentInfo.name,
        segmentSuffix, Siren10PostingsWriter.NOD_EXTENSION), context);

      skipIn = dir.openInput(IndexFileNames.segmentFileName(segmentInfo.name,
        segmentSuffix, Siren10PostingsWriter.SKIP_EXTENSION), context);

      posIn = factory.openPosInput(dir, IndexFileNames.segmentFileName(segmentInfo.name,
        segmentSuffix, Siren10PostingsWriter.POS_EXTENSION), context);

      success = true;
    }
    finally {
      if (!success) {
        this.close();
      }
    }
  }

  public static void files(final SegmentInfo segmentInfo, final String segmentSuffix, final Collection<String> files) throws IOException {
    files.add(IndexFileNames.segmentFileName(segmentInfo.name, segmentSuffix, Siren10PostingsWriter.DOC_EXTENSION));
    files.add(IndexFileNames.segmentFileName(segmentInfo.name, segmentSuffix, Siren10PostingsWriter.NOD_EXTENSION));
    files.add(IndexFileNames.segmentFileName(segmentInfo.name, segmentSuffix, Siren10PostingsWriter.SKIP_EXTENSION));

    files.add(IndexFileNames.segmentFileName(segmentInfo.name, segmentSuffix, Siren10PostingsWriter.POS_EXTENSION));
  }

  @Override
  public void init(final IndexInput termsIn) throws IOException {
    // Make sure we are talking to the matching past writer
    CodecUtil.checkHeader(termsIn, Siren10PostingsWriter.CODEC,
      Siren10PostingsWriter.VERSION_START, Siren10PostingsWriter.VERSION_START);
    blockSkipInterval = termsIn.readInt();
    maxSkipLevels = termsIn.readInt();
    blockSkipMinimum = termsIn.readInt();
    maxBlockSize = termsIn.readInt();
  }

  @Override
  public void close() throws IOException {
    try {
      if (nodIn != null)
        nodIn.close();
    } finally {
      try {
        if (docIn != null)
          docIn.close();
      } finally {
        try {
          if (skipIn != null)
            skipIn.close();
        } finally {
          if (posIn != null) {
            posIn.close();
          }
        }
      }
    }
  }

  private static final class SepTermState extends BlockTermState {
    // We store only the seek point to the docs file because
    // the rest of the info (freqIndex, posIndex, etc.) is
    // stored in the docs file:
    BlockIndexInput.Index docIndex;

    long skipFP;
    int blockCount;

    // Only used for "primary" term state; these are never
    // copied on clone:

    // TODO: these should somehow be stored per-TermsEnum
    // not per TermState; maybe somehow the terms dict
    // should load/manage the byte[]/DataReader for us?
    byte[] bytes;
    ByteArrayDataInput bytesReader;

    @Override
    public SepTermState clone() {
      final SepTermState other = new SepTermState();
      other.copyFrom(this);
      return other;
    }

    @Override
    public void copyFrom(final TermState _other) {
      super.copyFrom(_other);
      final SepTermState other = (SepTermState) _other;

      blockCount = other.blockCount;

      if (docIndex == null) {
        docIndex = (BlockIndexInput.Index) other.docIndex.clone();
      }
      else {
        docIndex.set(other.docIndex);
      }

      skipFP = other.skipFP;
    }

    @Override
    public String toString() {
      return super.toString() + " docIndex=" + docIndex + " skipFP=" + skipFP
        + " blockCount=" + blockCount;
    }
  }

  @Override
  public BlockTermState newTermState() throws IOException {
    final SepTermState state = new SepTermState();
    state.docIndex = docIn.index();
    return state;
  }

  @Override
  public void readTermsBlock(final IndexInput termsIn, final FieldInfo fieldInfo,
                             final BlockTermState _termState) throws IOException {
    final SepTermState termState = (SepTermState) _termState;

    final int len = termsIn.readVInt();

    if (termState.bytes == null) {
      termState.bytes = new byte[ArrayUtil.oversize(len, 1)];
      termState.bytesReader = new ByteArrayDataInput(termState.bytes);
    }
    else if (termState.bytes.length < len) {
      termState.bytes = new byte[ArrayUtil.oversize(len, 1)];
    }

    termState.bytesReader.reset(termState.bytes, 0, len);
    termsIn.readBytes(termState.bytes, 0, len);
  }

  @Override
  public void nextTerm(final FieldInfo fieldInfo, final BlockTermState _termState) throws IOException {
    final SepTermState termState = (SepTermState) _termState;
    final boolean isFirstTerm = termState.termBlockOrd == 0;

    termState.blockCount = termState.bytesReader.readVInt();

    termState.docIndex.read(termState.bytesReader, isFirstTerm);

    if (termState.blockCount >= blockSkipMinimum) {
      if (isFirstTerm) {
        termState.skipFP = termState.bytesReader.readVLong();
      } else {
        termState.skipFP += termState.bytesReader.readVLong();
      }
    }
    else if (isFirstTerm) {
      termState.skipFP = 0;
    }
  }

  @Override
  public DocsEnum docs(final FieldInfo fieldInfo, final BlockTermState _termState,
                       final Bits liveDocs, final DocsEnum reuse,
                       final boolean needsFreqs) throws IOException {
    if (needsFreqs && fieldInfo.indexOptions == IndexOptions.DOCS_ONLY) {
      return null;
    }
    final SepTermState termState = (SepTermState) _termState;
    Siren10DocsEnum docsEnum;
    if (reuse == null || !(reuse instanceof Siren10DocsEnum)) {
      docsEnum = new Siren10DocsEnum();
    }
    else {
      docsEnum = (Siren10DocsEnum) reuse;
      if (docsEnum.getDocNodesEnum().startDocIn != docIn) {
        // If you are using ParellelReader, and pass in a
        // reused DocsAndPositionsEnum, it could have come
        // from another reader also using sep codec
        docsEnum = new Siren10DocsEnum();
      }
    }

    return docsEnum.init(fieldInfo, termState, liveDocs);
  }

  // TODO: Can we simplify this ? This is practically the same piece of code
  // than docs(). Do we have to test for the index options here ?
  @Override
  public DocsAndPositionsEnum docsAndPositions(final FieldInfo fieldInfo,
                                               final BlockTermState _termState,
                                               final Bits liveDocs,
                                               final DocsAndPositionsEnum reuse,
                                               final boolean needsOffsets)
  throws IOException {

    if (fieldInfo.indexOptions.compareTo(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS) < 0) {
      return null;
    }

    if (needsOffsets) {
      return null;
    }

    assert fieldInfo.indexOptions == IndexOptions.DOCS_AND_FREQS_AND_POSITIONS;
    final SepTermState termState = (SepTermState) _termState;
    Siren10DocsEnum postingsEnum;
    if (reuse == null || !(reuse instanceof Siren10DocsEnum)) {
      postingsEnum = new Siren10DocsEnum();
    }
    else {
      postingsEnum = (Siren10DocsEnum) reuse;
      if (postingsEnum.getDocNodesEnum().startDocIn != docIn) {
        // If you are using ParellelReader, and pass in a
        // reused DocsAndPositionsEnum, it could have come
        // from another reader also using sep codec
        postingsEnum = new Siren10DocsEnum();
      }
    }

    return postingsEnum.init(fieldInfo, termState, liveDocs);
  }

  class Siren10DocsEnum extends DocsAndPositionsEnum {

    private final Siren10DocsNodesAndPositionsEnum docEnum;

    private int freq = 0;

    Siren10DocsEnum() throws IOException {
      docEnum = new Siren10DocsNodesAndPositionsEnum();
    }

    Siren10DocsEnum init(final FieldInfo fieldInfo, final SepTermState termState, final Bits liveDocs)
    throws IOException {
      docEnum.init(fieldInfo, termState, liveDocs);
      freq = 0;
      return this;
    }

    public Siren10DocsNodesAndPositionsEnum getDocNodesEnum() {
      return docEnum;
    }

    @Override
    public int nextDoc() throws IOException {
      docEnum.nextDocument();
      // cannot perform lazy loading of freq since #freq() does not allow exception
      // see: LUCENE-4046
      freq = docEnum.termFreqInDoc();
      return docEnum.doc();
    }

    @Override
    public int freq() {
      return freq;
    }

    @Override
    public int docID() {
      return docEnum.doc();
    }

    @Override
    public int advance(final int target) throws IOException {
      docEnum.skipTo(target);
      // cannot perform lazy loading of freq since #freq() does not allow exception
      // see: LUCENE-4046
      freq = docEnum.termFreqInDoc();
      return docEnum.doc();
    }

    @Override
    public int nextPosition() throws IOException {
      while (!docEnum.nextPosition()) { // while no more pos
        if (!docEnum.nextNode()) { // move to next node
          break; // if no more node, break loop
        }
      }
      // if no more node, should return sentinel value
      return docEnum.pos();
    }

    @Override
    public int startOffset() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public int endOffset() throws IOException {
      throw new UnsupportedOperationException();
    }

    private final VIntPayloadCodec sirenPayload = new VIntPayloadCodec();

    @Override
    public BytesRef getPayload() throws IOException {
      return sirenPayload.encode(docEnum.node());
    }

    @Override
    public boolean hasPayload() {
      return true;
    }

  }

  class Siren10DocsNodesAndPositionsEnum extends DocsNodesAndPositionsEnum {

    int docLimit;
    int blockLimit;

    int doc = -1;
    int docCount;
    int termFreqInDoc = 0;
    int nodFreq = 0;
    int termFreqInNode = 0;
    IntsRef node = new IntsRef(new int[] { -1 }, 0, 1);;
    int pos = -1;

    // flag to know if nextNode() has been called
    boolean termFreqInNodeReadPending = false;

    private int pendingTermFreqInDocCount;
    private int pendingNodFreqCount;
    private int pendingNodCount;
    private int pendingTermFreqInNodeCount;
    private int pendingPosNodCount;

    private Bits liveDocs;
    private final DocsFreqBlockIndexInput.DocsFreqBlockReader docReader;
    private final NodBlockIndexInput.NodBlockReader nodReader;
    private final PosBlockIndexInput.PosBlockReader posReader;
    private long skipFP;

    private final BlockIndexInput.Index docIndex;
    private final BlockIndexInput.Index nodIndex;
    private final BlockIndexInput.Index posIndex;
    private final DocsFreqBlockIndexInput startDocIn;

    boolean skipped;
    Siren10SkipListReader skipper;

    Siren10DocsNodesAndPositionsEnum() throws IOException {
      startDocIn = docIn;

      docReader = docIn.getBlockReader();
      docIndex = docIn.index();

      nodReader = nodIn.getBlockReader();
      nodIndex = nodIn.index();

      posReader = posIn.getBlockReader();
      posIndex = posIn.index();

      // register node and pos index in the doc reader
      docReader.setNodeBlockIndex(nodIndex);
      docReader.setPosBlockIndex(posIndex);
    }

    Siren10DocsNodesAndPositionsEnum init(final FieldInfo fieldInfo,
                                          final SepTermState termState,
                                          final Bits liveDocs)
    throws IOException {
      logger.debug("Init DocsNodesAndPositionsEnum - id={}", this.hashCode());
      this.liveDocs = liveDocs;

      // Init readers
      docReader.init();
      nodReader.init();
      posReader.init();

      // TODO: can't we only do this if consumer
      // skipped consuming the previous docs?
      logger.debug("Set docIndex: {}", termState.docIndex);
      docIndex.set(termState.docIndex);
      docIndex.seek(docReader);

      docLimit = termState.docFreq;
      blockLimit = termState.blockCount;

      // NOTE: unused if blockCount < skipMinimum:
      skipFP = termState.skipFP;

      doc = -1;
      node = new IntsRef(new int[] { -1 }, 0, 1);
      termFreqInDoc = nodFreq = termFreqInNode = 0;
      pos = -1;

      docCount = 0;

      this.resetPendingCounters();

      skipped = false;

      return this;
    }

    private void resetPendingCounters() {
      pendingTermFreqInDocCount = 0;
      pendingNodFreqCount = 0;
      pendingNodCount = 0;
      pendingTermFreqInNodeCount = 0;
      pendingPosNodCount = 0;
      termFreqInNodeReadPending = false;
    }

    @Override
    public boolean nextDocument() throws IOException {
      do {
        if (docCount == docLimit) {
          doc = NO_MORE_DOC;
          return false;
        }

        docCount++;

        // If block exhausted, decode next block
        if (docReader.isExhausted()) {
          docReader.nextBlock();
          nodIndex.seek(nodReader); // move node reader to next block
          nodReader.nextBlock(); // doc and node blocks are synchronised
          posIndex.seek(posReader); // move node reader to next block
          posReader.nextBlock(); // doc and pos blocks are synchronised
          this.resetPendingCounters(); // reset counters as we move to next block
        }
        // decode next doc
        doc = docReader.nextDocument();
        termFreqInDoc = nodFreq = termFreqInNode = 0; // lazy load of freq
        termFreqInNodeReadPending = false; // reset flag
        // increment freq and node pending counters
        pendingTermFreqInDocCount++;
        pendingNodFreqCount++;
        // reset current node and position for delta computation
        nodReader.resetCurrentNode();
        posReader.resetCurrentPosition();
      } while (liveDocs != null && !liveDocs.get(doc));

      return true;
    }

    @Override
    public boolean nextNode() throws IOException {
      termFreqInNode = 0; // lazy load of freq
      termFreqInNodeReadPending = true;
      final int nodeFreqInDoc = this.nodeFreqInDoc(); // load node freq

      // scan over any nodes that were ignored during doc iteration
      while (pendingNodCount > nodeFreqInDoc) {
        // no need to check for exhaustion as doc and node blocks are synchronised
        node = nodReader.nextNode();
        pendingNodCount--;
      }

      if (pendingNodCount > 0) {
        // no need to check for exhaustion as doc and node blocks are synchronised
        node = nodReader.nextNode();
        pendingNodCount--;
        assert pendingNodCount >= 0;
        return true;
      }
      assert pendingNodCount == 0;
      node = NO_MORE_NOD; // set to sentinel value
      return false;
    }

    @Override
    public boolean skipTo(final int target) throws IOException {
      if ((target - (blockSkipInterval * maxBlockSize)) >= doc &&
          docLimit >= (blockSkipMinimum * maxBlockSize)) {

        // There are enough docs in the posting to have
        // skip data, and its not too close

        if (skipper == null) {
          // This DocsEnum has never done any skipping
          skipper = new Siren10SkipListReader((IndexInput) skipIn.clone(),
                                              docIn, maxSkipLevels,
                                              blockSkipInterval, maxBlockSize);
        }

        if (!skipped) {
          // We haven't yet skipped for this posting
          skipper.init(skipFP, docIndex, blockLimit);
          skipper.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
          skipped = true;
        }
        final int newCount = skipper.skipTo(target);

        if (newCount > docCount) {
          // Skipper did move
          skipper.getDocIndex().seek(docReader);
          docCount = newCount;
          doc = skipper.getDoc();
          // reset so that it is consider exhausted in #nextDocument and move
          // to the next block
          docReader.initBlock();
        }
      }

      // Now, linear scan for the rest:
      // TODO: Implement linear block skipping based on first and last doc ids
      do {
        if (!this.nextDocument()) {
          return false;
        }
      } while (target > doc);

      return true;
    }

    @Override
    public int doc() {
      return doc;
    }

    @Override
    public IntsRef node() {
      return node;
    }

    @Override
    public boolean nextPosition() throws IOException {
      final int termFreqInNode = this.termFreqInNode(); // load term freq
      // scan over any nodes that were ignored during doc iteration
      while (pendingPosNodCount > termFreqInNode) {
        // no need to check for exhaustion as doc and pos blocks are synchronised
        pos = posReader.nextPosition();
        pendingPosNodCount--;
      }

      assert pendingPosNodCount <= this.termFreqInNode();

      if (pendingPosNodCount > 0) {
        // no need to check for exhaustion as doc and pos blocks are synchronised
        pos = posReader.nextPosition();
        pendingPosNodCount--;
        assert pendingPosNodCount >= 0;
        return true;
      }
      assert pendingPosNodCount == 0;
      pos = NO_MORE_POS; // set to sentinel value
      return false;
    }

    @Override
    public int pos() {
      return pos;
    }

    @Override
    public int termFreqInDoc() throws IOException {
      if (termFreqInDoc == 0) {
        // scan over any freqs that were ignored during doc iteration
        while (pendingTermFreqInDocCount > 0) {
          termFreqInDoc = docReader.nextFreq();
          pendingTermFreqInDocCount--;
        }
      }
      return termFreqInDoc;
    }

    @Override
    public int nodeFreqInDoc() throws IOException {
      if (nodFreq == 0) {
        // scan over any freqs that were ignored during doc iteration
        while (pendingNodFreqCount > 0) {
          nodFreq = docReader.nextNodeFreq();
          pendingNodFreqCount--;
          pendingNodCount += nodFreq;
          pendingTermFreqInNodeCount += nodFreq;
        }
      }
      return nodFreq;
    }

    @Override
    public int termFreqInNode() throws IOException {
      // nextNode should be called first
      if (termFreqInNodeReadPending) {
        // scan over any freqs that were ignored during doc iteration
        while (pendingTermFreqInNodeCount > nodFreq) {
          termFreqInNode = nodReader.nextTermFreqInNode();
          pendingTermFreqInNodeCount--;
          pendingPosNodCount += termFreqInNode;
        }

        // scan next freq
        termFreqInNode = nodReader.nextTermFreqInNode();
        pendingTermFreqInNodeCount--;
        pendingPosNodCount += termFreqInNode;

        // reset flag
        termFreqInNodeReadPending = false;
      }
      return termFreqInNode;
    }

  }

}

