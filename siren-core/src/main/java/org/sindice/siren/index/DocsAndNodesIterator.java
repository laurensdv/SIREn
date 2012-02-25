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
 * doc ids and node paths. Note that this class assumes it iterates
 * on doc ids, and therefore {@link #NO_MORE_DOC} is set to
 * {@value #NO_MORE_DOC} in order to be used as a sentinel object.
 * Implementations of this class are expected to consider
 * {@link Integer#MAX_VALUE} as an invalid.
 */
public interface DocsAndNodesIterator  {

  /**
   * When returned by {@link #doc()} it means there are no more docs in the
   * iterator.
   */
  public static final int NO_MORE_DOC = Integer.MAX_VALUE;

  /**
   * When returned by {@link #node()} it means there are no more nodes in the
   * iterator.
   */
  public static final int[] NO_MORE_NOD = new int[] { Integer.MAX_VALUE };

  /**
   * Advances to the next document in the set.
   *
   * @return false if there is no more docs in the set.
   */
  public boolean nextDocument() throws IOException;

  /**
   * Move to the next node path in the current document.
   * <p>
   * Should not be called until {@link #nextDocument()} or {@link #skipTo(int)}
   * are called for the first time.
   *
   * @return false if there is no more node for the current document or if
   * {@link #nextDocument()} or {@link #skipTo(int)} were not called yet.
   */
  public boolean nextNode() throws IOException;

  /**
   * Skip to the first document beyond (see NOTE below) the current whose
   * number is greater than or equal to <i>target</i>. Returns false if there
   * are no more docs in the set.
   * <p>
   * Behaves as if written:
   *
   * <pre>
   * boolean skipTo(int target) {
   *   while (nextDocument()) {
   *     if (target &le; doc())
   *       return true;
   *   }
   *   return false;
   * }
   * </pre>
   *
   * Some implementations are considerably more efficient than that.
   * <p>
   * <b>NOTE:</b> when <code> target &le; current</code> implementations must
   * not advance beyond their current {@link #doc()}.
   */
  public boolean skipTo(int target) throws IOException;

  /**
   * Returns the following:
   * <ul>
   * <li>-1 or {@link #NO_MORE_DOC} if {@link #nextDocument()} or
   * {@link #skipTo(int)} were not called yet.
   * <li>{@link #NO_MORE_DOC} if the iterator has exhausted.
   * <li>Otherwise it should return the doc ID it is currently on.
   * </ul>
   * <p>
   */
  public int doc();

  /**
   * Returns the following:
   * <ul>
   * <li>-1 or {@link #NO_MORE_NOD} if {@link #nextNode()} or
   * {@link #skipTo(int, int[])} were not called yet.
   * <li>{@link #NO_MORE_NOD} if the iterator has exhausted.
   * <li>Otherwise it should return the node it is currently on.
   * </ul>
   */
  public int[] node();

}
