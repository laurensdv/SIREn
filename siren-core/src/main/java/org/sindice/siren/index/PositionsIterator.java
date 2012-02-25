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
 * @author Campinas Stephane [ 1 Dec 2011 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.siren.index;

import java.io.IOException;

/**
 * This interface defines methods to iterate over a set of increasing
 * positions. Note that this class assumes it iterates
 * on position, and therefore {@link #NO_MORE_POS} is set to
 * {@value #NO_MORE_POS} in order to be used as a sentinel object.
 * Implementations of this class are expected to consider
 * {@link Integer#MAX_VALUE} as an invalid.
 *
 * <p>
 * To be used in conjunction with {@link DocsAndNodesIterator}.
 */
public interface PositionsIterator  {

  /**
   * When returned by {@link #pos()} it means there are no more
   * positions in the iterator.
   */
  public static final int NO_MORE_POS = Integer.MAX_VALUE;

  /**
   * Move to the next position in the current node matching the query.
   * <p>
   * Should not be called until {@link DocsAndNodesIterator#nextNode()} or
   * {@link DocsAndNodesIterator#skipTo(int, int)} are called for the first
   * time.
   *
   * @return false if there is no more position for the current node or if
   * {@link DocsAndNodesIterator#nextNode()} or
   * {@link DocsAndNodesIterator#skipTo(int, int)} were not called yet.
   */
  public boolean nextPosition() throws IOException;

  /**
   * Returns the following:
   * <ul>
   * <li>-1 or {@link #NO_MORE_POS} if {@link #nextPosition()} were not called
   * yet.
   * <li>{@link #NO_MORE_POS} if the iterator has exhausted.
   * <li>Otherwise it should return the position it is currently on.
   * </ul>
   */
  public int pos();

}
