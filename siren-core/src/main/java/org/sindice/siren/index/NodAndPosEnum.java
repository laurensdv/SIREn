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
 * Iterates through documents, nodes and positions.
 * <br>
 * A node is defined by a list of node identifiers which represents the path
 * in the tree to reach the node.
 */
public abstract class NodAndPosEnum extends DocsAndPositionsEnum {

  /**
   * Sentinel value for nodes which means that there are no more nodes in the
   * iterator.
   */
  public static final int NO_MORE_NOD = Integer.MAX_VALUE;

  /**
   * When returned by {@link #nextPosition()} it means there are no more
   * positions in the iterator.
   */
  public static final int NO_MORE_POS = Integer.MAX_VALUE;

  /**
   * Decode the next node path.
   * <br>
   * The node path must be accessed with {{@link #node()}}.
   *
   * @return When there is no more nodes in the iterator, this method returns
   * false. Otherwise, this method returns true.
   */
  public abstract boolean nextNode() throws IOException;

  /**
   * Decode the next node path and the next position.
   * <br>
   * This method returns the position. The node path must be accessed with
   * {@link #node()}.
   *
   * @return When there is no more nodes and positions in the iterator, this
   * method returns {@link #NO_MORE_POS}.
   */
  @Override
  public abstract int nextPosition() throws IOException;

  /**
   * The node array keeps the tree hierarchy: the cell at index 0 is the root,
   * and as we advance up in the array, we go deeper into the tree. The nodes array
   * must have a size lower than or equal to the number of layers of this enum.
   * @param target
   * @param nodes
   * @return
   * @throws IOException
   */
  public abstract int advance(int target, int[] nodes) throws IOException;

  /**
   * Returns the current node path
   */
  public abstract int[] node();

  /**
   * Returns the current position
   */
  public abstract int pos();

}
