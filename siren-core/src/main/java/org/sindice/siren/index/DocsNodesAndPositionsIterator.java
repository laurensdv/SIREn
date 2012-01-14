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

import org.apache.lucene.search.Scorer;

/**
 * This interface defines methods to iterate over a set of increasing
 * doc ids, node paths and positions. Note that this class assumes it iterates
 * on doc Ids, and therefore {@link #NO_MORE_DOC} is set to
 * {@value #NO_MORE_DOC} in order to be used as a sentinel object.
 * Implementations of this class are expected to consider
 * {@link Integer#MAX_VALUE} as an invalid.
 */
public interface DocsNodesAndPositionsIterator  {

  /**
   * When returned by {@link #doc()} it means there are no more docs in the
   * iterator.
   */
  public static final int NO_MORE_DOC = Integer.MAX_VALUE;

  /**
   * When returned by {@link #node()} it means there are no more nodes in the
   * iterator.
   */
  public static final int NO_MORE_NOD = Integer.MAX_VALUE;

  /**
   * When returned by {@link #pos()} it means there are no more
   * positions in the iterator.
   */
  public static final int NO_MORE_POS = Integer.MAX_VALUE;

  /**
   * Advances to the next document in the set, or returns false if there are no
   * more docs in the set.
   * <br>
   * We were not able to keep the original method name, i.e., {@code nextDoc()},
   * as this would have created a incompatibility with {@link Scorer}.
   */
  public boolean nextDocument() throws IOException;

  /**
   * Move to the next node path in the current entity matching the query.
   * <p>
   * Should not be called until {@link #nextDocument()} or {@link #skipTo(int)}
   * are called for the first time.
   *
   * @return false if there is no more node for the current entity or if
   * {@link #nextDocument()} or {@link #skipTo(int)} were not called yet.
   */
  public boolean nextNode() throws IOException;

  /**
   * Move to the next position in the current node matching the query.
   * <p>
   * Should not be called until {@link #nextNode()} or {@link #skipTo(int, int)}
   * are called for the first time.
   *
   * @return false if there is no more position for the current node or if
   * {@link #nextNode()} or {@link #skipTo(int, int)} were not called yet.
   */
  public boolean nextPosition() throws IOException;

  /**
   * Skip to the first beyond (see NOTE below) the current whose document
   * number is greater than or equal to <i>target</i>. Returns false if there
   * are no more docs in the set.
   * <p>
   * Behaves as if written:
   *
   * <pre>
   * boolean advance(int target) {
   *   while (nextDocument()) {
   *     if (doc() &lt; target)
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
   * Skip to the first document and node beyond the current whose document
   * and/or node number is greater than or equal to <i>target</i>. Returns false
   * if there are no more docs in the set.
   */
  public boolean skipTo(int docID, int[] nodes) throws IOException;

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
