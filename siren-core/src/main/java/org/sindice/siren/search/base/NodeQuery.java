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
 * @author Renaud Delbru [ 24 Jan 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.search.base;

import org.apache.lucene.search.Query;
import org.sindice.siren.util.NodeUtils;

/**
 * Abstract class for SIREn's node queries
 */
public abstract class NodeQuery extends Query {

  /**
   * The node level constraint
   */
  protected int nodeLevelConstraint;

  /**
   * Set a constraint on the node's level
   */
  public void setNodeLevelConstraint(final int nodeLevelConstraint) {
    this.nodeLevelConstraint = nodeLevelConstraint;
  }

  /**
   * Lower bound constraint for the node path
   */
  protected int[] nodeLowerBoundConstraint = null;

  /**
   * Upper bound constraint for the node path
   */
  protected int[] nodeUpperBoundConstraint = null;

  /**
   * Is the node level is constrained ?
   */
  protected boolean isNodeLevelConstrained = false;

  /**
   * Set an interval constraint for a node path. These constraints are
   * inclusives.
   * <br>
   * <b>NOTE:</b> The node path constraints must be of the same length.
   *
   * @see NodeUtils#isConstraintSatisfied(int[], int[], int[], boolean)
   */
  public void setNodeConstraint(final int[] lowerBound, final int[] upperBound, final boolean isNodeLevelConstrained) {
    if (lowerBound == null || upperBound == null) {
      return;
    }

    if (lowerBound.length != upperBound.length) {
      throw new IllegalArgumentException("Lower and upper bound must be of the same length");
    }

    this.nodeLowerBoundConstraint = lowerBound;
    this.nodeUpperBoundConstraint = upperBound;
    this.isNodeLevelConstrained = isNodeLevelConstrained;
  }

}
