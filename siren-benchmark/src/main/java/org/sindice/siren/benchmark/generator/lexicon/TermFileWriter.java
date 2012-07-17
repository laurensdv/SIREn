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
 * @project siren-benchmark
 * @author Renaud Delbru [ 6 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.generator.lexicon;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class TermFileWriter implements Closeable {

  private final RandomAccessFile raf;

  private final long             nTerms;

  private final int              PTR_SIZE = 8;
  private final long             INDEX_START = 8;

  private long                   indexOffset;
  private long                   dataOffset;

  public TermFileWriter(final File path, final long nTerms) throws IOException {
    this.raf = new RandomAccessFile(path, "rw");
    this.nTerms = nTerms;
    this.writeNumberTerms();
    indexOffset = INDEX_START;
    dataOffset = nTerms * PTR_SIZE + INDEX_START;
  }

  public long getNumberTerms() {
    return nTerms;
  }

  public void add(final String term) throws IOException {
    if (indexOffset / 8 > nTerms) {
      throw new IndexOutOfBoundsException("The file cannot hold more terms");
    }
    // Write term index
    this.raf.seek(indexOffset);
    this.raf.writeLong(dataOffset);
    indexOffset = this.raf.getFilePointer();
    // Write term data
    this.raf.seek(dataOffset);
    this.raf.writeUTF(term);
    dataOffset = this.raf.getFilePointer();
  }

  private void writeNumberTerms() throws IOException {
    this.raf.seek(0);
    this.raf.writeLong(nTerms);
  }

  @Override
  public void close() throws IOException {
    this.raf.close();
  }

}
