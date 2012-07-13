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

public class TermFileReader implements Closeable {

  private final RandomAccessFile raf;

  private final long             nTerms;

  private static final int       PTR_SIZE = 8;

  public TermFileReader(final File path) throws IOException {
    this.raf = new RandomAccessFile(path, "r");
    // Read number of terms
    nTerms = this.raf.readLong();
  }

  public long getNumberTerms() {
    return nTerms;
  }

  /**
   * Return th i-th term from this lexicon
   * @param ith the i-th element to get
   */
  public String getTerm(final long index) throws IOException {
    if (index >= nTerms) {
      throw new IndexOutOfBoundsException("Index is not smaller than the number of terms");
    }
    // Seek term index
    raf.seek(index * PTR_SIZE + 8);
    // read term data position
    final long pos = raf.readLong();
    raf.seek(pos);
    return raf.readUTF();
  }

  @Override
  public void close()
  throws IOException {
    this.raf.close();
  }

}
