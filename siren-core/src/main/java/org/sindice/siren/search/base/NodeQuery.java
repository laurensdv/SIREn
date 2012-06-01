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

import java.util.Iterator;
import java.util.TreeMap;

import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.search.Query;
import org.sindice.siren.index.DocsNodesAndPositionsEnum;
import org.sindice.siren.index.IntervalConstrainedNodesEnum;
import org.sindice.siren.index.LevelConstrainedNodesEnum;
import org.sindice.siren.index.SingleIntervalConstrainedNodesEnum;
import org.sindice.siren.index.SirenDocsEnum;

/**
 * Abstract class for SIREn's node queries
 */
public abstract class NodeQuery extends Query {

  /**
   * The node level constraint.
   * <p>
   * Set to sentinel value -1 by default.
   */
  protected int levelConstraint = -1;

  /**
   * Set a constraint on the node's level
   * <p>
   * Given that the root of the tree (level 0) is the document id, the node
   * level constraint ranges from 1 to <code>Integer.MAX_VALUE</code>. A node
   * level constraint of 0 will always return false.
   */
  public void setLevelConstraint(final int levelConstraint) {
    this.levelConstraint = levelConstraint;
  }

  /**
   * The lower and upper bound of the interval constraint over the node indexes.
   * <p>
   * Set to sentinel value -1 by default.
   */
  protected int lowerBound = -1, upperBound = -1;

  /**
   * Set an index interval constraint for a node. These constraints are
   * inclusives.
   */
  public void setNodeConstraint(final int lowerBound, final int upperBound) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  /**
   * Set the node index constraint.
   */
  public void setNodeConstraint(final int index) {
    this.setNodeConstraint(index, index);
  }

  public int[] getNodeConstraint() {
    return new int[] { lowerBound, upperBound };
  }

  class ConstraintStack {

    final TreeMap<Integer, Integer[]> stack = new TreeMap<Integer, Integer[]>();

    protected void add(final int level, final int lowerBound, final int upperBound) {
      stack.put(level, new Integer[] { lowerBound, upperBound });
    }

    protected int size() {
      return stack.size();
    }

    protected int[] getLevelIndex() {
      final int[] levels = new int[stack.size()];
      final Iterator<Integer> it = stack.keySet().iterator();
      for (int i = 0; i < stack.size(); i++) {
        levels[i] = it.next();
      }
      return levels;
    }

    protected int[][] getConstraints() {
      final int[][] constraints = new int[stack.size()][2];
      final Iterator<Integer[]> it = stack.values().iterator();
      for (int i = 0; i < stack.size(); i++) {
        final Integer[] constraint = it.next();
        constraints[i] = new int[2];
        constraints[i][0] = constraint[0];
        constraints[i][1] = constraint[1];
      }
      return constraints;
    }

  }

  /**
   * The pointer to direct node query ancestor
   */
  protected NodeQuery ancestor;

  /**
   * Expert: Add a pointer to the node query ancestor
   * <p>
   * The pointer to node query ancestor is used to retrieve node constraints from
   * ancestors.
   */
  public void setAncestorPointer(final NodeQuery ancestor) {
    this.ancestor = ancestor;
  }

  protected DocsNodesAndPositionsEnum getDocsNodesAndPositionsEnum(final DocsAndPositionsEnum docsEnum) {
    // Map Lucene's docs enum to a SIREn's docs, nodes and positions enum
    final DocsNodesAndPositionsEnum sirenDocsEnum = SirenDocsEnum.map(docsEnum);

    // Retrieve constraints starting from the direct ancestor
    final ConstraintStack stack = new ConstraintStack();
    this.retrieveConstraint(this.ancestor, stack);

    // if at least one constraint has been found among the ancestors
    if (stack.size() > 0) {
      // add the interval constraint of the current node
      if (lowerBound != -1 && upperBound != -1) {
        stack.add(levelConstraint, lowerBound, upperBound);
      }
      return new IntervalConstrainedNodesEnum(sirenDocsEnum, levelConstraint,
        stack.getLevelIndex(), stack.getConstraints());
    }
    // if an interval constraint has been set for the current node
    else if (lowerBound != -1 && upperBound != -1) {
      // use the interval constraint of the current node
      return new SingleIntervalConstrainedNodesEnum(sirenDocsEnum,
        levelConstraint, new int[] { lowerBound, upperBound });
    }
    // if only a level constraint has been set for the current node
    else if (levelConstraint != -1) {
      return new LevelConstrainedNodesEnum(sirenDocsEnum, levelConstraint);
    }
    else {
      return sirenDocsEnum;
    }

  }

  private void retrieveConstraint(final NodeQuery query, final ConstraintStack stack) {
    if (query == null) {
      return;
    }

    // add a constraint only if lower and upper bounds are defined
    if (query.lowerBound != -1 && query.upperBound != -1) {
      stack.add(query.levelConstraint, query.lowerBound, query.upperBound);
    }

    // recursively traverse the ancestors
    this.retrieveConstraint(query.ancestor, stack);
  }

}
