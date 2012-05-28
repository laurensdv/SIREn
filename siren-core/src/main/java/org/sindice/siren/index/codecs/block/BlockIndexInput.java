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
 * @author Renaud Delbru [ 28 Mar 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.index.codecs.block;

import java.io.Closeable;
import java.io.IOException;

import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.IndexInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BlockIndexInput implements Closeable {

  protected final IndexInput in;

  protected static final Logger logger = LoggerFactory.getLogger(BlockIndexInput.class);

  public BlockIndexInput(final IndexInput in) throws IOException {
    this.in = in;
  }

  public void close() throws IOException {
    in.close();
  }

  public Index index() throws IOException {
    return new Index();
  }

  public class Index {

    private long fp;

    public void read(final DataInput indexIn, final boolean absolute) throws IOException {
      if (absolute) {
        fp = indexIn.readVLong();
      }
      else {
        fp += indexIn.readVLong();
      }
      logger.debug("Read index {}", fp);
    }

    public void seek(final BlockIndexInput.BlockReader other) throws IOException {
      other.seek(fp);
    }

    public void set(final BlockIndexInput.Index other) {
      final Index idx = other;
      fp = idx.fp;
    }

    @Override
    public Object clone() {
      final Index other = new Index();
      other.fp = fp;
      return other;
    }

    @Override
    public String toString() {
      return "fp=" + fp;
    }
  }

  public abstract BlockReader getBlockReader();

  protected abstract class BlockReader {

    private boolean seekPending = false;
    private long pendingFP = 0;
    private long lastBlockFP = -1;

    /**
     * Each block reader should have their own clone of the {@link IndexInput}
     */
    protected final IndexInput in;

    protected BlockReader(final IndexInput in) {
      this.in = in;
    }

    /**
     * Init reader
     */
    public void init() {
      seekPending = false;
      pendingFP = 0;
      lastBlockFP = -1;
      this.initBlock();
    }

    /**
     * Move to the next block and decode block header
     */
    public void nextBlock() throws IOException {
      if (!seekPending) {
        this.skipData();
      }
      this.maybeSeek();
      this.initBlock();
      this.readHeader();
    }

    /**
     * Init reader for new block
     */
    protected abstract void initBlock();

    public abstract boolean isExhausted();

    /**
     * Read and decode block header
     */
    protected abstract void readHeader() throws IOException;

    /**
     * Skip remaining data in the block and advance input stream pointer.
     */
    protected abstract void skipData() throws IOException;

    public void seek(final long fp) {
      logger.debug("Set pending seek to {}", fp);
      pendingFP = fp;
      seekPending = true;
    }

    /**
     * Seek block if needed. Return true if a seek has been performed.
     */
    private boolean maybeSeek() throws IOException {
      if (seekPending) {
        if (pendingFP != lastBlockFP) {
          logger.debug("Seek to {}", pendingFP);
          in.seek(pendingFP);
          lastBlockFP = pendingFP;
          seekPending = false;
          return true;
        }
        seekPending = false;
      }
      return false;
    }

  }

}
