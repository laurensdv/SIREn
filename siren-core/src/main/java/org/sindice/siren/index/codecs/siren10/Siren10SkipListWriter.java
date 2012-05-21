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
import java.util.Arrays;

import org.apache.lucene.codecs.MultiLevelSkipListWriter;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.store.IndexOutput;
import org.sindice.siren.index.codecs.block.BlockIndexOutput;

/**
 * Implements the skip list writer for the default block-based posting list
 * format that stores document information.
 * <p>
 * The {@link MultiLevelSkipListWriter} implementation is based on document
 * count, but it is used here with block count instead of document count.
 * In order to make it compatible with block, this class is converting
 * document count into block count.
 */
public class Siren10SkipListWriter extends MultiLevelSkipListWriter {

  private final int[] lastSkipDoc;

  private final BlockIndexOutput.Index[] docIndex;

  private int curDoc;

  Siren10SkipListWriter(final int blockSkipInterval, final int maxSkipLevels,
                        final int blockSize,
                        final int docCount, final BlockIndexOutput docOutput)
  throws IOException {
    super(blockSkipInterval, maxSkipLevels, docCount / blockSize);

    lastSkipDoc = new int[numberOfSkipLevels];

    docIndex = new BlockIndexOutput.Index[numberOfSkipLevels];

    for(int i = 0; i < numberOfSkipLevels; i++) {
      docIndex[i] = docOutput.index();
    }
  }

  IndexOptions indexOptions;

  void setIndexOptions(final IndexOptions v) {
    indexOptions = v;
  }

  /**
   * Sets the values for the current skip data.
   * <p>
   * Called at every index interval (every block by default)
   */
  void setSkipData(final int doc) {
    this.curDoc = doc;
  }

  /**
   * Called at start of new term
   */
  protected void resetSkip(final BlockIndexOutput.Index topDocIndex)
  throws IOException {
    super.resetSkip();

    Arrays.fill(lastSkipDoc, 0);
    for(int i = 0; i < numberOfSkipLevels; i++) {
      docIndex[i].copyFrom(topDocIndex, true);
    }
  }

  @Override
  protected void writeSkipData(final int level, final IndexOutput skipBuffer) throws IOException {
    skipBuffer.writeVInt(curDoc - lastSkipDoc[level]);

    docIndex[level].mark();
    docIndex[level].write(skipBuffer, false);

    lastSkipDoc[level] = curDoc;
  }
}
