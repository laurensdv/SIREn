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

public class PosBlockIndexOutput extends BlockIndexOutput {

  private final PosBlockWriter writer;

  private final BlockCompressor posCompressor;

  public PosBlockIndexOutput(final IndexOutput out, final int blockSize,
                             final BlockCompressor posCompressor)
  throws IOException {
    super(out);
    this.posCompressor = posCompressor;
    writer = new PosBlockWriter(blockSize);
  }

  @Override
  public PosBlockWriter getBlockWriter() {
    return writer;
  }

  protected class PosBlockWriter extends BlockWriter {

    final IntsRef posBuffer;

    BytesRef posCompressedBuffer;

    private int currentPos = 0;

    public PosBlockWriter(final int blockSize) {
      posBuffer = new IntsRef(blockSize);
    }

    @Override
    protected void writeHeader() throws IOException {
      logger.debug("Write Pos header: {}", this.hashCode());
      logger.debug("Pos header start at {}", out.getFilePointer());
      // write block sizes
      out.writeVInt(posBuffer.length);
      // write size of compressed data block
      out.writeVInt(posCompressedBuffer.length);
    }

    @Override
    protected void compress() {
      // Flip buffer before compression
      posBuffer.length = posBuffer.offset;
      posBuffer.offset = 0;

      // determine max size of compressed buffer to avoid overflow
      int size = posCompressor.maxCompressedValueSize() * posBuffer.length;
      size += posCompressor.getHeaderSize();
      posCompressedBuffer = new BytesRef(size);

      posCompressor.compress(posBuffer, posCompressedBuffer);
    }

    @Override
    protected void writeData() throws IOException {
      logger.debug("Write Pos data: {}", this.hashCode());
      out.writeBytes(posCompressedBuffer.bytes, posCompressedBuffer.length);
    }

    public void write(final int pos) {
      if (posBuffer.offset >= posBuffer.ints.length) {
        posBuffer.grow(posBuffer.ints.length * 3/2);
      }

      posBuffer.ints[posBuffer.offset++] = pos - currentPos;
      currentPos = pos;
    }

    @Override
    protected void initBlock() {
      posBuffer.offset = posBuffer.length = 0;
      this.resetCurrentPosition();
    }

    public void resetCurrentPosition() {
      currentPos = 0;
    }

    @Override
    public boolean isEmpty() {
      return posBuffer.offset == 0;
    }

    @Override
    public boolean isFull() {
      // this implementation is never full as it is synchronised with doc block
      // and grows on demand
      return false;
    }

  }

}