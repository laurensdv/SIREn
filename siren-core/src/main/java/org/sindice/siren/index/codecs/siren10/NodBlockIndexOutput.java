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

import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRef;
import org.sindice.siren.index.codecs.block.BlockCompressor;
import org.sindice.siren.index.codecs.block.BlockIndexOutput;
import org.sindice.siren.util.ArrayUtils;

public class NodBlockIndexOutput extends BlockIndexOutput {

  private final int maxBlockSize;
  private final BlockCompressor nodCompressor;

  public NodBlockIndexOutput(final IndexOutput out, final int maxBlockSize,
                             final BlockCompressor nodCompressor)
  throws IOException {
    super(out);
    this.nodCompressor = nodCompressor;
    this.maxBlockSize = maxBlockSize;
  }

  @Override
  public NodBlockWriter getBlockWriter() {
    return new NodBlockWriter();
  }

  /**
   * TODO: Can try to reduce the number of test conditions for buffer size by
   * using term frequency information. At each new document, nodBlockWriter is
   * informed of the term frequency, and check buffer size appropriately.
   */
  protected class NodBlockWriter extends BlockWriter {

    final IntsRef nodLenBuffer;
    final IntsRef nodBuffer;

    final IntsRef termFreqBuffer;

    BytesRef nodLenCompressedBuffer;
    BytesRef nodCompressedBuffer;

    BytesRef termFreqCompressedBuffer;

    private int currentNodeOffset = 0;
    private int currentNodeLength = 0;

    public NodBlockWriter() {
      // blockSize is just use as a minimum initial capacity for the buffers
      nodLenBuffer = new IntsRef(maxBlockSize);
      nodBuffer = new IntsRef(maxBlockSize);
      termFreqBuffer = new IntsRef(maxBlockSize);

      // init of the compressed buffers
      nodLenCompressedBuffer = new BytesRef();
      nodCompressedBuffer = new BytesRef();
      termFreqCompressedBuffer = new BytesRef();
    }

    public void write(final IntsRef node) {
      final int nodeOffset = node.offset;
      final int nodeLength = node.length;
      final int[] nodeInts = node.ints;

      assert nodeLength > 0;

      // write node

      final int nodBufferOffset = currentNodeOffset = nodBuffer.offset;

      // increase buffers if needed
      if (nodBufferOffset + nodeLength >= nodBuffer.ints.length) {
        // Take the max to ensure that buffer will be large enough
        nodBuffer.grow(Math.max(nodBufferOffset + nodeLength, nodBuffer.ints.length * 3/2));
      }

      // compute delta
      this.delta(nodeInts, nodeLength);

      // copy
      System.arraycopy(nodeInts, nodeOffset, nodBuffer.ints, nodBufferOffset, nodeLength);

      // increment node buffer offset with node length
      nodBuffer.offset += nodeLength;

      // write node length

      // increase node length buffer if needed
      if (nodLenBuffer.offset >= nodLenBuffer.ints.length) {
        // Take the max to ensure that buffer will be large enough
        nodLenBuffer.grow(Math.max(nodLenBuffer.offset + 1, nodLenBuffer.ints.length * 3/2));
      }

      currentNodeLength = nodeLength;
      // decrement length by one
      nodLenBuffer.ints[nodLenBuffer.offset++] = nodeLength - 1;
    }

    /**
     * Compute the delta of the new node.
     */
    private final void delta(final int[] node, final int length) {
      final int[] nodBufferInts = nodBuffer.ints;

      for (int i = currentNodeOffset, j = 0; i < currentNodeLength &&
                                          j < length &&
                                          nodBufferInts[i] <= node[j]; i++, j++) {
        if (nodBufferInts[i] < node[j]) {
          node[j] = node[j] - nodBufferInts[i];
          return;
        }
        // while equal, compute delta and move to next
        node[j] = node[j] - nodBufferInts[i];
      }
    }

    /**
     * Write the term frequency within the current node
     */
    public void writeTermFreq(final int termFreq) {
      // check size of the buffer and increase it if needed
      if (termFreqBuffer.offset >= termFreqBuffer.ints.length) {
        // Take the max to ensure that buffer will be large enough
        termFreqBuffer.grow(Math.max(termFreqBuffer.offset + 1, termFreqBuffer.ints.length * 3/2));
      }
      // decrement freq by one
      termFreqBuffer.ints[termFreqBuffer.offset++] = termFreq - 1;
    }

    @Override
    public boolean isEmpty() {
      return nodBuffer.offset == 0;
    }

    @Override
    public boolean isFull() {
      // this implementation is never full as it is synchronised with doc block
      // and grows on demand
      return false;
    }

    @Override
    protected void writeHeader() throws IOException {
      logger.debug("Write Nod header: {}", this.hashCode());
      logger.debug("Nod header start at {}", out.getFilePointer());
      // write block sizes
      out.writeVInt(nodLenBuffer.length);
      out.writeVInt(nodBuffer.length);
      out.writeVInt(termFreqBuffer.length);
      assert nodLenBuffer.length <= nodBuffer.length;
      // write size of compressed data blocks
      out.writeVInt(nodLenCompressedBuffer.length);
      out.writeVInt(nodCompressedBuffer.length);
      out.writeVInt(termFreqCompressedBuffer.length);
    }

    @Override
    protected void compress() {
      // Flip buffers before compression
      nodLenBuffer.length = nodLenBuffer.offset;
      nodLenBuffer.offset = 0;

      nodBuffer.length = nodBuffer.offset;
      nodBuffer.offset = 0;

      termFreqBuffer.length = termFreqBuffer.offset;
      termFreqBuffer.offset = 0;

      // determine max size of compressed buffer to avoid overflow
      int size = nodCompressor.maxCompressedValueSize() * nodLenBuffer.length;
      size += nodCompressor.getHeaderSize();
      nodLenCompressedBuffer = ArrayUtils.grow(nodLenCompressedBuffer, size);

      size = nodCompressor.maxCompressedValueSize() * nodBuffer.length;
      size += nodCompressor.getHeaderSize();
      nodCompressedBuffer = ArrayUtils.grow(nodCompressedBuffer, size);

      size = nodCompressor.maxCompressedValueSize() * termFreqBuffer.length;
      size += nodCompressor.getHeaderSize();
      termFreqCompressedBuffer = ArrayUtils.grow(termFreqCompressedBuffer, size);

      nodCompressor.compress(nodLenBuffer, nodLenCompressedBuffer);
      nodCompressor.compress(nodBuffer, nodCompressedBuffer);
      nodCompressor.compress(termFreqBuffer, termFreqCompressedBuffer);
    }

    @Override
    protected void writeData() throws IOException {
      logger.debug("Write Node data: {}", this.hashCode());
      logger.debug("Write Node Length at {}", out.getFilePointer());
      out.writeBytes(nodLenCompressedBuffer.bytes, nodLenCompressedBuffer.length);
      logger.debug("Write Node at {}", out.getFilePointer());
      out.writeBytes(nodCompressedBuffer.bytes, nodCompressedBuffer.length);
      logger.debug("Write Term Freq in Node at {}", out.getFilePointer());
      out.writeBytes(termFreqCompressedBuffer.bytes, termFreqCompressedBuffer.length);
    }

    @Override
    protected void initBlock() {
      nodLenBuffer.offset = nodLenBuffer.length = 0;
      nodBuffer.offset = nodBuffer.length = 0;
      termFreqBuffer.offset = termFreqBuffer.length = 0;
      this.resetCurrentNode();
    }

    protected void resetCurrentNode() {
      currentNodeOffset = 0;
      currentNodeLength = 0;
    }

  }

}
