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
  public void testIsAncestor() {
    int[] anc = new int[] { 0, 0, 0 };
    int[] desc = new int[] { 1, 0, 0 };
    assertTrue(NodeUtils.isAncestor(anc, desc));

    anc = new int[] { 1, 1, 0 };
    desc = new int[] { 1, 0, 0 };
    assertFalse(NodeUtils.isAncestor(anc, desc));

    anc = new int[] { 1, 1, 0 };
    desc = new int[] { 1, 1, 0 };
    assertFalse(NodeUtils.isAncestor(anc, desc));

    anc = new int[] { 1, 1 };
    desc = new int[] { 1, 1, 0 };
    assertTrue(NodeUtils.isAncestor(anc, desc));
  }

  @Test
  public void testIsAncestorOrEqual() {
    int[] anc = new int[] { 0, 0, 0 };
    int[] desc = new int[] { 1, 0, 0 };
    assertTrue(NodeUtils.isAncestorOrEqual(anc, desc));

    anc = new int[] { 1, 1, 0 };
    desc = new int[] { 1, 0, 0 };
    assertFalse(NodeUtils.isAncestorOrEqual(anc, desc));

    anc = new int[] { 1, 1, 0 };
    desc = new int[] { 1, 1, 0 };
    assertTrue(NodeUtils.isAncestorOrEqual(anc, desc));

    anc = new int[] { 1, 1 };
    desc = new int[] { 1, 1, 0 };
    assertTrue(NodeUtils.isAncestorOrEqual(anc, desc));
  }

}
