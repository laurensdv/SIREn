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
package org.sindice.siren.search;

import java.io.IOException;

/**
 *
 */
public interface NodIdSetIterator {

  /**
   * Sentinel value for nodes which means that there are no more nodes in the
   * iterator.
   */
  public static final int NOD_SENTINEL_VAL = Integer.MAX_VALUE;

  /**
   *  When returned by {@link #nextNode()} it means there are no more
   * positions in the iterator.
   */
  public static final boolean NO_MORE_NOD = Boolean.FALSE;

  /**
   * When returned by {@link #nextPosition()} it means there are no more
   * positions in the iterator.
   */
  public static final int NO_MORE_POS = Integer.MAX_VALUE;

  /**
   * Moves to the next entity identifier in the set. Returns true, iff there is
   * such an entity identifier.
   * <p>
   * Move the tuple, cell and pos pointer to the first position [SRN-24].
   **/
  public int nextDoc() throws IOException;

  /**
   * Move to the next node path in the current entity matching
   * the query.
   * <p>
   * This is invalid until {@link #nextDoc()} is called for the first time.
   *
   * @return false iff there is no more tuple, cell and position for the current
   *         entity.
   */
  public boolean nextNode() throws IOException;

  /**
   * Move to the next node path and position in the current entity matching
   * the query.
   * <p>
   * This is invalid until {@link #nextDoc()} is called for the first time.
   *
   * @return false iff there is no more tuple, cell and position for the current
   *         entity.
   */
  public int nextPosition() throws IOException;

  /**
   * Skips to the first match (including the current) whose is greater than or
   * equal to a given entity.
   */
  public int advance(int docID) throws IOException;

  /**
   * Skips to the first match (including the current) whose is greater than or
   * equal to a given entity and tuple.
   */
  public int advance(int docID, int[] nodes) throws IOException;

  /**
   * Returns the current doc identifier.
   * <p>
   * This is invalid until {@link #nextDoc()} is called for the first time.
   **/
  public int docID();

  /**
   * Returns the current node identifiers
   * <p>
   * Initially invalid, until {@link #nextNode()} or {@link #nextPosition()}
   * is called the first time.
   */
  public int[] node();

  /**
   * Returns the current position identifier matching the query.
   * <p>
   * Initially invalid, until {@link #nextPosition()} is called the first time.
   */
  public int pos();

}
