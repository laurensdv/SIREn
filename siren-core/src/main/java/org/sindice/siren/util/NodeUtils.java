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
 * @author Renaud Delbru [ 13 Jan 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.util;

/**
 * Reusable methods to manage and compare node paths.
 */
public class NodeUtils {

  /**
   * Check if a node path is the predecessor of another.
   */
  public static final boolean isPredecessor(final int[] anc, final int[] desc) {
    boolean equal = true;

    for (int i = 0; i < anc.length && i < desc.length; i++) {
      equal &= anc[i] == desc[i];
      if (anc[i] > desc[i]) {
        return false;
      }
    }

    // exception, if node path is equal, check node path length
    if (equal && (anc.length >= desc.length)) {
      return false;
    }

    return true;
  }

  /**
   * Check if a node path is the predecessor of or is equal to another.
   */
  public static final boolean isPredecessorOrEqual(final int[] anc, final int[] desc) {
    boolean equal = true;

    for (int i = 0; i < anc.length && i < desc.length; i++) {
      equal &= anc[i] == desc[i];
      if (anc[i] > desc[i]) {
        return false;
      }
    }

    // exception, if node path is equal, check node path length
    if (equal && (anc.length > desc.length)) {
      return false;
    }

    return true;
  }

  /**
   * Compares the first node with the second node for order.
   * Returns a negative integer, zero, or a positive integer if the first node
   * is less than, equal to, or greater than the second node.
   */
  public static final int compare(final int[] n1, final int[] n2) {
    for (int i = 0; i < n1.length && i < n2.length; i++) {
      if (n1[i] != n2[i]) {
        return n1[i] - n2[i];
      }
    }
    // exception, if node path is equal, check node path length
    return n1.length - n2.length;
  }

  /**
   * Compares the first node with the second node for order.
   * Returns:
   * <ul>
   * <li> a negative integer if the first node is a predecessor of the second
   * node,
   * <li> zero if the first node is an ancestor of the second node,
   * <li> a positive integer if the first node is equal to or greater than the
   * second node.
   * </ul>
   */
  public static final int compareAncestor(final int[] n1, final int[] n2) {
    for (int i = 0; i < n1.length && i < n2.length; i++) {
      if (n1[i] != n2[i]) {
        return n1[i] - n2[i];
      }
    }
    // exception, if node path is equal, check node path length
    return n1.length < n2.length ? 0 : 1;
  }

  /**
   * Increase the size of the array and copy the content of the original array
   * into the new one.
   */
  public static final int[] growAndCopy(final int[] array, final int minSize) {
    assert minSize >= 0: "size must be positive (got " + minSize + "): likely integer overflow?";
    if (array.length < minSize) {
      final int[] newArray = new int[minSize];
      System.arraycopy(array, 0, newArray, 0, array.length);
      return newArray;
    } else {
      return array;
    }
  }

  /**
   * Increase the size of the array if needed. Do not copy the content of the
   * original array into the new one.
   */
  public static final int[] grow(final int[] array, final int minSize) {
    assert minSize >= 0: "size must be positive (got " + minSize + "): likely integer overflow?";
    if (array.length < minSize) {
      final int[] newArray = new int[minSize];
      return newArray;
    } else {
      return array;
    }
  }

  /**
   * Check if a node path is satisfying the constraints.
   * <p>
   * If <code>levelConstraint</code> parameter is true, then check is the node
   * is on the same level than the constraints.
   * <p>
   * <b>NOTE:</b> The node path constraints must be of the same length.
   * <p>
   * The lower bound constraint is satisfied if the node path is either
   * <ul>
   * <li> following (in node path order) the lower bound,
   * <li> descendant of the lower bound, or
   * <li> equal to the lower bound.
   * </ul>
   * The upper bound constraint is satisfied if the node path is either
   * <ul>
   * <li> preceding (in node path order) the upper bound,
   * <li> descendant of the upper bound, or
   * <li> equal to the upper bound.
   * </ul>
   * <p>
   * Example: <br>
   * Given the lower bound constraint [0,1] and upper bound constraint [3,10],
   * the node paths [0,1], [3,10], [2,5] or [2,3,4] satisfy the constraints.
   * However, the node paths [0,0], [3,11] or [5] do not satisfy the
   * constraints.
   * <p>
   * The <code>isNodeLevelConstrained</code> parameter allows to force the node
   * to be on the same level than the constraint boundaries.
   * <p>
   * Example: <br>
   * Given the lower bound constraint [0,1] and upper bound constraint [3,10],
   * the node paths [0,1], [3,10] or [2,5] satisfy the constraints.
   * However, the node path [2,3,4] does not satisfy the constraints.
   */
  public static final boolean isConstraintSatisfied(final int[] node,
                                                    final int[] lowerBound,
                                                    final int[] upperBound,
                                                    final boolean isNodeLevelConstrained) {
    // it is assumed that lowerBound.length == upperBound.length
    if (isNodeLevelConstrained ? node.length != lowerBound.length : node.length < lowerBound.length) {
      return false;
    }

    for (int i = 0; i < lowerBound.length; i++) {
      if (node[i] > lowerBound[i] && node[i] < upperBound[i]) {
        return true;
      }
      if (node[i] < lowerBound[i] || node[i] > upperBound[i]) {
        return false;
      }
    }

    return true;
  }

}
