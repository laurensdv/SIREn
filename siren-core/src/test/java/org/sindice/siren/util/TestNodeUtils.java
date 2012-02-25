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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestNodeUtils {

  @Test
  public void testIsPredecessor() {
    int[] anc = new int[] { 0, 0, 0 };
    int[] desc = new int[] { 1, 0, 0 };
    assertTrue(NodeUtils.isPredecessor(anc, desc));

    anc = new int[] { 1, 1, 0 };
    desc = new int[] { 1, 0, 0 };
    assertFalse(NodeUtils.isPredecessor(anc, desc));

    anc = new int[] { 1, 1, 0 };
    desc = new int[] { 1, 1, 0 };
    assertFalse(NodeUtils.isPredecessor(anc, desc));

    anc = new int[] { 1, 1 };
    desc = new int[] { 1, 1, 0 };
    assertTrue(NodeUtils.isPredecessor(anc, desc));
  }

  @Test
  public void testIsPredecessorOrEqual() {
    int[] anc = new int[] { 0, 0, 0 };
    int[] desc = new int[] { 1, 0, 0 };
    assertTrue(NodeUtils.isPredecessorOrEqual(anc, desc));

    anc = new int[] { 1, 1, 0 };
    desc = new int[] { 1, 0, 0 };
    assertFalse(NodeUtils.isPredecessorOrEqual(anc, desc));

    anc = new int[] { 1, 1, 0 };
    desc = new int[] { 1, 1, 0 };
    assertTrue(NodeUtils.isPredecessorOrEqual(anc, desc));

    anc = new int[] { 1, 1 };
    desc = new int[] { 1, 1, 0 };
    assertTrue(NodeUtils.isPredecessorOrEqual(anc, desc));
  }

  @Test
  public void testCompare() {
    int[] n1 = new int[] { 0, 0, 0 };
    int[] n2 = new int[] { 1, 0, 0 };
    assertTrue(NodeUtils.compare(n1, n2) < 0);

    n1 = new int[] { 1, 1, 0 };
    n2 = new int[] { 1, 0, 0 };
    assertTrue(NodeUtils.compare(n1, n2) > 0);

    n1 = new int[] { 1, 1, 0 };
    n2 = new int[] { 1, 1, 0 };
    assertTrue(NodeUtils.compare(n1, n2) == 0);

    n1 = new int[] { 1, 1 };
    n2 = new int[] { 1, 1, 0 };
    assertTrue(NodeUtils.compare(n1, n2) < 0);
  }

  @Test
  public void testCompareAncestor() {
    int[] n1 = new int[] { 0, 0, 0 };
    int[] n2 = new int[] { 1, 0, 0 };
    assertTrue(NodeUtils.compareAncestor(n1, n2) < 0);

    n1 = new int[] { 1, 1, 0 };
    n2 = new int[] { 1, 0, 0 };
    assertTrue(NodeUtils.compareAncestor(n1, n2) > 0);

    n1 = new int[] { 1, 1, 0 };
    n2 = new int[] { 1, 1, 0 };
    assertTrue(NodeUtils.compareAncestor(n1, n2) > 0);

    n1 = new int[] { 1, 1 };
    n2 = new int[] { 1, 1, 0 };
    assertTrue(NodeUtils.compareAncestor(n1, n2) == 0);
  }

  @Test
  public void testIsConstraintSatisfied() {

    final int[] lb = new int[] {1,1,0};
    final int[] ub = new int[] {1,10,0};

    int[] node = new int[] {1,5,1};
    assertTrue(NodeUtils.isConstraintSatisfied(node, lb, ub, false));

    node = new int[] {1,1,0};
    assertTrue(NodeUtils.isConstraintSatisfied(node, lb, ub, false));

    node = new int[] {1,10,0};
    assertTrue(NodeUtils.isConstraintSatisfied(node, lb, ub, false));

    node = new int[] {1,1,0,0};
    assertTrue(NodeUtils.isConstraintSatisfied(node, lb, ub, false));

    node = new int[] {1,10,0,0};
    assertTrue(NodeUtils.isConstraintSatisfied(node, lb, ub, false));

    node = new int[] {1,0,1};
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub, false));

    node = new int[] {1,10,1};
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub, false));

    node = new int[] {1,10};
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub, false));

    node = new int[] {1,1};
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub, false));
  }

  @Test
  public void testIsLevelConstraintSatisfied() {

    final int[] lb = new int[] {1,1,0};
    final int[] ub = new int[] {1,10,0};

    int[] node = new int[] {1,5,1};
    assertTrue(NodeUtils.isConstraintSatisfied(node, lb, ub, true));

    node = new int[] {1,1,0,0};
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub, true));

    node = new int[] {1,10,0,0};
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub, true));

    node = new int[] {1,10};
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub, true));

    node = new int[] {1,1};
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub, true));
  }

}
