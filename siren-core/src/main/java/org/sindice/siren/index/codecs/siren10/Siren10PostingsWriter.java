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
 * @author Renaud Delbru [ 31 Mar 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.index.codecs.siren10;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.codecs.PostingsWriterBase;
import org.apache.lucene.codecs.TermStats;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexFileNames;
import org.apache.lucene.index.SegmentWriteState;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.RAMOutputStream;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CodecUtil;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.IntsRef;
import org.sindice.siren.analysis.filter.VIntPayloadCodec;
import org.sindice.siren.index.codecs.block.BlockIndexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes docs, freqs and nodFreqs to .doc, nodes and freqsInNode to .nod,
 * position to .pos and block skip data to .skp
 */
public class Siren10PostingsWriter extends PostingsWriterBase {

  final static String CODEC = "Siren10PostingsWriter";

  final static String DOC_EXTENSION = "doc";
  final static String SKIP_EXTENSION = "skp";
  final static String NOD_EXTENSION = "nod";
  final static String POS_EXTENSION = "pos";

  // Increment version to change it:
  final static int VERSION_START = 0;
  final static int VERSION_CURRENT = VERSION_START;

  DocsFreqBlockIndexOutput docOut;
  DocsFreqBlockIndexOutput.DocsFreqBlockWriter docWriter;
  DocsFreqBlockIndexOutput.Index docIndex;

  NodBlockIndexOutput nodOut;
  NodBlockIndexOutput.NodBlockWriter nodWriter;
  NodBlockIndexOutput.Index nodIndex;

  PosBlockIndexOutput posOut;
  PosBlockIndexOutput.PosBlockWriter posWriter;
  PosBlockIndexOutput.Index posIndex;

  IndexOutput skipOut;
  IndexOutput termsOut;

  final Siren10SkipListWriter skipWriter;

  /**
   * Expert: The fraction of blocks stored in skip tables,
   * used to accelerate {@link DocsEnum#advance(int)}.  Larger values result in
   * smaller indexes, greater acceleration, but fewer accelerable cases, while
   * smaller values result in bigger indexes, less acceleration and more
   * accelerable cases.
   */
  final int blockSkipInterval;
  static final int DEFAULT_BLOCK_SKIP_INTERVAL = 2;

  /**
   * Expert: minimum block to write any skip data at all
   */
  final int blockSkipMinimum;

  /**
   * Expert: maximum block size allowed.
   */
  final int maxBlockSize;

  /**
   * Expert: The maximum number of skip levels. Smaller values result in
   * slightly smaller indexes, but slower skipping in big posting lists.
   */
  final int maxSkipLevels = 10;

  final int totalNumDocs;

  IndexOptions indexOptions;

  FieldInfo fieldInfo;

  int blockCount;

  // Holds pending byte[] blob for the current terms block
  private final RAMOutputStream indexBytesWriter = new RAMOutputStream();

  protected static final Logger logger = LoggerFactory.getLogger(Siren10PostingsWriter.class);

  public Siren10PostingsWriter(final SegmentWriteState state,
                               final Siren10BlockStreamFactory factory)
  throws IOException {
    this(state, DEFAULT_BLOCK_SKIP_INTERVAL, factory);
  }

  public Siren10PostingsWriter(final SegmentWriteState state,
                               final int blockSkipInterval,
                               final Siren10BlockStreamFactory factory)
  throws IOException {
    nodOut = null;
    nodIndex = null;
    posOut = null;
    posIndex = null;
    boolean success = false;

    try {
      this.blockSkipInterval = blockSkipInterval;
      this.blockSkipMinimum = blockSkipInterval; /* set to the same for now */

      final String docFileName = IndexFileNames.segmentFileName(state.segmentName,
        state.segmentSuffix, DOC_EXTENSION);
      docOut = factory.createDocsFreqOutput(state.directory, docFileName, state.context);
      docWriter = docOut.getBlockWriter();
      docIndex = docOut.index();

      this.maxBlockSize = docWriter.getMaxBlockSize();

      final String nodFileName = IndexFileNames.segmentFileName(state.segmentName,
        state.segmentSuffix, NOD_EXTENSION);
      nodOut = factory.createNodOutput(state.directory, nodFileName, state.context);
      nodWriter = nodOut.getBlockWriter();
      nodIndex = nodOut.index();

      final String posFileName = IndexFileNames.segmentFileName(state.segmentName,
        state.segmentSuffix, POS_EXTENSION);
      posOut = factory.createPosOutput(state.directory, posFileName, state.context);
      posWriter = posOut.getBlockWriter();
      posIndex = posOut.index();

      final String skipFileName = IndexFileNames.segmentFileName(state.segmentName,
        state.segmentSuffix, SKIP_EXTENSION);
      skipOut = state.directory.createOutput(skipFileName, state.context);

      totalNumDocs = state.numDocs;

      skipWriter = new Siren10SkipListWriter(blockSkipInterval,
          maxSkipLevels, docWriter.getMaxBlockSize(), state.numDocs, docOut);
      docWriter.setNodeBlockIndex(nodIndex);
      docWriter.setPosBlockIndex(posIndex);

      success = true;
    }
    finally {
      if (!success) {
        IOUtils.closeWhileHandlingException(docOut, skipOut, nodOut, posOut);
      }
    }
  }

  @Override
  public void start(final IndexOutput termsOut) throws IOException {
    this.termsOut = termsOut;
    CodecUtil.writeHeader(termsOut, CODEC, VERSION_CURRENT);
    termsOut.writeInt(blockSkipInterval);                // write skipInterval
    termsOut.writeInt(maxSkipLevels);               // write maxSkipLevels
    termsOut.writeInt(blockSkipMinimum);                 // write skipMinimum
    termsOut.writeInt(maxBlockSize);                 // write maxBlockSize
  }

  @Override
  public void startTerm() throws IOException {
    docIndex.mark();
    nodIndex.mark();
    posIndex.mark();

    skipWriter.resetSkip(docIndex);
  }

  // Currently, this instance is re-used across fields, so
  // our parent calls setField whenever the field changes
  @Override
  public void setField(final FieldInfo fieldInfo) {
    this.fieldInfo = fieldInfo;
    this.indexOptions = fieldInfo.indexOptions;
    if (indexOptions.compareTo(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS) >= 0) {
      throw new UnsupportedOperationException("this codec cannot index offsets");
    }
    skipWriter.setIndexOptions(indexOptions);
  }

  /**
   * Adds a new doc in this term. If this returns null
   * then we just skip consuming positions.
   */
  @Override
  public void startDoc(final int docID, final int termDocFreq)
  throws IOException {
    if (docID < 0) {
      throw new CorruptIndexException("docs out of order (" + docID + ") (docOut: " + docOut + ")");
    }

    if (docWriter.isFull()) {
      if ((++blockCount % blockSkipInterval) == 0) {
        skipWriter.setSkipData(docWriter.getFirstDocId());
        skipWriter.bufferSkip(blockCount);
      }
      docWriter.flush();
      nodWriter.flush(); // flush node block to synchronise it with doc block
      posWriter.flush(); // flush pos block to synchronise it with doc block
    }

    docWriter.write(docID, termDocFreq);

    // reset current node and position for delta computation
    nodWriter.resetCurrentNode();
    posWriter.resetCurrentPosition();
    // reset payload hash to sentinel value
    lastPayloadHash = Long.MAX_VALUE;
  }

  // Sentinel value used to indicate that this is the first payload received for
  // the document.
  // Use long to avoid collision between sentinel value and payload hashcode.
  private long lastPayloadHash = Long.MAX_VALUE;
  private int nodeFreqInDoc = 0;
  private int termFreqInNode = 0;

  @Override
  public void addPosition(final int position, final BytesRef payload,
                          final int startOffset, final int endOffset)
  throws IOException {
    assert indexOptions == IndexOptions.DOCS_AND_FREQS_AND_POSITIONS;
    // we always receive node ids in the payload
    assert payload != null;

    // check if we received the same node
    final int payloadHash = payload.hashCode();
    if (lastPayloadHash != payloadHash) { // if different node
      // add term freq for previous node if not first payload.
      if (lastPayloadHash != Long.MAX_VALUE) {
        this.addTermFreqInNode();
      }
      // add new node
      this.addNode(payload);
    }
    lastPayloadHash = payloadHash;

    // add position
    this.addPosition(position, startOffset, endOffset);
  }

  private final VIntPayloadCodec sirenPayload = new VIntPayloadCodec();

  private void addNode(final BytesRef payload) {
    // decode payload
    final IntsRef node = sirenPayload.decode(payload);
    nodWriter.write(node);
    nodeFreqInDoc++;
  }

  private void addPosition(final int position, final int startOffset, final int endOffset) {
    posWriter.write(position);
    termFreqInNode++;
  }

  private void addNodeFreqInDoc() {
    docWriter.writeNodeFreq(nodeFreqInDoc);
    nodeFreqInDoc = 0;
  }

  private void addTermFreqInNode() {
    nodWriter.writeTermFreq(termFreqInNode);
    termFreqInNode = 0;
  }

  @Override
  public void finishDoc() {
    this.addNodeFreqInDoc();
    this.addTermFreqInNode();
  }

  private static class PendingTerm {

    public final BlockIndexOutput.Index docIndex;
    public final long skipFP;
    public final int blockCount;

    public PendingTerm(final BlockIndexOutput.Index docIndex,
                       final long skipFP, final int blockCount) {
      this.docIndex = docIndex;
      this.skipFP = skipFP;
      this.blockCount = blockCount;
    }
  }

  private final List<PendingTerm> pendingTerms = new ArrayList<PendingTerm>();

  /**
   * Called when we are done adding docs to this term
   */
  @Override
  public void finishTerm(final TermStats stats) throws IOException {
    assert stats.docFreq > 0;

    // if block flush pending, write last skip data
    if (!docWriter.isEmpty() && (++blockCount % blockSkipInterval) == 0) {
      skipWriter.setSkipData(docWriter.getFirstDocId());
      skipWriter.bufferSkip(blockCount);
    }

    // flush doc block
    docWriter.flush();
    final BlockIndexOutput.Index docIndexCopy = docOut.index();
    docIndexCopy.copyFrom(docIndex, false);

    // flush node block
    nodWriter.flush();
    final BlockIndexOutput.Index nodIndexCopy = nodOut.index();
    nodIndexCopy.copyFrom(nodIndex, false);

    // flush pos block
    posWriter.flush();
    final BlockIndexOutput.Index posIndexCopy = posOut.index();
    posIndexCopy.copyFrom(posIndex, false);

    // Write skip data to the output file
    final long skipFP;
    if (blockCount >= blockSkipMinimum) {
      skipFP = skipOut.getFilePointer();
      skipWriter.writeSkip(skipOut);
    }
    else {
      skipFP = -1;
    }

    pendingTerms.add(new PendingTerm(docIndexCopy, skipFP, blockCount));

    // reset block counter
    blockCount = 0;
  }

  @Override
  public void flushTermsBlock(final int start, final int count) throws IOException {
    logger.debug("flushTermsBlock: {}", this.hashCode());
    assert indexBytesWriter.getFilePointer() == 0;
    final int absStart = pendingTerms.size() - start;
    final List<PendingTerm> slice = pendingTerms.subList(absStart, absStart+count);

    long lastSkipFP = 0;

    if (count == 0) {
      termsOut.writeByte((byte) 0);
      return;
    }

    final PendingTerm firstTerm = slice.get(0);
    final BlockIndexOutput.Index docIndexFlush = firstTerm.docIndex;

    for (int idx = 0; idx < slice.size(); idx++) {
      final boolean isFirstTerm = idx == 0;
      final PendingTerm t = slice.get(idx);

      // write block count stat
      logger.debug("Write blockCount: {}", t.blockCount);
      indexBytesWriter.writeVInt(t.blockCount);

      docIndexFlush.copyFrom(t.docIndex, false);
      logger.debug("Write docIndex: {}", docIndexFlush);
      docIndexFlush.write(indexBytesWriter, isFirstTerm);

      if (t.skipFP != -1) {
        if (isFirstTerm) {
          indexBytesWriter.writeVLong(t.skipFP);
        }
        else {
          indexBytesWriter.writeVLong(t.skipFP - lastSkipFP);
        }
        lastSkipFP = t.skipFP;
      }
    }

    termsOut.writeVLong((int) indexBytesWriter.getFilePointer());
    indexBytesWriter.writeTo(termsOut);
    indexBytesWriter.reset();
    slice.clear();
  }

  @Override
  public void close() throws IOException {
    IOUtils.close(docOut, skipOut, nodOut, posOut);
  }

}
