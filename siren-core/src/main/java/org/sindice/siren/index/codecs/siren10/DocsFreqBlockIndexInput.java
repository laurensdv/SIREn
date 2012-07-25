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
 * @author Renaud Delbru [ 30 Mar 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.index.codecs.siren10;

import java.io.IOException;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRef;
import org.sindice.siren.index.codecs.block.BlockDecompressor;
import org.sindice.siren.index.codecs.block.BlockIndexInput;
import org.sindice.siren.util.ArrayUtils;

public class DocsFreqBlockIndexInput extends BlockIndexInput {

  protected BlockDecompressor docDecompressor;
  protected BlockDecompressor freqDecompressor;

  public DocsFreqBlockIndexInput(final IndexInput in,
                                 final BlockDecompressor docDecompressor,
                                 final BlockDecompressor freqDecompressor)
  throws IOException {
    super(in);
    this.docDecompressor = docDecompressor;
    this.freqDecompressor = freqDecompressor;
  }

  @Override
  public DocsFreqBlockReader getBlockReader() {
    // Clone index input. A cloned index input does not need to be closed
    // by the block reader, as the underlying stream will be closed by the
    // input it was cloned from
    return new DocsFreqBlockReader((IndexInput) in.clone());
  }

  public class DocsFreqBlockReader extends BlockReader {

    protected int blockSize;

    IntsRef docBuffer = new IntsRef();
    IntsRef nodFreqBuffer = new IntsRef();

    boolean docsReadPending = true;
    boolean nodFreqsReadPending = true;

    int docCompressedBufferLength;
    int nodFreqCompressedBufferLength;

    BytesRef docCompressedBuffer = new BytesRef();
    BytesRef nodFreqCompressedBuffer = new BytesRef();

    int firstDocId, lastDocId;

    long dataBlockOffset = -1;

    NodBlockIndexInput.Index nodeBlockIndex;
    PosBlockIndexInput.Index posBlockIndex;

    private DocsFreqBlockReader(final IndexInput in) {
      super(in);
    }

    public void setNodeBlockIndex(final NodBlockIndexInput.Index index) throws IOException {
      this.nodeBlockIndex = index;
    }

    public void setPosBlockIndex(final PosBlockIndexInput.Index index) throws IOException {
      this.posBlockIndex = index;
    }

    @Override
    protected void readHeader() throws IOException {
      // logger.debug("Read DocFreq header: {}", this.hashCode());
      // logger.debug("DocFreq header start at {}", in.getFilePointer());

      // read blockSize and check buffer size
      blockSize = in.readVInt();
      docBuffer = ArrayUtils.grow(docBuffer, blockSize);
      nodFreqBuffer = ArrayUtils.grow(nodFreqBuffer, blockSize);

      // read size of each compressed data block and check buffer size
      docCompressedBufferLength = in.readVInt();
      docCompressedBuffer = ArrayUtils.grow(docCompressedBuffer, docCompressedBufferLength);
      docsReadPending = true;

      nodFreqCompressedBufferLength = in.readVInt();
      nodFreqCompressedBuffer = ArrayUtils.grow(nodFreqCompressedBuffer, nodFreqCompressedBufferLength);
      nodFreqsReadPending = true;

      // read first and last doc id
      firstDocId = in.readVInt();
      lastDocId = firstDocId + in.readVInt();

      // read node and pos skip data
      nodeBlockIndex.read(in, true);
      posBlockIndex.read(in, true);

      // record file pointer as data block offset for skipping
      dataBlockOffset = in.getFilePointer();
    }

    @Override
    protected void skipData() {
      int size = docCompressedBufferLength;
      size += nodFreqCompressedBufferLength;

      this.seek(dataBlockOffset + size);
      // logger.debug("Skip DocFreq data: {}", dataBlockOffset + size);
    }

    private void decodeDocs() throws IOException {
      // logger.debug("Decode Doc block: {}", this.hashCode());

      in.seek(dataBlockOffset); // skip to doc data block
      in.readBytes(docCompressedBuffer.bytes, 0, docCompressedBufferLength);
      docCompressedBuffer.offset = 0;
      docCompressedBuffer.length = docCompressedBufferLength;
      docDecompressor.decompress(docCompressedBuffer, docBuffer);

      docsReadPending = false;
    }

    private void decodeNodeFreqs() throws IOException {
      // logger.debug("Decode Node Freqs block: {}", this.hashCode());

      in.seek(dataBlockOffset + docCompressedBufferLength); // skip to node freq data block
      in.readBytes(nodFreqCompressedBuffer.bytes, 0, nodFreqCompressedBufferLength);
      nodFreqCompressedBuffer.offset = 0;
      nodFreqCompressedBuffer.length = nodFreqCompressedBufferLength;
      freqDecompressor.decompress(nodFreqCompressedBuffer, nodFreqBuffer);

      nodFreqsReadPending = false;
    }

    public int getFirstDocId() {
      return firstDocId;
    }

    public int getLastDocId() {
      return lastDocId;
    }

    private int currentDocId;

    public int nextDocument() throws IOException {
      if (docsReadPending) {
        this.decodeDocs();
        currentDocId = firstDocId;
      }
      // decode delta
      currentDocId += docBuffer.ints[docBuffer.offset++];
      return currentDocId;
    }

    public int nextNodeFreq() throws IOException {
      if (nodFreqsReadPending) {
        this.decodeNodeFreqs();
      }
      // Increment freq
      return nodFreqBuffer.ints[nodFreqBuffer.offset++] + 1;
    }

    @Override
    public boolean isExhausted() {
      return docBuffer.offset >= docBuffer.length;
    }

    @Override
    protected void initBlock() {
      docBuffer.offset = docBuffer.length = 0;
      nodFreqBuffer.offset = nodFreqBuffer.length = 0;

      docsReadPending = true;
      nodFreqsReadPending = true;

      docCompressedBufferLength = 0;
      nodFreqCompressedBufferLength = 0;

      dataBlockOffset = -1;
    }

  }

}
