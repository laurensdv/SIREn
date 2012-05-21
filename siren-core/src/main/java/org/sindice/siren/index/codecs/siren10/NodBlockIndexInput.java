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

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRef;
import org.sindice.siren.index.codecs.block.BlockDecompressor;
import org.sindice.siren.index.codecs.block.BlockIndexInput;
import org.sindice.siren.util.ArrayUtils;

public class NodBlockIndexInput extends BlockIndexInput {

  final NodBlockReader reader;

  protected BlockDecompressor nodDecompressor;

  public NodBlockIndexInput(final IndexInput in, final BlockDecompressor nodDecompressor)
  throws IOException {
    super(in);
    this.nodDecompressor = nodDecompressor;
    reader = new NodBlockReader();
  }

  @Override
  public NodBlockReader getBlockReader() {
    return reader;
  }

  protected class NodBlockReader extends BlockReader {

    IntsRef nodLenBuffer = new IntsRef();
    IntsRef nodBuffer = new IntsRef();
    IntsRef termFreqBuffer = new IntsRef();

    /**
     * Used to slice the nodBuffer and disclose only the subset containing
     * information about the current node.
     */
    private final IntsRef currentNode = new IntsRef();

    boolean nodLenReadPending = true;
    boolean nodReadPending = true;
    boolean termFreqReadPending = true;

    int nodLenCompressedBufferLength;
    BytesRef nodLenCompressedBuffer = new BytesRef();
    int nodCompressedBufferLength;
    BytesRef nodCompressedBuffer = new BytesRef();
    int termFreqCompressedBufferLength;
    BytesRef termFreqCompressedBuffer = new BytesRef();

    @Override
    protected void readHeader() throws IOException {
      logger.debug("Read Nod header: {}", this.hashCode());
      logger.debug("Nod header start at {}", in.getFilePointer());
      // read blockSize and check buffer size
      final int nodLenblockSize = in.readVInt();
      nodLenBuffer = ArrayUtils.grow(nodLenBuffer, nodLenblockSize);
      logger.debug("Read Nod length block size: {}", nodLenblockSize);
      final int nodBlockSize = in.readVInt();
      nodBuffer = ArrayUtils.grow(nodBuffer, nodBlockSize);
      logger.debug("Read Nod block size: {}", nodBlockSize);
      final int termFreqblockSize = in.readVInt();
      termFreqBuffer = ArrayUtils.grow(termFreqBuffer, termFreqblockSize);
      logger.debug("Read Term Freq In Node block size: {}", termFreqblockSize);

      // read size of each compressed data block and check buffer size
      nodLenCompressedBufferLength = in.readVInt();
      nodLenCompressedBuffer = ArrayUtils.grow(nodLenCompressedBuffer, nodLenCompressedBufferLength);
      nodLenReadPending = true;

      nodCompressedBufferLength = in.readVInt();
      nodCompressedBuffer = ArrayUtils.grow(nodCompressedBuffer, nodCompressedBufferLength);
      nodReadPending = true;

      termFreqCompressedBufferLength = in.readVInt();
      termFreqCompressedBuffer = ArrayUtils.grow(termFreqCompressedBuffer, termFreqCompressedBufferLength);
      termFreqReadPending = true;

      // decode node lengths
      this.decodeNodeLengths();

      // copy reference of node buffer
      currentNode.ints = nodBuffer.ints;
    }

    @Override
    protected void skipData() throws IOException {
      long size = 0;
      if (nodLenReadPending) {
        size += nodLenCompressedBufferLength;
      }
      if (nodReadPending) {
        size += nodCompressedBufferLength;
      }
      if (termFreqReadPending) {
        size += termFreqCompressedBufferLength;
      }
      this.seek(in.getFilePointer() + size);
      logger.debug("Skip Nod data: {}", in.getFilePointer() + size);
    }

    private void decodeNodeLengths() throws IOException {
      logger.debug("Decode Nodes length: {}", this.hashCode());
      in.readBytes(nodLenCompressedBuffer.bytes, 0, nodLenCompressedBufferLength);
      nodLenCompressedBuffer.offset = 0;
      nodLenCompressedBuffer.length = nodLenCompressedBufferLength;
      nodDecompressor.decompress(nodLenCompressedBuffer, nodLenBuffer);
      nodLenReadPending = false;
    }

    private void decodeNodes() throws IOException {
      logger.debug("Decode Nodes: {}", this.hashCode());
      in.readBytes(nodCompressedBuffer.bytes, 0, nodCompressedBufferLength);
      nodCompressedBuffer.offset = 0;
      nodCompressedBuffer.length = nodCompressedBufferLength;
      nodDecompressor.decompress(nodCompressedBuffer, nodBuffer);
      nodReadPending = false;
    }

    private void decodeTermFreqs() throws IOException {
      logger.debug("Decode Term Freq in Node: {}", this.hashCode());
      in.readBytes(termFreqCompressedBuffer.bytes, 0, termFreqCompressedBufferLength);
      termFreqCompressedBuffer.offset = 0;
      termFreqCompressedBuffer.length = termFreqCompressedBufferLength;
      nodDecompressor.decompress(termFreqCompressedBuffer, termFreqBuffer);
      termFreqReadPending = false;
    }

    public IntsRef nextNode() throws IOException {
      if (nodReadPending) {
        this.decodeNodes();
      }
      // decode delta
      this.deltaDecoding();
      return currentNode;
    }

    /**
     * Decode delta of the node.
     * <p>
     * If a new doc has been read (currentNode.length == 0), then update currentNode
     * offset and length. Otherwise, perform delta decoding.
     * <p>
     * Perform delta decoding while current node id and previous node id are
     * equals.
     */
    private final void deltaDecoding() {
      final int[] nodBufferInts = nodBuffer.ints;
      // increment length by one
      final int nodLength = nodLenBuffer.ints[nodLenBuffer.offset++] + 1;
      final int nodOffset = nodBuffer.offset;

      for (int i = nodOffset, j = currentNode.offset;
           i < nodLength && j < currentNode.length;
           i++, j++) {
        nodBufferInts[i] += nodBufferInts[j];
        // if node ids are different, then stop decoding
        if (nodBufferInts[i] != nodBufferInts[j]) {
          break;
        }
      }

      // increment node buffer offset
      nodBuffer.offset += nodLength;
      // update last node offset and length
      currentNode.offset = nodOffset;
      currentNode.length = nodLength;
    }

    public int nextTermFreqInNode() throws IOException {
      if (termFreqReadPending) {
        this.decodeTermFreqs();
      }
      // increment freq by one
      return termFreqBuffer.ints[termFreqBuffer.offset++] + 1;
    }

    @Override
    public boolean isExhausted() {
      return nodLenBuffer.offset >= nodLenBuffer.length;
    }

    @Override
    public void initBlock() {
      nodLenBuffer.offset = nodLenBuffer.length = 0;
      nodBuffer.offset = nodBuffer.length = 0;
      termFreqBuffer.offset = termFreqBuffer.length = 0;
      this.resetCurrentNode();
    }

    public void resetCurrentNode() {
      currentNode.offset = currentNode.length = 0;
    }

  }

}
