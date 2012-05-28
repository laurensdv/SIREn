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
import static org.sindice.siren.analysis.MockSirenToken.node;

import org.apache.lucene.util.IntsRef;
import org.junit.Test;

public class TestNodeUtils {

  @Test
  public void testCompare() {
    IntsRef n1 = node(0, 0, 0);
    IntsRef n2 = node(1, 0, 0);
    assertTrue(NodeUtils.compare(n1, n2) < 0);

    n1 = node(1, 1, 0);
    n2 = node(1, 0, 0);
    assertTrue(NodeUtils.compare(n1, n2) > 0);

    n1 = node(1, 1, 0);
    n2 = node(1, 1, 0);
    assertTrue(NodeUtils.compare(n1, n2) == 0);

    n1 = node(1, 1);
    n2 = node(1, 1, 0);
    assertTrue(NodeUtils.compare(n1, n2) < 0);
  }

  @Test
  public void testCompareAncestor() {
    IntsRef n1 = node(0, 0, 0);
    IntsRef n2 = node(1, 0, 0);
    assertTrue(NodeUtils.compareAncestor(n1, n2) < 0);

    n1 = node(1, 1, 0);
    n2 = node(1, 0, 0);
    assertTrue(NodeUtils.compareAncestor(n1, n2) > 0);

    n1 = node(1, 1, 0);
    n2 = node(1, 1, 0);
    assertTrue(NodeUtils.compareAncestor(n1, n2) > 0);

    n1 = node(1, 1);
    n2 = node(1, 1, 0);
    assertTrue(NodeUtils.compareAncestor(n1, n2) == 0);
  }

  @Test
  public void testIsConstraintSatisfied() {

    final int[] lb = new int[] {1,1,0};
    final int[] ub = new int[] {1,10,0};

    IntsRef node = node(1,5,1);
    assertTrue(NodeUtils.isConstraintSatisfied(node, lb, ub));

    node = node(1,1,0);
    assertTrue(NodeUtils.isConstraintSatisfied(node, lb, ub));

    node = node(1,10,0);
    assertTrue(NodeUtils.isConstraintSatisfied(node, lb, ub));

    node = node(1,1,0,0);
    assertTrue(NodeUtils.isConstraintSatisfied(node, lb, ub));

    node = node(1,10,0,0);
    assertTrue(NodeUtils.isConstraintSatisfied(node, lb, ub));

    node = node(1,2);
    assertTrue(NodeUtils.isConstraintSatisfied(node, lb, ub));

    node = node(1);
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub));

    node = node(1,0,1);
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub));

    node = node(1,10,1);
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub));

    node = node(1,10);
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub));

    node = node(1,1);
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub));
  }

  @Test
  public void testIsLevelConstraintSatisfied() {

    final int[] lb = new int[] {1,1,0};
    final int[] ub = new int[] {1,10,0};

    IntsRef node = node(1,5,1);
    assertTrue(NodeUtils.isConstraintSatisfied(node, lb, ub, 3));

    node = node(1,5,1);
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub, 2));

    node = node(1,1,0,0);
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub, 3));

    node = node(1,10,0,0);
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub, 3));

    node = node(1,10);
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub, 3));

    node = node(1,1);
    assertFalse(NodeUtils.isConstraintSatisfied(node, lb, ub, 3));
  }

}
