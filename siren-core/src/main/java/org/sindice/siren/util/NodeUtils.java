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

import org.apache.lucene.util.IntsRef;

/**
 * Reusable methods to manage and compare node paths.
 */
public class NodeUtils {

  /**
   * Compares the first node with the second node for order.
   * Returns a negative integer, zero, or a positive integer if the first node
   * is less than, equal to, or greater than the second node.
   */
  public static final int compare(final IntsRef n1, final IntsRef n2) {
    return compare(n1.ints, n1.length, n2.ints, n2.length);
  }

  private static final int compare(final int[] n1, final int n1Len,
                                   final int[] n2, final int n2Len) {
    for (int i = 0; i < n1Len && i < n2Len; i++) {
      if (n1[i] != n2[i]) {
        return n1[i] - n2[i];
      }
    }
    // exception, if node path is equal, check node path length
    return n1Len - n2Len;
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
  public static final int compareAncestor(final IntsRef n1, final IntsRef n2) {
    return compareAncestor(n1.ints, n1.length, n2.ints, n2.length);
  }

  public static final int compareAncestor(final int[] n1, final int n1Len,
                                          final int[] n2, final int n2Len) {
    for (int i = 0; i < n1Len && i < n2Len; i++) {
      if (n1[i] != n2[i]) {
        return n1[i] - n2[i];
      }
    }
    // exception, if node path is equal, check node path length
    return n1Len < n2Len ? 0 : 1;
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
   * Increase the size of the array if needed. Do not copy the content of the
   * original array into the new one.
   */
  public static final IntsRef grow(final IntsRef ref, final int minSize) {
    ref.ints = grow(ref.ints, minSize);
    return ref;
  }

  /**
   * Check if a node path is satisfying the constraints.
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
   * the node paths [0,1], [3,10], [2], [2,5] or [2,3,4] satisfy the
   * constraints. However, the node paths [0], [0,0], [3,11] or [3] do not
   * satisfy the constraints.
   */
  public static final boolean isConstraintSatisfied(final IntsRef node,
                                                    final int[] lowerBound,
                                                    final int[] upperBound) {
    // it is assumed that lowerBound.length == upperBound.length
    final int len = node.length > lowerBound.length ? lowerBound.length : node.length;

    for (int i = 0; i < len; i++) {
      if (node.ints[i] > lowerBound[i] && node.ints[i] < upperBound[i]) {
        return true;
      }
      if (node.ints[i] < lowerBound[i] || node.ints[i] > upperBound[i]) {
        return false;
      }
    }

    // if path equal until now, check if the node path is equal or descendant
    return node.length >= lowerBound.length ? true : false;
  }

  /**
   * Check if a node path is satisfying the constraints.
   * <p>
   * If <code>lowerBound</code> and <code>upperBound</code> are null, then
   * these constraints are ignored and only the level constraint is checked.
   * <p>
   * The <code>nodeLevel</code> parameter allows to force the node
   * to be on a certain level.
   * <br>
   * Given that the root of the tree (level 0) is the document id, the node
   * level constraint ranges from 1 to <code>Integer.MAX_VALUE</code>. A node
   * level constraint of 0 will always return false.
   * <br>
   * The sentinel value to ignore the node level constraint is -1.
   * <p>
   * Example: <br>
   * Given the lower bound constraint [0,1], upper bound constraint [3,10] and
   * the level constraint 1, then the node paths [0,1], [3,10] or [2,5] satisfy
   * the constraints. However, the node path [2,3,4] does not satisfy the
   * constraints.
   *
   * @see NodeUtils#isConstraintSatisfied(int[], int[], int[])
   */
  public static final boolean isConstraintSatisfied(final IntsRef node,
                                                    final int[] lowerBound,
                                                    final int[] upperBound,
                                                    final int nodeLevel) {
    // check level
    if (nodeLevel != -1 && node.length != nodeLevel) {
      return false;
    }

    // if bound set to sentinel value, ignore
    if (lowerBound == null || upperBound == null) {
      return true;
    }

    return isConstraintSatisfied(node, lowerBound, upperBound);
  }

}
