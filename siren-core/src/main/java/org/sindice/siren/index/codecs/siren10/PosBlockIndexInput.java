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

public class PosBlockIndexInput extends BlockIndexInput {

  final PosBlockReader reader;

  protected BlockDecompressor posDecompressor;

  public PosBlockIndexInput(final IndexInput in, final BlockDecompressor posDecompressor)
  throws IOException {
    super(in);
    this.posDecompressor = posDecompressor;
    reader = new PosBlockReader();
  }

  @Override
  public PosBlockReader getBlockReader() {
    return reader;
  }

  protected class PosBlockReader extends BlockReader {

    IntsRef posBuffer = new IntsRef();

    boolean posReadPending = true;

    int posCompressedBufferLength;
    BytesRef posCompressedBuffer = new BytesRef();

    private int currentPos = 0;

    @Override
    protected void readHeader() throws IOException {
      logger.debug("Decode Pos header: {}", this.hashCode());
      logger.debug("Pos header start at {}", in.getFilePointer());
      // read blockSize and check buffer size
      final int posBlockSize = in.readVInt();
      posBuffer = ArrayUtils.grow(posBuffer, posBlockSize);
      logger.debug("Read Pos block size: {}", posBlockSize);

      // read size of each compressed data block and check buffer size
      posCompressedBufferLength = in.readVInt();
      posCompressedBuffer = ArrayUtils.grow(posCompressedBuffer, posCompressedBufferLength);
      posReadPending = true;
    }

    @Override
    protected void skipData() throws IOException {
      long size = 0;
      if (posReadPending) {
        size += posCompressedBufferLength;
      }
      this.seek(in.getFilePointer() + size);
      logger.debug("Skip Pos data: {}", in.getFilePointer() + size);
    }

    public int nextPosition() throws IOException {
      if (posReadPending) {
        this.decodePositions();
      }
      assert posBuffer.offset <= posBuffer.length;
      return currentPos = posBuffer.ints[posBuffer.offset++] + currentPos;
    }

    private void decodePositions() throws IOException {
      logger.debug("Decode Pos: {}", this.hashCode());
      in.readBytes(posCompressedBuffer.bytes, 0, posCompressedBufferLength);
      posCompressedBuffer.offset = 0;
      posCompressedBuffer.length = posCompressedBufferLength;
      posDecompressor.decompress(posCompressedBuffer, posBuffer);
      posReadPending = false;
    }

    @Override
    public boolean isExhausted() {
      return posBuffer.offset >= posBuffer.length;
    }

    @Override
    protected void initBlock() {
      posBuffer.offset = posBuffer.length = 0;
      this.resetCurrentPosition();
    }

    public void resetCurrentPosition() {
      currentPos = 0;
    }

  }

}
