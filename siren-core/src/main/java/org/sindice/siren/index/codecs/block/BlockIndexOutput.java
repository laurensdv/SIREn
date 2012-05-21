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
 * @author Renaud Delbru [ 27 Mar 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.index.codecs.block;

import java.io.Closeable;
import java.io.IOException;

import org.apache.lucene.store.IndexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BlockIndexOutput implements Closeable {

  protected final IndexOutput out;

  protected static final Logger logger = LoggerFactory.getLogger(BlockIndexOutput.class);

  public BlockIndexOutput(final IndexOutput out) {
    this.out = out;
  }

  /**
   * If you are indexing the primary output file, call
   * this and interact with the returned IndexWriter.
   */
  public Index index() throws IOException {
    return new Index();
  }

  public class Index {

    long fp;
    long lastFP;

    public void mark() throws IOException {
      fp = out.getFilePointer();
    }

    public void copyFrom(final BlockIndexOutput.Index other, final boolean copyLast)
    throws IOException {
      final Index idx = other;
      fp = idx.fp;
      if (copyLast) {
        lastFP = fp;
      }
    }

    public void write(final IndexOutput indexOut, final boolean absolute)
    throws IOException {
      logger.debug("Write index at {}", fp);
      if (absolute) {
        indexOut.writeVLong(fp);
      }
      else {
        indexOut.writeVLong(fp - lastFP);
      }
      lastFP = fp;
    }

    @Override
    public String toString() {
      return "fp=" + fp;
    }
  }

  public void close() throws IOException {
    try {
      this.getBlockWriter().flush();
    }
    finally {
      out.close();
    }
  }

  public abstract BlockWriter getBlockWriter();

  protected abstract class BlockWriter {

    /**
     * Flush of pending data block to the output file.
     */
    public void flush() throws IOException {
      // Flush only if the block is non empty
      if (!this.isEmpty()) {
        this.writeBlock();
      }
    }

    /**
     * Write data block to the output file with the following sequence of
     * operations:
     * <ul>
     * <li> Compress the data
     * <li> Write block header (as header can depend on statistic computed
     * from data compression)
     * <li> Write compressed data block
     * <li> Reset writer for new block
     * </ul>
     */
    protected void writeBlock() throws IOException {
      this.compress();
      this.writeHeader();
      this.writeData();
      this.initBlock();
    }

    public abstract boolean isEmpty();

    public abstract boolean isFull();

    /**
     * Compress the data block
     */
    protected abstract void compress();

    /**
     * Write block header to the output file
     */
    protected abstract void writeHeader() throws IOException;

    /**
     * Write compressed data block to the output file
     */
    protected abstract void writeData() throws IOException;

    /**
     * Init writer for new block
     */
    protected abstract void initBlock();

  }

}
