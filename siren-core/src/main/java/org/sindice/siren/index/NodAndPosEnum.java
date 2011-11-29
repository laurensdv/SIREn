/**
 * Copyright (c) 2009-2011 Sindice Limited. All Rights Reserved.
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
 * @author Campinas Stephane [ 28 Nov 2011 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.siren.index;

import java.io.IOException;

import org.apache.lucene.index.DocsAndPositionsEnum;

/**
 * 
 */
public abstract class NodAndPosEnum extends DocsAndPositionsEnum {

  /**
   * When returned by {@link #nextPosition()} it means there are no more
   * positions in the iterator.
   */
  public static final int NO_MORE_POS = Integer.MAX_VALUE;
  
  /**
   * The node array keeps the tree hierarchy: the cell at index 0 is the root,
   * and as we advance up in the array, we go deeper into the tree. A cell with
   * value -1 stops the advance into the array.
   * @param target
   * @param nodes
   * @return
   * @throws IOException 
   */
  public abstract int advance(int target, int[] nodes) throws IOException;
  
  public abstract int[] node();

}
